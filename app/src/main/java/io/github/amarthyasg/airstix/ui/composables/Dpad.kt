package io.github.amarthyasg.airstix.ui.composables

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.amarthyasg.VGP_Data_Exchange.GameButtons
import io.github.amarthyasg.VGP_Data_Exchange.GamepadReading
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.ui.theme.darken
import io.github.amarthyasg.airstix.ui.utils.HapticUtils

import io.github.amarthyasg.airstix.data.ButtonComponent
import io.github.amarthyasg.airstix.data.ButtonConfig

/** Fraction of layout height used by the grouped D-pad container. */
const val DpadGroupSizeRatio = 0.45f

/** Fraction of layout height used by each D-pad button (slightly smaller to prevent overlap). */
const val DpadButtonSizeRatio = 0.17f

private data class DpadButtonSlot(
    val type: DpadButtonType,
    val component: ButtonComponent,
    val alignment: Alignment,
)

private val dpadButtonSlots = listOf(
    DpadButtonSlot(DpadButtonType.UP, ButtonComponent.DPAD_UP, Alignment.TopCenter),
    DpadButtonSlot(DpadButtonType.DOWN, ButtonComponent.DPAD_DOWN, Alignment.BottomCenter),
    DpadButtonSlot(DpadButtonType.LEFT, ButtonComponent.DPAD_LEFT, Alignment.CenterStart),
    DpadButtonSlot(DpadButtonType.RIGHT, ButtonComponent.DPAD_RIGHT, Alignment.CenterEnd),
)

private val dpadIndividualComponents = dpadButtonSlots.map { it.component }

/** Individual D-pad buttons use offsets relative to their slot inside the group container. */
fun dpadIndividualConfigsFromGroup(groupConfig: ButtonConfig): Map<ButtonComponent, ButtonConfig> {
    return dpadIndividualComponents.associateWith { component ->
        ButtonConfig(
            component = component,
            visible = groupConfig.visible,
            scale = groupConfig.scale,
            opacity = groupConfig.opacity,
            offsetX = 0f,
            offsetY = 0f,
            anchor = groupConfig.anchor,
        )
    }
}

fun sanitizeUngroupedDpadConfigs(
    configs: Map<ButtonComponent, ButtonConfig>,
): Map<ButtonComponent, ButtonConfig> {
    val groupConfig = configs[ButtonComponent.DPAD] ?: ButtonConfig.default(ButtonComponent.DPAD)
    return configs + dpadIndividualConfigsFromGroup(groupConfig).mapValues { (component, defaults) ->
        val saved = configs[component]
        if (saved != null) {
            defaults.copy(
                scale = saved.scale,
                opacity = saved.opacity,
                visible = saved.visible,
                offsetX = saved.offsetX,
                offsetY = saved.offsetY,
            )
        } else {
            defaults
        }
    }
}

/**
 * Renders ungrouped D-pad buttons inside the same container frame as [Dpad], using [groupConfig]
 * for placement and per-button configs for relative offsets within the diamond.
 */
@Composable
fun UngroupedDpadButtons(
    layoutSizeDp: Float,
    groupConfig: ButtonConfig,
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
    gamepadState: GamepadReading,
    modifier: Modifier = Modifier,
    buttonModifier: (ButtonComponent, ButtonConfig) -> Modifier = { _, _ -> Modifier },
) {
    val groupSize = layoutSizeDp * DpadGroupSizeRatio * groupConfig.scale
    Box(
        modifier = modifier
            .offset(
                x = (groupConfig.offsetX * layoutSizeDp).dp,
                y = (groupConfig.offsetY * layoutSizeDp).dp,
            )
            .size(groupSize.dp),
        contentAlignment = Alignment.Center,
    ) {
        dpadButtonSlots.forEach { slot ->
            val config = buttonConfigs[slot.component] ?: ButtonConfig.default(slot.component)
            if (config.visible) {
                val buttonSize = layoutSizeDp * DpadButtonSizeRatio * config.scale
                Box(
                    modifier = Modifier
                        .align(slot.alignment)
                        .offset(
                            x = (config.offsetX * layoutSizeDp).dp,
                            y = (config.offsetY * layoutSizeDp).dp,
                        )
                        .alpha(config.opacity)
                        .then(buttonModifier(slot.component, config)),
                ) {
                    DpadButton(
                        type = slot.type,
                        size = buttonSize.dp,
                        gamepadState = gamepadState,
                        hapticEnabled = config.hapticEnabled,
                    )
                }
            }
        }
    }
}

enum class DpadButtonType {
    UP, DOWN, LEFT, RIGHT
}

@Composable
fun DpadButton(
    type: DpadButtonType,
    modifier: Modifier = Modifier,
    foregroundColour: Color = Color.Unspecified,
    backgroundColour: Color = Color.Unspecified,
    size: Dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    val view = LocalView.current
    val rotation = when (type) {
        DpadButtonType.UP -> -90f
        DpadButtonType.DOWN -> 90f
        DpadButtonType.LEFT -> 180f
        DpadButtonType.RIGHT -> 0f
    }
    val gameButton = when (type) {
        DpadButtonType.UP -> GameButtons.DPadUp
        DpadButtonType.DOWN -> GameButtons.DPadDown
        DpadButtonType.LEFT -> GameButtons.DPadLeft
        DpadButtonType.RIGHT -> GameButtons.DPadRight
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val resolvedBackground = if (backgroundColour != Color.Unspecified) {
        backgroundColour
    } else {
        MaterialTheme.colorScheme.surface
    }

    val resolvedForeground = if (foregroundColour != Color.Unspecified) {
        foregroundColour
    } else {
        if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    }

    val resolvedBorderColor = if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    // See https://stackoverflow.com/a/69157877/8659747
    if (isPressed) {
        DisposableEffect(Unit) {
            Log.d("DPadButton ${type.name}", "Pressed")
            if (hapticEnabled) {
                HapticUtils.performButtonPressFeedback(view)
            }
            gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
            onDispose {
                Log.d("DPadButton ${type.name}", "Released")
                if (hapticEnabled) {
                    HapticUtils.performButtonReleaseFeedback(view)
                }
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }
    OutlinedIconButton(
        modifier = modifier
            .size(size)
            .padding(0.dp),
        onClick = {},
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = resolvedBackground,
        ),
        border = BorderStroke(2.dp, resolvedBorderColor),
        interactionSource = interactionSource,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play_arrow),
            contentDescription = stringResource(R.string.content_desc_dpad_button, type.name),
            modifier = Modifier
                .rotate(rotation)
                .size(size),
            tint = resolvedForeground
        )
    }
}

/**
 * A directional pad with up, down, left, and right buttons.
 */
@Composable
fun Dpad(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    val buttonSize = size * 0.36f
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        DpadButton(
            type = DpadButtonType.UP,
            modifier = Modifier.align(Alignment.TopCenter),
            size = buttonSize,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
        DpadButton(
            type = DpadButtonType.DOWN,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = buttonSize,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
        DpadButton(
            type = DpadButtonType.LEFT,
            modifier = Modifier.align(Alignment.CenterStart),
            size = buttonSize,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
        DpadButton(
            type = DpadButtonType.RIGHT,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = buttonSize,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
    }
}

@Preview(showBackground = false)
@Composable
fun DpadPreview() {
    Dpad(
        gamepadState = GamepadReading(),
    )
}
