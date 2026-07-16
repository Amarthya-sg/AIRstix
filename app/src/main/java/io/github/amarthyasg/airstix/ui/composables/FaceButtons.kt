package io.github.amarthyasg.airstix.ui.composables

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.amarthyasg.VGP_Data_Exchange.GameButtons
import io.github.amarthyasg.VGP_Data_Exchange.GamepadReading
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.ui.theme.darken
import io.github.amarthyasg.airstix.ui.theme.faceButtonTextStyle
import io.github.amarthyasg.airstix.ui.theme.lighten
import io.github.amarthyasg.airstix.ui.utils.HapticUtils


enum class FaceButtonType {
    A, B, X, Y
}

private val faceButtonColourMap = mapOf(
    FaceButtonType.A to Color(0xFF00FF00),
    FaceButtonType.B to Color(0xFFFF0000),
    FaceButtonType.X to Color(0xFF0055FF),
    FaceButtonType.Y to Color(0xFFFFFF00),
)

@Composable
fun FaceButton(
    type: FaceButtonType,
    modifier: Modifier = Modifier,
    foregroundColour: Color = lighten(faceButtonColourMap[type]!!, 0.2f),
    backgroundColour: Color = darken(faceButtonColourMap[type]!!, 0.8f),
    size: Dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    val view = LocalView.current
    val gameButton = when (type) {
        FaceButtonType.A -> GameButtons.A
        FaceButtonType.B -> GameButtons.B
        FaceButtonType.X -> GameButtons.X
        FaceButtonType.Y -> GameButtons.Y
    }
    val label = when (type) {
        FaceButtonType.A -> stringResource(R.string.button_a)
        FaceButtonType.B -> stringResource(R.string.button_b)
        FaceButtonType.X -> stringResource(R.string.button_x)
        FaceButtonType.Y -> stringResource(R.string.button_y)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // See https://stackoverflow.com/a/69157877/8659747
    if (isPressed) {
        DisposableEffect(Unit) {
            Log.d("FaceButton ${type.name}", "Pressed")
            if (hapticEnabled) {
                HapticUtils.performButtonPressFeedback(view)
            }
            gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
            onDispose {
                Log.d("FaceButton ${type.name}", "Released")
                if (hapticEnabled) {
                    HapticUtils.performButtonReleaseFeedback(view)
                }
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }
    OutlinedButton(
        modifier = modifier
            .size(size)
            .padding(0.dp),
        onClick = {},
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColour,
        ),
        interactionSource = interactionSource,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(
            text = label,
            color = foregroundColour,
            textAlign = TextAlign.Center,
            style = faceButtonTextStyle(size),
        )
    }
}

/**
 * The A, B, X, Y buttons on a gamepad, also known as the face buttons.
 */
@Composable
fun FaceButtons(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    gamepadState: GamepadReading,
    hapticEnabled: Boolean = true,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        FaceButton(
            type = FaceButtonType.A,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = 2 * size / 5,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
        FaceButton(
            type = FaceButtonType.B,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = 2 * size / 5,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
        FaceButton(
            type = FaceButtonType.X,
            modifier = Modifier.align(Alignment.TopCenter),
            size = 2 * size / 5,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
        FaceButton(
            type = FaceButtonType.Y,
            modifier = Modifier.align(Alignment.CenterStart),
            size = 2 * size / 5,
            gamepadState = gamepadState,
            hapticEnabled = hapticEnabled,
        )
    }
}

@Preview(showBackground = false)
@Composable
fun FaceButtonsPreview() {
    PreviewBase {
        FaceButtons(
            gamepadState = GamepadReading(),
        )
    }
}
