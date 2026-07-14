package io.github.amarthyasg.airstix.ui.screens

import android.content.Intent
import android.net.InetAddresses
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.data.PreviewHeightDp
import io.github.amarthyasg.airstix.data.PreviewWidthDp
import io.github.amarthyasg.airstix.ui.components.QRScanResult
import io.github.amarthyasg.airstix.ui.components.rememberQRCodeScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.github.amarthyasg.airstix.ui.composables.HUDViewfinder
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ButtonDefaults

const val LOG_TAG = "ConnectMenu"

private fun getIP(qrCode: String): String {
    val splitTill = qrCode.lastIndexOf(":")
    if (splitTill == -1) return qrCode
    return qrCode.substring(0, splitTill)
}

private fun getPort(qrCode: String): String {
    val splitAt = qrCode.lastIndexOf(":")
    if (splitAt == -1) return qrCode
    return qrCode.substring(splitAt + 1)
}

@Suppress("DEPRECATION")
private fun validateIP(ipAddress: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        InetAddresses.isNumericAddress(ipAddress)
    } else {
        Patterns.IP_ADDRESS.matcher(ipAddress).matches()
    }
}

private fun validatePort(port: String): Boolean {
    val minPort = 1
    val maxPort = 65535
    return port.toIntOrNull().let { it != null && it in minPort..maxPort }
}

private fun processQRScanResult(
    result: QRScanResult,
    onNavigateToConnectingScreen: (String, String) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    context: android.content.Context
) {
    when (result) {
        is QRScanResult.Success -> {
            try {
                val qrCode = result.content
                Log.d(LOG_TAG, qrCode)
                val ipAddress = getIP(qrCode)
                val port = getPort(qrCode)

                if (validateIP(ipAddress) && validatePort(port)) {
                    // Navigate to the connecting screen with the IP and port
                    onNavigateToConnectingScreen(ipAddress, port)
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.connect_qr_error_format),
                        )
                    }
                }
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.connect_qr_error_processing,
                            e.message ?: e.toString()
                        ),
                    )
                }
            }
        }

        is QRScanResult.Error -> {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connect_qr_error_scanning, result.message),
                )
            }
        }

        is QRScanResult.PermissionDenied -> {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connect_camera_permission_denied),
                )
            }
        }

        is QRScanResult.Cancelled -> { // User canceled the QR code scan
        }
    }
}

@Composable
fun ConnectMenu(
    onNavigateToConnectingScreen: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    initialIp: String,
    initialPort: String
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val downloadsUrl = "https://github.com/Amarthya-sg/AIRstix-server"

    val qrCodeScanner = rememberQRCodeScanner { result ->
        processQRScanResult(
            result,
            onNavigateToConnectingScreen = onNavigateToConnectingScreen,
            snackbarHostState = snackbarHostState,
            scope = scope,
            context = context
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        var ipAddress by rememberSaveable { mutableStateOf(initialIp) }
        var port by rememberSaveable { mutableStateOf(initialPort) }
        var isIPValid by rememberSaveable { mutableStateOf(validateIP(initialIp)) }
        var isPortValid by rememberSaveable { mutableStateOf(validatePort(initialPort)) }

        // Update state when parameters arrive asynchronously from DataStore
        LaunchedEffect(initialIp, initialPort) {
            if (ipAddress.isBlank() && initialIp.isNotBlank()) {
                ipAddress = initialIp
                isIPValid = validateIP(initialIp)
            }
            if (port.isBlank() && initialPort.isNotBlank()) {
                port = initialPort
                isPortValid = validatePort(initialPort)
            }
        }

        val focusManager = LocalFocusManager.current
        val connectErrorIpStr = stringResource(R.string.connect_error_ip)
        val connectErrorPortStr = stringResource(R.string.connect_error_port)
        val connectErrorParamsStr = stringResource(R.string.connect_error_params)

        fun attemptToConnect() {
            if (isIPValid && isPortValid) {
                // Navigate to the connecting screen with the IP and port
                onNavigateToConnectingScreen(ipAddress, port)
            } else {
                scope.launch {
                    val errorMessage = when {
                        !isIPValid -> connectErrorIpStr
                        !isPortValid -> connectErrorPortStr
                        else -> connectErrorParamsStr
                    }
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Viewfinder Corner Brackets
            HUDViewfinder(modifier = Modifier.padding(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Quick Pair
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "01 · QUICK PAIR",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = { qrCodeScanner() },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.connect_scan_qr),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.connect_download_server),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, downloadsUrl.toUri())
                                    context.startActivity(intent)
                                }
                                .padding(8.dp)
                        )
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.7f)
                        .width(1.dp)
                        .background(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // Right Column: Manual Connect
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(start = 24.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "02 · MANUAL ENTRY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val connectButtonStr = stringResource(R.string.connect_button)

                    TextField(
                        value = ipAddress,
                        onValueChange = {
                            ipAddress = it
                            isIPValid = validateIP(ipAddress)
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.connect_ip_label),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = port,
                        onValueChange = {
                            port = it
                            isPortValid = validatePort(port)
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.connect_port_label),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                attemptToConnect()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { attemptToConnect() },
                        border = BorderStroke(1.dp, if (isIPValid && isPortValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        shape = MaterialTheme.shapes.medium,
                        enabled = isIPValid && isPortValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics {
                                text = AnnotatedString(connectButtonStr)
                            },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = connectButtonStr,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "→",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Top-Left Standard Back button to return to Main Menu
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun ConnectMenuPreview() {
    PreviewBase {
        ConnectMenu(
            onNavigateToConnectingScreen = { _, _ -> },
            onNavigateBack = {},
            initialIp = "",
            initialPort = "12345"
        )
    }
}
