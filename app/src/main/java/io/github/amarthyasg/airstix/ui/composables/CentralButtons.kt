package io.github.amarthyasg.airstix.ui.composables

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.amarthyasg.VGP_Data_Exchange.GameButtons
import io.github.amarthyasg.VGP_Data_Exchange.GamepadReading
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.ButtonAnchor
import io.github.amarthyasg.airstix.data.ButtonComponent
import io.github.amarthyasg.airstix.data.ButtonConfig
import io.github.amarthyasg.airstix.ui.utils.HapticUtils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.alpha

/**
 * Convert ButtonAnchor to Compose Alignment
 */
private fun ButtonAnchor.toAlignment(): Alignment = when (this) {
    ButtonAnchor.TOP_LEFT -> Alignment.TopStart
    ButtonAnchor.TOP_CENTER -> Alignment.TopCenter
    ButtonAnchor.TOP_RIGHT -> Alignment.TopEnd
    ButtonAnchor.CENTER_LEFT -> Alignment.CenterStart
    ButtonAnchor.CENTER -> Alignment.Center
    ButtonAnchor.CENTER_RIGHT -> Alignment.CenterEnd
    ButtonAnchor.BOTTOM_LEFT -> Alignment.BottomStart
    ButtonAnchor.BOTTOM_CENTER -> Alignment.BottomCenter
    ButtonAnchor.BOTTOM_RIGHT -> Alignment.BottomEnd
}

enum class ShoulderButtonType {
    LEFT, RIGHT
}

enum class MenuButtonType {
    VIEW, MENU
}

