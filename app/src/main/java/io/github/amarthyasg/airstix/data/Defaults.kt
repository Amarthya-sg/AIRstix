package io.github.amarthyasg.airstix.data

val defaultColorScheme = ColorScheme.SYSTEM
val defaultBaseColor = BaseColor.BLUE
const val defaultPollingDelay = 80 // in milliseconds
const val defaultHapticFeedbackEnabled = false // vibrations
const val defaultSaveConnectionCredentials = false
const val defaultFullScreenEnabled = true
val defaultHapticIntensity = HapticIntensity.MEDIUM

// Default button configurations with offsets matching the original layout
// Note: Offset values are multipliers of baseDp (heightDp in landscape)
// These will be applied as: offsetX * baseDp or offsetY * baseDp at runtime
val defaultButtonConfigs = mapOf(
    ButtonComponent.LEFT_ANALOG_STICK to ButtonConfig(
        component = ButtonComponent.LEFT_ANALOG_STICK,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_LEFT
    ),
    ButtonComponent.RIGHT_ANALOG_STICK to ButtonConfig(
        component = ButtonComponent.RIGHT_ANALOG_STICK,
        visible = true,
        scale = 1.0f,
        offsetX = -0.25f, // -baseDp/4
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_RIGHT
    ),
    ButtonComponent.DPAD to ButtonConfig(
        component = ButtonComponent.DPAD,
        visible = true,
        scale = 1.0f,
        offsetX = 0.333f, // baseDp/3
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.DPAD_UP to ButtonConfig(
        component = ButtonComponent.DPAD_UP,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.DPAD_DOWN to ButtonConfig(
        component = ButtonComponent.DPAD_DOWN,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.DPAD_LEFT to ButtonConfig(
        component = ButtonComponent.DPAD_LEFT,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.DPAD_RIGHT to ButtonConfig(
        component = ButtonComponent.DPAD_RIGHT,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.FACE_BUTTONS to ButtonConfig(
        component = ButtonComponent.FACE_BUTTONS,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_RIGHT
    ),
    ButtonComponent.FACE_BUTTON_A to ButtonConfig(
        component = ButtonComponent.FACE_BUTTON_A,
        visible = true,
        scale = 1.0f,
        offsetX = -0.135f,
        offsetY = 0.27f,
        anchor = ButtonAnchor.TOP_RIGHT
    ),
    ButtonComponent.FACE_BUTTON_B to ButtonConfig(
        component = ButtonComponent.FACE_BUTTON_B,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0.135f,
        anchor = ButtonAnchor.TOP_RIGHT
    ),
    ButtonComponent.FACE_BUTTON_X to ButtonConfig(
        component = ButtonComponent.FACE_BUTTON_X,
        visible = true,
        scale = 1.0f,
        offsetX = -0.135f,
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_RIGHT
    ),
    ButtonComponent.FACE_BUTTON_Y to ButtonConfig(
        component = ButtonComponent.FACE_BUTTON_Y,
        visible = true,
        scale = 1.0f,
        offsetX = -0.27f,
        offsetY = 0.135f,
        anchor = ButtonAnchor.TOP_RIGHT
    ),
    ButtonComponent.LEFT_TRIGGER to ButtonConfig(
        component = ButtonComponent.LEFT_TRIGGER,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.RIGHT_TRIGGER to ButtonConfig(
        component = ButtonComponent.RIGHT_TRIGGER,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_RIGHT
    ),
    ButtonComponent.LEFT_SHOULDER to ButtonConfig(
        component = ButtonComponent.LEFT_SHOULDER,
        visible = true,
        scale = 1.0f,
        offsetX = -0.25f, // -baseDp/4
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.RIGHT_SHOULDER to ButtonConfig(
        component = ButtonComponent.RIGHT_SHOULDER,
        visible = true,
        scale = 1.0f,
        offsetX = 0.25f, // baseDp/4
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.SELECT_BUTTON to ButtonConfig(
        component = ButtonComponent.SELECT_BUTTON,
        visible = true,
        scale = 1.0f,
        offsetX = -0.25f, // -baseDp/4
        offsetY = 0.25f, // baseDp/4
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.START_BUTTON to ButtonConfig(
        component = ButtonComponent.START_BUTTON,
        visible = true,
        scale = 1.0f,
        offsetX = 0.25f, // baseDp/4
        offsetY = 0.25f, // baseDp/4
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.SETTINGS_BUTTON to ButtonConfig(
        component = ButtonComponent.SETTINGS_BUTTON,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0.35f, // just below the center cluster
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.HOME_BUTTON to ButtonConfig(
        component = ButtonComponent.HOME_BUTTON,
        visible = true,
        scale = 1.0f,
        offsetX = 0.1f,
        offsetY = 0.25f,
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.CAPTURE_BUTTON to ButtonConfig(
        component = ButtonComponent.CAPTURE_BUTTON,
        visible = true,
        scale = 1.0f,
        offsetX = -0.1f,
        offsetY = 0.25f,
        anchor = ButtonAnchor.TOP_CENTER
    )
)
