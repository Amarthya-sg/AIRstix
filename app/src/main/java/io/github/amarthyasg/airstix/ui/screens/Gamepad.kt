package io.github.amarthyasg.airstix.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import io.github.amarthyasg.VGP_Data_Exchange.GamepadReading
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.data.PreviewHeightDp
import io.github.amarthyasg.airstix.data.PreviewWidthDp
import io.github.amarthyasg.airstix.data.SettingsRepository
import io.github.amarthyasg.airstix.data.defaultButtonConfigs
import io.github.amarthyasg.airstix.data.defaultPollingDelay
import io.github.amarthyasg.airstix.network.ConnectionViewModel
import io.github.amarthyasg.airstix.ui.composables.DrawGamepad
import io.github.amarthyasg.airstix.ui.composables.sanitizeUngroupedDpadConfigs
import io.github.amarthyasg.airstix.ui.utils.findActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val tag = "GamePadScreen"

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun GamePad(
    connectionViewModel: ConnectionViewModel?,
    onNavigateBack: () -> Unit,
    onNavigateToMainMenu: () -> Unit,
    onNavigateToConnectionLost: (ipAddress: String, port: Int, error: String?) -> Unit,
    settingsRepository: SettingsRepository,
) {
    val gamepadState = remember { GamepadReading() }
    val context = LocalContext.current
    val pollingDelay =
        settingsRepository.pollingDelay.collectAsState(defaultPollingDelay).value.toLong()
    val faceButtonsGrouped =
        settingsRepository.faceButtonsGrouped.collectAsState(initial = true).value
    val dpadGrouped =
        settingsRepository.dpadGrouped.collectAsState(initial = true).value
    val rawButtonConfigs =
        settingsRepository.buttonConfigs.collectAsState(defaultButtonConfigs).value
    val buttonConfigs = remember(rawButtonConfigs, dpadGrouped) {
        if (!dpadGrouped) sanitizeUngroupedDpadConfigs(rawButtonConfigs) else rawButtonConfigs
    }

    val isStopping = remember { mutableStateOf(false) }

    // Zeroes all button/axis state and sends a final clean frame — does NOT disconnect.
    // Used when navigating away while the session should stay alive (e.g. Settings button).
    fun releaseAllButtons() {
        if (connectionViewModel != null && !isStopping.value) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    isStopping.value = true
                    gamepadState.ButtonsUp = gamepadState.ButtonsDown
                    gamepadState.ButtonsDown = 0
                    gamepadState.LeftThumbstickX = 0F
                    gamepadState.LeftThumbstickY = 0F
                    gamepadState.RightThumbstickX = 0F
                    gamepadState.RightThumbstickY = 0F
                    gamepadState.LeftTrigger = 0F
                    gamepadState.RightTrigger = 0F
                    connectionViewModel.enqueueGamepadState(gamepadState)
                    Log.d(tag, "Buttons released, session kept alive")
                } catch (e: Exception) {
                    Log.d(tag, "Error during releaseAllButtons: ${e.message}")
                }
            }
        }
    }

    // Zeroes state AND disconnects. Used by ON_DESTROY for real app closure.
    fun disconnectSession() {
        if (connectionViewModel != null && !isStopping.value) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    isStopping.value = true
                    gamepadState.ButtonsUp = gamepadState.ButtonsDown
                    gamepadState.ButtonsDown = 0
                    gamepadState.LeftThumbstickX = 0F
                    gamepadState.LeftThumbstickY = 0F
                    gamepadState.RightThumbstickX = 0F
                    gamepadState.RightThumbstickY = 0F
                    gamepadState.LeftTrigger = 0F
                    gamepadState.RightTrigger = 0F
                    connectionViewModel.enqueueGamepadState(gamepadState)
                    connectionViewModel.disconnect()
                    Log.d(tag, "Disconnected")
                } catch (e: Exception) {
                    Log.d(tag, "Error during disconnect: ${e.message}")
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth.value.toInt()
        val screenHeight = maxHeight.value.toInt()

        DrawGamepad(
            widthDp = screenWidth,
            heightDp = screenHeight,
            gamepadState = gamepadState,
            buttonConfigs = buttonConfigs,
            faceButtonsGrouped = faceButtonsGrouped,
            dpadGrouped = dpadGrouped,
            onSettingsClick = {
                releaseAllButtons()
                onNavigateToMainMenu()
            }
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val activity = LocalContext.current.findActivity()
    DisposableEffect(lifecycleOwner, activity) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY
                && activity?.isChangingConfigurations != true
            ) {
                disconnectSession()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val connectionState by connectionViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(connectionState) {
        val state = connectionState
        if (state != null && !state.connected && state.error != null) {
            onNavigateToConnectionLost(state.ipAddress, state.port, state.error)
        }
    }

    val startAfter = 100L

    LaunchedEffect(gamepadState, pollingDelay) {
        delay(startAfter)

        val lastSentState = GamepadReading()
        var lastSentTime = 0L

        while (connectionViewModel != null && !isStopping.value) {
            val currentButtonsDown = gamepadState.ButtonsDown
            val currentButtonsUp = gamepadState.ButtonsUp
            val currentLeftThumbstickX = gamepadState.LeftThumbstickX
            val currentLeftThumbstickY = gamepadState.LeftThumbstickY
            val currentRightThumbstickX = gamepadState.RightThumbstickX
            val currentRightThumbstickY = gamepadState.RightThumbstickY
            val currentLeftTrigger = gamepadState.LeftTrigger
            val currentRightTrigger = gamepadState.RightTrigger

            val isChanged = currentButtonsDown != lastSentState.ButtonsDown ||
                    currentButtonsUp != lastSentState.ButtonsUp ||
                    currentLeftThumbstickX != lastSentState.LeftThumbstickX ||
                    currentLeftThumbstickY != lastSentState.LeftThumbstickY ||
                    currentRightThumbstickX != lastSentState.RightThumbstickX ||
                    currentRightThumbstickY != lastSentState.RightThumbstickY ||
                    currentLeftTrigger != lastSentState.LeftTrigger ||
                    currentRightTrigger != lastSentState.RightTrigger

            val currentTime = System.currentTimeMillis()
            if (isChanged || (currentTime - lastSentTime >= 500L)) {
                connectionViewModel.enqueueGamepadState(gamepadState)

                lastSentState.ButtonsDown = currentButtonsDown
                lastSentState.ButtonsUp = currentButtonsUp
                lastSentState.LeftThumbstickX = currentLeftThumbstickX
                lastSentState.LeftThumbstickY = currentLeftThumbstickY
                lastSentState.RightThumbstickX = currentRightThumbstickX
                lastSentState.RightThumbstickY = currentRightThumbstickY
                lastSentState.LeftTrigger = currentLeftTrigger
                lastSentState.RightTrigger = currentRightTrigger

                lastSentTime = currentTime
            }

            gamepadState.ButtonsUp = 0
            delay(pollingDelay)
        }
    }
}

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Design Preview (Light)",
    device = "spec:width=${PreviewWidthDp}dp,height=${PreviewHeightDp}dp,orientation=landscape,dpi=420",
)
@Preview(
    name = "Design Preview (Dark)",
    device = "spec:width=${PreviewWidthDp}dp,height=${PreviewHeightDp}dp,orientation=landscape,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Phone - Landscape (Light)",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true
)
@Preview(
    name = "Phone - Landscape (Dark)",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Tablet (Light)",
    device = TABLET,
    showSystemUi = true
)
@Preview(
    name = "Tablet (Dark)",
    device = TABLET,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Desktop (Light)",
    device = DESKTOP,
    showSystemUi = true
)
@Preview(
    name = "Desktop (Dark)",
    device = DESKTOP,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class MultiDevicePreview

@MultiDevicePreview
@Composable
fun GamePadPreview() {
    PreviewBase {
        GamePad(
            connectionViewModel = null,
            onNavigateBack = {},
            onNavigateToMainMenu = {},
            onNavigateToConnectionLost = { _, _, _ -> },
            settingsRepository = SettingsRepository(LocalContext.current)
        )
    }
}
