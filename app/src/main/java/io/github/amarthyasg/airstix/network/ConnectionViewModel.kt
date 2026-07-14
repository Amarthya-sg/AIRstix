package io.github.amarthyasg.airstix.network

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.amarthyasg.VGP_Data_Exchange.GamepadReading
import io.github.amarthyasg.airstix.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.LinkedBlockingQueue

class ConnectionViewModel(private val onConnectionSuccess: suspend (String, Int) -> Unit = { _, _ -> }) :
    ViewModel() {

    private val tag = this::class.java.simpleName
    private var clientSocket: Socket? = null // Internal storage for the socket
    private var bufferedOutputStream: java.io.BufferedOutputStream? = null

    // Replace gamepad state queue with a general command queue
    private val commandQueue = LinkedBlockingQueue<NetworkCommand>()

    // Job for the command processor
    private var commandProcessorJob: Job? = null

    // Job for background socket reading/EOF detection
    private var readListenerJob: Job? = null

    // Expose screen UI state
    private val _uiState = MutableStateFlow(ConnectionState())
    val uiState: StateFlow<ConnectionState> = _uiState.asStateFlow()

    /**
     * Enqueues a network command for processing.
     * This is the core method for all network operations.
     *
     * @param command The network command to enqueue
     */
    private fun enqueueCommand(command: NetworkCommand) {
        // Don't enqueue send commands if disconnected or disconnecting
        // But always allow Connect and Disconnect commands
        if (command !is NetworkCommand.Connect &&
            command !is NetworkCommand.Disconnect &&
            (_uiState.value.error != null || !_uiState.value.connected)
        ) {
            Log.d(tag, "Skipping command enqueue while disconnected: $command")
            return
        }

        commandQueue.offer(command)

        // Start the command processor if not already running
        if (commandProcessorJob == null || commandProcessorJob?.isActive != true) {
            startCommandProcessor()
        }
    }

    /**
     * Enqueues a gamepad state for sending.
     * This is the public API for sending gamepad updates.
     *
     * @param gamepadState The gamepad state to enqueue
     */
    fun enqueueGamepadState(gamepadState: GamepadReading) {
        enqueueCommand(NetworkCommand.SendGamepadState(gamepadState))
    }

    /**
     * Enqueues a string to send.
     * This is primarily for testing purposes.
     *
     * @param string The string to send
     */
    @Suppress("unused")
    fun enqueueString(string: String) {
        enqueueCommand(NetworkCommand.SendString(string))
    }

    /**
     * Connect to a server at the given IP address and port.
     * This now enqueues a connection command.
     */
    fun connect(ipAddress: String, port: Int) {
        _uiState.update {
            it.copy(
                isConnecting = true,
                ipAddress = ipAddress,
                port = port,
                error = null,
                connected = false
            )
        }

        enqueueCommand(NetworkCommand.Connect(ipAddress, port))
    }

    /**
     * Disconnect from the server.
     * This now enqueues a disconnect command.
     */
    fun disconnect() {
        enqueueCommand(NetworkCommand.Disconnect)
    }

    /**
     * Starts the command processor job that executes network commands.
     * Uses a blocking queue implementation to efficiently wait for new commands.
     */
    private fun startCommandProcessor() {
        // Cancel existing job if any
        commandProcessorJob?.cancel()

        // Start new processor job
        commandProcessorJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    // This will block until a command is available
                    // Process based on command type
                    when (val command = commandQueue.take()) {
                        is NetworkCommand.Connect -> processConnectCommand(command)
                        is NetworkCommand.SendGamepadState -> processSendGamepadStateCommand(command)
                        is NetworkCommand.SendString -> processSendStringCommand(command)
                        is NetworkCommand.Disconnect -> {
                            processDisconnectCommand()
                            // Exit the loop after disconnect
                            break
                        }
                    }
                } catch (e: InterruptedException) {
                    // The coroutine was canceled, just exit the loop
                    Log.d(tag, "Command processor interrupted: ${e.message}")
                    break
                } catch (e: CancellationException) {
                    // The coroutine was canceled, just exit the loop
                    Log.d(tag, "Command processor canceled")
                    break
                } catch (e: Exception) {
                    // Catch any other exceptions to prevent the loop from crashing
                    Log.e(tag, "Command processor error: ${e.message}", e)
                    _uiState.update {
                        if (it.error == null) {
                            it.copy(
                                error = "Unexpected error: ${e.message ?: "Unknown"}",
                                connected = false
                            )
                        } else {
                            it.copy(connected = false)
                        }
                    }

                    // Force disconnect on fatal errors preserving error state
                    try {
                        processDisconnectCommand(preserveError = true)
                    } catch (e2: Exception) {
                        Log.e(tag, "Error during emergency disconnect: ${e2.message}")
                    }
                    break
                }
            }

            Log.d(tag, "Command processor stopped")
        }
    }

    /**
     * Process a connect command.
     */
    private fun processConnectCommand(command: NetworkCommand.Connect) {
        try {
            // Create and configure socket
            val socket = Socket()
            socket.tcpNoDelay = true
            socket.setPerformancePreferences(1, 2, 0)
            socket.trafficClass = 0x10 // IPTOS_LOWDELAY
            // Set timeout to 5 seconds
            val timeout = 5000 // in milliseconds

            try {
                socket.connect(java.net.InetSocketAddress(command.ipAddress, command.port), timeout)
                clientSocket = socket // Store the connected socket
                bufferedOutputStream = java.io.BufferedOutputStream(socket.getOutputStream())

                _uiState.update {
                    it.copy(
                        connected = true,
                        isConnecting = false,
                        error = null
                    )
                }
                Log.d(tag, "Connected: $clientSocket")

                startReadListener(socket)

                // Invoke callback on successful connection
                viewModelScope.launch {
                    try {
                        onConnectionSuccess(command.ipAddress, command.port)
                    } catch (e: Exception) {
                        Log.e(tag, "Error saving connection credentials: ${e.message}", e)
                    }
                }
            } catch (e: IOException) {
                Log.e(tag, "Connection failed: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        connected = false,
                        isConnecting = false,
                        error = e.message ?: "Unknown connection error"
                    )
                }
                socket.close() // Ensure socket is closed on error
            }
        } catch (e: Exception) {
            Log.e(tag, "Connect error: ${e.message}", e)
            _uiState.update {
                it.copy(
                    connected = false,
                    isConnecting = false,
                    error = "Connect error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Process a send gamepad state command.
     */
    private fun handleSendError(e: IOException) {
        Log.e(tag, "Error sending data: ${e.message}", e)
        _uiState.update {
            if (it.error == null) {
                it.copy(
                    error = "Connection lost: ${e.message ?: "Unknown error"}",
                    connected = false
                )
            } else {
                it.copy(connected = false)
            }
        }
        Log.d(tag, "Socket error detected during send, forcing disconnect")
        processDisconnectCommand(preserveError = true)
    }

    private fun processSendGamepadStateCommand(command: NetworkCommand.SendGamepadState) {
        if (!_uiState.value.connected || clientSocket == null || bufferedOutputStream == null) {
            // Skip sending if we're not connected
            return
        }

        try {
            val out = bufferedOutputStream
            if (out != null) {
                command.gamepadState.marshal(out, null)
                out.flush()
            } else {
                throw IOException("Buffered output stream is null")
            }
        } catch (e: IOException) {
            handleSendError(e)
            throw e
        }
    }

    /**
     * Process a send string command.
     */
    private fun processSendStringCommand(command: NetworkCommand.SendString) {
        if (!_uiState.value.connected || clientSocket == null || bufferedOutputStream == null) {
            // Skip sending if we're not connected
            return
        }

        try {
            val out = bufferedOutputStream
            if (out != null) {
                out.write(command.string.toByteArray())
                out.flush()
            } else {
                throw IOException("Buffered output stream is null")
            }
        } catch (e: IOException) {
            handleSendError(e)
            throw e
        }
    }

    /**
     * Process a disconnect command.
     */
    private fun processDisconnectCommand(preserveError: Boolean = false) {
        readListenerJob?.cancel()
        readListenerJob = null

        try {
            bufferedOutputStream?.close()
        } catch (e: IOException) {
            Log.e(tag, "Error closing buffered output stream: ${e.message}", e)
        } finally {
            bufferedOutputStream = null
        }

        try {
            clientSocket?.close()
        } catch (e: IOException) {
            Log.e(tag, "Error closing socket: ${e.message}", e)
        } finally {
            clientSocket = null
        }

        commandQueue.clear()

        if (!preserveError) {
            _uiState.update {
                ConnectionState() // Reset to initial state
            }
        } else {
            _uiState.update {
                it.copy(
                    connected = false
                )
            }
        }

        Log.d(tag, "Disconnected (preserveError=$preserveError)")
    }

    private fun startReadListener(socket: Socket) {
        readListenerJob?.cancel()
        readListenerJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                while (isActive) {
                    val len = inputStream.read(buffer)
                    if (len == -1) {
                        Log.d(tag, "Read -1: Server closed socket")
                        handleDisconnectOnError("Connection lost: Server closed connection")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.d(tag, "Read exception: ${e.message}")
                if (_uiState.value.connected) {
                    handleDisconnectOnError("Connection lost: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    private fun handleDisconnectOnError(errorMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            processDisconnectCommand(preserveError = true)
            _uiState.update {
                it.copy(
                    error = errorMessage
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure disconnection when ViewModel is cleared
        if (_uiState.value.connected || clientSocket != null) {
            Log.d(tag, "ViewModel cleared, ensuring disconnection.")
            try {
                processDisconnectCommand(preserveError = false)
            } catch (e: Exception) {
                Log.e(tag, "Error during cleanup: ${e.message}", e)
            } finally {
                commandProcessorJob?.cancel()
                commandProcessorJob = null
            }
        }
    }

    /**
     * Runs a series of network diagnostics to troubleshoot connection issues.
     * @param context Android context to access system services
     */
    fun runDiagnostics(context: Context) {
        val currentState = _uiState.value
        val ipAddress = currentState.ipAddress
        val port = currentState.port

        if (ipAddress.isBlank() || port == -1) {
            Log.e(tag, "Cannot run diagnostics: IP or Port missing")
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunningDiagnostics = true,
                    diagnosticResults = emptyList()
                )
            }

            val diagnostics = NetworkDiagnostics(context)
            val results = mutableListOf<NetworkDiagnostics.DiagnosticResult>()

            // Step 1: Wi-Fi/Network
            val isWifiConnected = diagnostics.checkNetworkConnectivity()
            results.add(
                NetworkDiagnostics.DiagnosticResult(
                    NetworkDiagnostics.DiagnosticStep.WIFI,
                    isWifiConnected,
                    if (isWifiConnected) context.getString(R.string.diagnostics_pass_wifi)
                    else context.getString(R.string.diagnostics_fail_wifi)
                )
            )
            _uiState.update { it.copy(diagnosticResults = results.toList()) }
            if (!isWifiConnected) {
                _uiState.update { it.copy(isRunningDiagnostics = false) }
                return@launch
            }

            // Step 2: Local IP
            val localIp = diagnostics.getLocalIpAddress()
            val hasLocalIp = localIp != null
            results.add(
                NetworkDiagnostics.DiagnosticResult(
                    NetworkDiagnostics.DiagnosticStep.IP,
                    hasLocalIp,
                    if (hasLocalIp) context.getString(R.string.diagnostics_pass_ip, localIp)
                    else context.getString(R.string.diagnostics_fail_ip)
                )
            )
            _uiState.update { it.copy(diagnosticResults = results.toList()) }
            if (!hasLocalIp) {
                _uiState.update { it.copy(isRunningDiagnostics = false) }
                return@launch
            }

            // Step 3: Subnet check
            val sameSubnet = diagnostics.isSameSubnet(localIp, ipAddress)
            results.add(
                NetworkDiagnostics.DiagnosticResult(
                    NetworkDiagnostics.DiagnosticStep.SUBNET,
                    sameSubnet,
                    if (sameSubnet) context.getString(R.string.diagnostics_pass_subnet)
                    else context.getString(R.string.diagnostics_fail_subnet, localIp, ipAddress)
                )
            )
            _uiState.update { it.copy(diagnosticResults = results.toList()) }

            // Step 4: Ping
            val canPing = diagnostics.pingHost(ipAddress)
            results.add(
                NetworkDiagnostics.DiagnosticResult(
                    NetworkDiagnostics.DiagnosticStep.PING,
                    canPing,
                    if (canPing) context.getString(R.string.diagnostics_pass_ping)
                    else context.getString(R.string.diagnostics_fail_ping)
                )
            )
            _uiState.update { it.copy(diagnosticResults = results.toList()) }

            // Step 5: Port check
            val canConnectPort = diagnostics.checkPort(ipAddress, port)
            results.add(
                NetworkDiagnostics.DiagnosticResult(
                    NetworkDiagnostics.DiagnosticStep.PORT,
                    canConnectPort,
                    if (canConnectPort) context.getString(R.string.diagnostics_pass_port, port)
                    else context.getString(R.string.diagnostics_fail_port, port)
                )
            )
            _uiState.update {
                it.copy(
                    diagnosticResults = results.toList(),
                    isRunningDiagnostics = false
                )
            }
        }
    }

    /**
     * Clears the current diagnostic results.
     */
    fun clearDiagnostics() {
        _uiState.update { it.copy(diagnosticResults = emptyList(), isRunningDiagnostics = false) }
    }
}