@Composable
fun ShoulderButton(
    type: ShoulderButtonType,
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    val view = LocalView.current
    val gameButton = when (type) {
        ShoulderButtonType.LEFT -> GameButtons.LeftShoulder
        ShoulderButtonType.RIGHT -> GameButtons.RightShoulder
    }
    val text = when (type) {
        ShoulderButtonType.LEFT -> stringResource(R.string.button_l_shoulder)
        ShoulderButtonType.RIGHT -> stringResource(R.string.button_r_shoulder)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // See https://stackoverflow.com/a/69157877/8659747
    if (isPressed) {
        DisposableEffect(Unit) {
            Log.d(gameButton.name, "Pressed")
            if (hapticEnabled) {
                HapticUtils.performButtonPressFeedback(view)
            }
            gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
            onDispose {
                Log.d(gameButton.name, "Released")
                if (hapticEnabled) {
                    HapticUtils.performButtonReleaseFeedback(view)
                }
                gamepadState.ButtonsDown =
                    gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    Button(
        modifier = modifier
            .heightIn(min = size)
            .widthIn(min = size * 1.5f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        onClick = { },
        interactionSource = interactionSource,
    ) {
        Text(text)
    }
}

@Composable
fun MenuButton(
    type: MenuButtonType,
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    val view = LocalView.current
    val gameButton = when (type) {
        MenuButtonType.VIEW -> GameButtons.View
        MenuButtonType.MENU -> GameButtons.Menu
    }
    // "−" for Select (View) and "+" for Start (Menu)
    val glyph = when (type) {
        MenuButtonType.VIEW -> "−"
        MenuButtonType.MENU -> "+"
    }
    val contentDesc = when (type) {
        MenuButtonType.VIEW -> stringResource(R.string.content_desc_button, "Select (−)")
        MenuButtonType.MENU -> stringResource(R.string.content_desc_button, "Start (+)")
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // See https://stackoverflow.com/a/69157877/8659747
    if (isPressed) {
        DisposableEffect(Unit) {
            Log.d(gameButton.name, "Pressed")
            if (hapticEnabled) {
                HapticUtils.performButtonPressFeedback(view)
            }
            gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
            onDispose {
                Log.d(gameButton.name, "Released")
                if (hapticEnabled) {
                    HapticUtils.performButtonReleaseFeedback(view)
                }
                gamepadState.ButtonsDown =
                    gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    OutlinedIconButton(
        modifier = modifier.size(size),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        onClick = { },
        interactionSource = interactionSource,
    ) {
        Text(
            text = glyph,
            color = MaterialTheme.colorScheme.primary,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = androidx.compose.ui.unit.TextUnit(
                    value = size.value * 0.4f,
                    type = androidx.compose.ui.unit.TextUnitType.Sp
                )
            ),
            modifier = Modifier.semantics { contentDescription = contentDesc },
        )
    }
}

@Composable
fun HomeButton(
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    val view = LocalView.current
    val buttonBit = 0x4000
    val buttonName = "Home"

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    if (isPressed) {
        DisposableEffect(Unit) {
            Log.d(buttonName, "Pressed")
            if (hapticEnabled) {
                HapticUtils.performButtonPressFeedback(view)
            }
            gamepadState.ButtonsDown = gamepadState.ButtonsDown or buttonBit
            onDispose {
                Log.d(buttonName, "Released")
                if (hapticEnabled) {
                    HapticUtils.performButtonReleaseFeedback(view)
                }
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and buttonBit.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or buttonBit
            }
        }
    }

    OutlinedIconButton(
        modifier = modifier.size(size),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        onClick = { },
        interactionSource = interactionSource,
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = stringResource(R.string.content_desc_button, buttonName),
            modifier = Modifier.size(size / 2),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CaptureButton(
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    val view = LocalView.current
    val buttonBit = 0x8000
    val buttonName = "Capture"

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    if (isPressed) {
        DisposableEffect(Unit) {
            Log.d(buttonName, "Pressed")
            if (hapticEnabled) {
                HapticUtils.performButtonPressFeedback(view)
            }
            gamepadState.ButtonsDown = gamepadState.ButtonsDown or buttonBit
            onDispose {
                Log.d(buttonName, "Released")
                if (hapticEnabled) {
                    HapticUtils.performButtonReleaseFeedback(view)
                }
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and buttonBit.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or buttonBit
            }
        }
    }

    OutlinedIconButton(
        modifier = modifier.size(size),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        onClick = { },
        interactionSource = interactionSource,
    ) {
        Icon(
            imageVector = Icons.Default.RadioButtonUnchecked,
            contentDescription = stringResource(R.string.content_desc_button, "Capture (○)"),
            modifier = Modifier.size(size / 2),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * The central buttons section containing shoulder buttons (L/R bumpers) and menu buttons (View/Menu) for the gamepad.
 */
@Composable
fun CentralButtons(
    modifier: Modifier = Modifier,
    baseDp: Int,
    gamepadState: GamepadReading,
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
    onSettingsClick: () -> Unit = {},
) {
    // Helper function to get config for a component
    fun getConfig(component: ButtonComponent) =
        buttonConfigs[component] ?: ButtonConfig.default(component)

    // Helper function to render a button component with its anchor
    @Composable
    fun RenderButton(component: ButtonComponent, content: @Composable (ButtonConfig) -> Unit) {
        val config = getConfig(component)
        if (config.visible) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = config.anchor.toAlignment()
            ) {
                content(config)
            }
        }
    }

    // Left Shoulder button
    RenderButton(ButtonComponent.LEFT_SHOULDER) { config ->
        ShoulderButton(
            type = ShoulderButtonType.LEFT,
            modifier = Modifier
                .offset(
                    x = (config.offsetX * baseDp).dp,
                    y = (config.offsetY * baseDp).dp
                )
                .alpha(config.opacity),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
            hapticEnabled = config.hapticEnabled,
        )
    }

    // Right Shoulder button
    RenderButton(ButtonComponent.RIGHT_SHOULDER) { config ->
        ShoulderButton(
            type = ShoulderButtonType.RIGHT,
            modifier = Modifier
                .offset(
                    x = (config.offsetX * baseDp).dp,
                    y = (config.offsetY * baseDp).dp
                )
                .alpha(config.opacity),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
            hapticEnabled = config.hapticEnabled,
        )
    }

    // Select button (View)
    RenderButton(ButtonComponent.SELECT_BUTTON) { config ->
        MenuButton(
            type = MenuButtonType.VIEW,
            modifier = Modifier
                .offset(
                    x = (config.offsetX * baseDp).dp,
                    y = (config.offsetY * baseDp).dp
                )
                .alpha(config.opacity),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
            hapticEnabled = config.hapticEnabled,
        )
    }

    // Start button (Menu)
    RenderButton(ButtonComponent.START_BUTTON) { config ->
        MenuButton(
            type = MenuButtonType.MENU,
            modifier = Modifier
                .offset(
                    x = (config.offsetX * baseDp).dp,
                    y = (config.offsetY * baseDp).dp
                )
                .alpha(config.opacity),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
            hapticEnabled = config.hapticEnabled,
        )
    }

    // Settings button
    RenderButton(ButtonComponent.SETTINGS_BUTTON) { config ->
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .offset(
                    x = (config.offsetX * baseDp).dp,
                    y = (config.offsetY * baseDp).dp
                )
                .size((baseDp / 10 * config.scale).dp)
                .alpha(config.opacity)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size((baseDp / 20 * config.scale).dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Home button
    RenderButton(ButtonComponent.HOME_BUTTON) { config ->
        HomeButton(
            modifier = Modifier
                .offset(
                    x = (config.offsetX * baseDp).dp,
                    y = (config.offsetY * baseDp).dp
                )
                .alpha(config.opacity),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
            hapticEnabled = config.hapticEnabled,
        )
    }

    // Capture button
    RenderButton(ButtonComponent.CAPTURE_BUTTON) { config ->
        CaptureButton(
            modifier = Modifier
                .offset(
                    x = (config.offsetX * baseDp).dp,
                    y = (config.offsetY * baseDp).dp
                )
                .alpha(config.opacity),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
            hapticEnabled = config.hapticEnabled,
        )
    }
}

@Preview(showBackground = false)
@Composable
fun CentralButtonsPreview() {
    CentralButtons(
        baseDp = 400,
        gamepadState = GamepadReading(),
        buttonConfigs = io.github.amarthyasg.airstix.data.defaultButtonConfigs,
    )
}
