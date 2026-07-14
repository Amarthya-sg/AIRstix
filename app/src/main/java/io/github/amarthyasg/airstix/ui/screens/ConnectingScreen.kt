package io.github.amarthyasg.airstix.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.data.PreviewHeightDp
import io.github.amarthyasg.airstix.data.PreviewWidthDp
import io.github.amarthyasg.airstix.network.ConnectionViewModel
import io.github.amarthyasg.airstix.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConnectingScreen(
    onNavigateToMainMenu: () -> Unit,
    onNavigateBack: () -> Unit,
    connectionViewModel: ConnectionViewModel?,
    ipAddress: String,
    port: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDiagnosticsDialog by rememberSaveable { mutableStateOf(false) }

    // Get the current connection state
    val connectionState by connectionViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(null) }

    val connectErrorParamsStr = stringResource(R.string.connect_error_params)
    val connectingFailedMsg =
        connectionState?.error?.let { stringResource(R.string.connecting_failed, it) }

    // Initiate connection when entering screen
    LaunchedEffect(ipAddress, port) {
        try {
            Log.d("ConnectingScreen", "Initiating connection to $ipAddress:$port")
            connectionViewModel?.connect(ipAddress, port.toInt())
        } catch (e: Exception) {
            Log.e("ConnectingScreen", "Failed to initiate connection: ${e.message}")
            snackbarHostState.showSnackbar(
                message = connectErrorParamsStr + ": ${e.message}",
                duration = SnackbarDuration.Short
            )
            onNavigateBack()
        }
    }

    // Auto-redirect to Main Menu on success (brief pause so the success feedback is visible)
    LaunchedEffect(connectionState?.connected) {
        if (connectionState?.connected == true) {
            delay(600L)
            onNavigateToMainMenu()
        }
    }

    // Watch for connection errors
    LaunchedEffect(
        connectionState?.error,
        connectionState?.isConnecting
    ) {
        connectionState?.let { state ->
            if (!state.isConnecting && state.error != null && !state.connected) {
                // Connection failed, show error and stay on screen
                Log.d("ConnectingScreen", "Connection failed: ${state.error}")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = connectingFailedMsg ?: "",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Handle back navigation to cancel connection
    BackHandler {
        // Cancel any pending connection
        Log.d("ConnectingScreen", "Back pressed, canceling connection")
        connectionViewModel?.disconnect()
        onNavigateBack()
    }

    // UI for connecting screen
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (connectionState?.connected == true) {
                // Auto-redirecting — show brief success feedback while LaunchedEffect counts down
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = SuccessGreen.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.connecting_success),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else if (connectionState?.isConnecting == true) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.width(22.dp))
                    Text(
                        text = stringResource(R.string.connecting_status, ipAddress, port),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                // Show error message if there is one
                connectionState?.error?.let { error ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Error Warning Icon on the left
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Right column containing text and actions
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.connecting_error_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { connectionViewModel?.connect(ipAddress, port.toInt()) },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(stringResource(R.string.connecting_retry))
                                }

                                Button(
                                    onClick = {
                                        connectionViewModel?.runDiagnostics(context)
                                        showDiagnosticsDialog = true
                                    },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(stringResource(R.string.connecting_diagnostics))
                                }

                                TextButton(onClick = onNavigateBack) {
                                    Text(stringResource(R.string.back))
                                }
                            }
                        }
                    }
                } ?: run {
                    // No error but also not connecting - initial state
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.width(22.dp))
                        Text(
                            text = stringResource(R.string.connecting_preparing),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        if (showDiagnosticsDialog) {
            DiagnosticsDialog(
                connectionState = connectionState,
                onDismiss = {
                    showDiagnosticsDialog = false
                    connectionViewModel?.clearDiagnostics()
                }
            )
        }
    }
}

@Composable
fun DiagnosticsDialog(
    connectionState: io.github.amarthyasg.airstix.network.ConnectionState?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.diagnostics_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (connectionState?.isRunningDiagnostics == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.diagnostics_running))
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(connectionState?.diagnosticResults ?: emptyList()) { result ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (result.isPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.isPassed) SuccessGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text = result.message,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.diagnostics_close))
            }
        }
    )
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
private fun DiagnosticsDialogPreview() {
    PreviewBase {
        val serverIP = "192.0.2.1" // Use TEST-NET-1 IP for preview
        val clientIP = "192.0.2.5" // Another TEST-NET-1 IP for client
        val port = 12345
        val sampleState = io.github.amarthyasg.airstix.network.ConnectionState(
            connected = false,
            ipAddress = serverIP,
            port = port,
            error = null,
            isConnecting = false,
            isRunningDiagnostics = true,
            diagnosticResults = listOf(
                io.github.amarthyasg.airstix.network.NetworkDiagnostics.DiagnosticResult(
                    io.github.amarthyasg.airstix.network.NetworkDiagnostics.DiagnosticStep.WIFI,
                    true,
                    "Device connected to Wi-Fi"
                ),
                io.github.amarthyasg.airstix.network.NetworkDiagnostics.DiagnosticResult(
                    io.github.amarthyasg.airstix.network.NetworkDiagnostics.DiagnosticStep.IP,
                    true,
                    "Local IP: $clientIP"
                ),
                io.github.amarthyasg.airstix.network.NetworkDiagnostics.DiagnosticResult(
                    io.github.amarthyasg.airstix.network.NetworkDiagnostics.DiagnosticStep.PING,
                    false,
                    "Ping to server failed",
                    "Timeout"
                )
            )
        )

        DiagnosticsDialog(
            connectionState = sampleState,
            onDismiss = {}
        )
    }
}
