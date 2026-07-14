package io.github.amarthyasg.airstix.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import kotlin.math.sqrt
import io.github.amarthyasg.VGP_Data_Exchange.GamepadReading
import io.github.amarthyasg.airstix.data.ButtonAnchor
import io.github.amarthyasg.airstix.data.ButtonComponent
import io.github.amarthyasg.airstix.data.ButtonConfig
import io.github.amarthyasg.airstix.ui.screens.GamepadPreview
import io.github.amarthyasg.airstix.data.defaultButtonConfigs

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

/**
 * GamepadCustomizationEditor displays a scrollable card list of all button configurations.
 * This is the primary non-visual editor screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamepadCustomizationEditor(
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
    onConfigChange: (ButtonComponent, ButtonConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ButtonComponent.entries.forEach { component ->
            val config = buttonConfigs[component] ?: ButtonConfig.default(component)

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (component == ButtonComponent.SETTINGS_BUTTON) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                            } else if (component == ButtonComponent.HOME_BUTTON) {
                                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                            } else if (component == ButtonComponent.CAPTURE_BUTTON) {
                                Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = component.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = config.visible,
                            onCheckedChange = { onConfigChange(component, config.copy(visible = it)) }
                        )
                    }

                    if (config.visible) {
                        // Anchor Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Anchor", style = MaterialTheme.typography.bodyMedium)
                            
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                Button(onClick = { expanded = true }) {
                                    Text(config.anchor.displayName)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    ButtonAnchor.entries.forEach { anchor ->
                                        DropdownMenuItem(
                                            text = { Text(anchor.displayName) },
                                            onClick = {
                                                onConfigChange(component, config.copy(anchor = anchor))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Scale Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Scale", style = MaterialTheme.typography.bodyMedium)
                                Text(String.format("%.1fx", config.scale), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = config.scale,
                                valueRange = 0.5f..2.0f,
                                onValueChange = { onConfigChange(component, config.copy(scale = it)) }
                            )
                        }

                        // Opacity Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Opacity", style = MaterialTheme.typography.bodyMedium)
                                Text("${(config.opacity * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = config.opacity,
                                valueRange = 0.2f..1.0f,
                                onValueChange = { onConfigChange(component, config.copy(opacity = it)) }
                            )
                        }

                        // Offset X
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Offset X", style = MaterialTheme.typography.bodyMedium)
                                Text(String.format("%.2f", config.offsetX), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = config.offsetX,
                                valueRange = -1.5f..1.5f,
                                onValueChange = { onConfigChange(component, config.copy(offsetX = it)) }
                            )
                        }

                        // Offset Y
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Offset Y", style = MaterialTheme.typography.bodyMedium)
                                Text(String.format("%.2f", config.offsetY), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = config.offsetY,
                                valueRange = -1.5f..1.5f,
                                onValueChange = { onConfigChange(component, config.copy(offsetY = it)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * FullScreenGamepadCustomizer displays a full-screen dotted grid overlay where buttons are positioned
 * exactly as they appear in-game, and can be selected, dragged, and customized.
 */
@Composable
fun FullScreenGamepadCustomizer(
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
    faceButtonsGrouped: Boolean = true,
    dpadGrouped: Boolean = true,
    onConfigChange: (ButtonComponent, ButtonConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedComponent by remember { mutableStateOf<ButtonComponent?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density
    var lastTapTime by remember { mutableStateOf(0L) }
    var tappedComponentOnce by remember { mutableStateOf<ButtonComponent?>(null) }
    val currentConfigsState = rememberUpdatedState(buttonConfigs)

    val buttonCenters = remember { mutableStateMapOf<ButtonComponent, Offset>() }
    val buttonSizes = remember { mutableStateMapOf<ButtonComponent, IntSize>() }
    // Track the root-space origin of BoxWithConstraints so we can convert touch coords
    var canvasRootOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .onGloballyPositioned { coordinates ->
                    canvasRootOffset = coordinates.positionInRoot()
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                            val downChange = downEvent.changes.firstOrNull() ?: continue
                            if (!downChange.pressed) continue

                            val touchPos = downChange.position
                            // Convert local touch position to root coordinates to match buttonCenters
                            val touchPosRoot = touchPos + canvasRootOffset
                            
                            // Manual hit-test across all button components using globally positioned coordinates
                            var touchedComponent: ButtonComponent? = null
                            var closestDist = Float.MAX_VALUE
                            
                            val filteredCenters = buttonCenters.filter { (component, _) ->
                                val faceOk = if (faceButtonsGrouped) {
                                    component != ButtonComponent.FACE_BUTTON_A &&
                                    component != ButtonComponent.FACE_BUTTON_B &&
                                    component != ButtonComponent.FACE_BUTTON_X &&
                                    component != ButtonComponent.FACE_BUTTON_Y
                                } else {
                                    component != ButtonComponent.FACE_BUTTONS
                                }
                                val dpadOk = if (dpadGrouped) {
                                    component != ButtonComponent.DPAD_UP &&
                                    component != ButtonComponent.DPAD_DOWN &&
                                    component != ButtonComponent.DPAD_LEFT &&
                                    component != ButtonComponent.DPAD_RIGHT
                                } else {
                                    component != ButtonComponent.DPAD
                                }
                                faceOk && dpadOk
                            }
                            filteredCenters.forEach { (component, center) ->
                                val size = buttonSizes[component] ?: IntSize.Zero
                                val radius = (maxOf(size.width, size.height) / 2f) + (24f * density)
                                val dx = touchPosRoot.x - center.x
                                val dy = touchPosRoot.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist <= radius && dist < closestDist) {
                                    touchedComponent = component
                                    closestDist = dist
                                }
                            }

                            if (touchedComponent != null) {
                                val component = touchedComponent
                                
                                // Check double-click
                                val currentTime = System.currentTimeMillis()
                                val timeDiff = currentTime - lastTapTime
                                if (timeDiff < 300L && tappedComponentOnce == component) {
                                    selectedComponent = component
                                    lastTapTime = 0L
                                    tappedComponentOnce = null
                                } else {
                                    lastTapTime = currentTime
                                    tappedComponentOnce = component
                                }

                                // Consume down event so children do not receive it
                                downChange.consume()

                                // Track drag relative to static canvas coordinates
                                var lastPosition = touchPos
                                
                                while (true) {
                                    val moveEvent = awaitPointerEvent(PointerEventPass.Initial)
                                    val moveChange = moveEvent.changes.firstOrNull { it.id == downChange.id } ?: break
                                    
                                    if (moveChange.pressed) {
                                        val currentPosition = moveChange.position
                                        val deltaX = currentPosition.x - lastPosition.x
                                        val deltaY = currentPosition.y - lastPosition.y
                                        
                                        if (deltaX != 0f || deltaY != 0f) {
                                            moveChange.consume()
                                            val canvasHeightPx = size.height.toFloat()
                                            if (canvasHeightPx > 0) {
                                                val currentConfig = currentConfigsState.value[component] ?: ButtonConfig.default(component)
                                                val newOffsetX = currentConfig.offsetX + deltaX / canvasHeightPx
                                                val newOffsetY = currentConfig.offsetY + deltaY / canvasHeightPx
                                                val clampedX = newOffsetX.coerceIn(-1.5f, 1.5f)
                                                val clampedY = newOffsetY.coerceIn(-1.5f, 1.5f)
                                                
                                                onConfigChange(
                                                    component,
                                                    currentConfig.copy(offsetX = clampedX, offsetY = clampedY)
                                                )
                                            }
                                        }
                                        lastPosition = currentPosition
                                    } else {
                                        moveChange.consume()
                                        break
                                    }
                                }
                            } else {
                                // Touch on background clears selection
                                selectedComponent = null
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight
            val canvasHeightPx = constraints.maxHeight.toFloat()

            // 1. Draw Dotted Grid
            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            val spacingDp = 20.dp
            val radiusDp = 1.5.dp
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val spacing = spacingDp.toPx()
                        val radius = radiusDp.toPx()
                        val gridPath = Path().apply {
                            for (x in 0..size.width.toInt() step spacing.toInt()) {
                                for (y in 0..size.height.toInt() step spacing.toInt()) {
                                    addOval(
                                        Rect(
                                            center = Offset(x.toFloat(), y.toFloat()),
                                            radius = radius
                                        )
                                    )
                                }
                            }
                        }
                        onDrawBehind {
                            drawPath(path = gridPath, color = gridColor)
                        }
                    }
            )

            // 2. Render actual virtual controls in their configured positions
            val filteredComponents = ButtonComponent.entries.filter { component ->
                val faceOk = if (faceButtonsGrouped) {
                    component != ButtonComponent.FACE_BUTTON_A &&
                    component != ButtonComponent.FACE_BUTTON_B &&
                    component != ButtonComponent.FACE_BUTTON_X &&
                    component != ButtonComponent.FACE_BUTTON_Y
                } else {
                    component != ButtonComponent.FACE_BUTTONS
                }
                val dpadOk = if (dpadGrouped) {
                    component != ButtonComponent.DPAD_UP &&
                    component != ButtonComponent.DPAD_DOWN &&
                    component != ButtonComponent.DPAD_LEFT &&
                    component != ButtonComponent.DPAD_RIGHT
                } else {
                    component != ButtonComponent.DPAD &&
                    component != ButtonComponent.DPAD_UP &&
                    component != ButtonComponent.DPAD_DOWN &&
                    component != ButtonComponent.DPAD_LEFT &&
                    component != ButtonComponent.DPAD_RIGHT
                }
                faceOk && dpadOk
            }
            filteredComponents.forEach { component ->
                val config = buttonConfigs[component] ?: ButtonConfig.default(component)
                val isSelected = selectedComponent == component

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = config.anchor.toAlignment()
                ) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (config.offsetX * canvasHeight.value).dp,
                                y = (config.offsetY * canvasHeight.value).dp
                            )
                            .alpha(if (config.visible) config.opacity else 0.3f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .onGloballyPositioned { coordinates ->
                                val parentPosition = coordinates.positionInRoot()
                                val componentSize = coordinates.size
                                buttonSizes[component] = componentSize
                                buttonCenters[component] = Offset(
                                    x = parentPosition.x + componentSize.width / 2f,
                                    y = parentPosition.y + componentSize.height / 2f
                                )
                            },
                        contentAlignment = config.anchor.toAlignment()
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                        val mockGamepadState = remember { GamepadReading() }
                        when (component) {
                            ButtonComponent.LEFT_ANALOG_STICK -> {
                                AnalogStick(
                                    outerCircleWidth = (canvasHeight.value / 8 * config.scale).dp,
                                    innerCircleRadius = (canvasHeight.value / 12 * config.scale).dp,
                                    gamepadState = mockGamepadState,
                                    type = AnalogStickType.LEFT
                                )
                            }
                            ButtonComponent.RIGHT_ANALOG_STICK -> {
                                AnalogStick(
                                    outerCircleWidth = (canvasHeight.value / 8 * config.scale).dp,
                                    innerCircleRadius = (canvasHeight.value / 12 * config.scale).dp,
                                    gamepadState = mockGamepadState,
                                    type = AnalogStickType.RIGHT
                                )
                            }
                            ButtonComponent.DPAD -> {
                                Dpad(
                                    size = (0.45f * canvasHeight.value * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.FACE_BUTTONS -> {
                                FaceButtons(
                                    size = (0.45f * canvasHeight.value * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.FACE_BUTTON_A -> {
                                FaceButton(
                                    type = FaceButtonType.A,
                                    size = (0.18f * canvasHeight.value * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.FACE_BUTTON_B -> {
                                FaceButton(
                                    type = FaceButtonType.B,
                                    size = (0.18f * canvasHeight.value * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.FACE_BUTTON_X -> {
                                FaceButton(
                                    type = FaceButtonType.X,
                                    size = (0.18f * canvasHeight.value * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.FACE_BUTTON_Y -> {
                                FaceButton(
                                    type = FaceButtonType.Y,
                                    size = (0.18f * canvasHeight.value * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.LEFT_TRIGGER -> {
                                Trigger(
                                    type = TriggerType.LEFT,
                                    size = (canvasHeight.value / 6 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.RIGHT_TRIGGER -> {
                                Trigger(
                                    type = TriggerType.RIGHT,
                                    size = (canvasHeight.value / 6 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.LEFT_SHOULDER -> {
                                ShoulderButton(
                                    type = ShoulderButtonType.LEFT,
                                    size = (canvasHeight.value / 8 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.RIGHT_SHOULDER -> {
                                ShoulderButton(
                                    type = ShoulderButtonType.RIGHT,
                                    size = (canvasHeight.value / 8 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.SELECT_BUTTON -> {
                                MenuButton(
                                    type = MenuButtonType.VIEW,
                                    size = (canvasHeight.value / 8 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.START_BUTTON -> {
                                MenuButton(
                                    type = MenuButtonType.MENU,
                                    size = (canvasHeight.value / 8 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.SETTINGS_BUTTON -> {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    modifier = Modifier.size((canvasHeight.value / 12 * config.scale).dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            ButtonComponent.HOME_BUTTON -> {
                                HomeButton(
                                    size = (canvasHeight.value / 8 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.CAPTURE_BUTTON -> {
                                CaptureButton(
                                    size = (canvasHeight.value / 8 * config.scale).dp,
                                    gamepadState = mockGamepadState
                                )
                            }
                            ButtonComponent.DPAD_UP,
                            ButtonComponent.DPAD_DOWN,
                            ButtonComponent.DPAD_LEFT,
                            ButtonComponent.DPAD_RIGHT -> {
                                // Rendered separately via UngroupedDpadButtons when ungrouped
                            }
                        }
                        } // close Box(modifier = Modifier.padding(8.dp))
                    }
                }
            }

            // Ungrouped D-Pad: render inside the group container so positions match the grouped layout
            if (!dpadGrouped) {
                val groupConfig = buttonConfigs[ButtonComponent.DPAD]
                    ?: ButtonConfig.default(ButtonComponent.DPAD)
                val ungroupedMockState = remember { GamepadReading() }
                val primaryColor = MaterialTheme.colorScheme.primary
                val primaryFaintColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                val primaryBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                val surfaceFaintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = groupConfig.anchor.toAlignment(),
                ) {
                    UngroupedDpadButtons(
                        layoutSizeDp = canvasHeight.value,
                        groupConfig = groupConfig,
                        buttonConfigs = buttonConfigs,
                        gamepadState = ungroupedMockState,
                        buttonModifier = { component, config ->
                            val isSelected = selectedComponent == component
                            Modifier
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) primaryColor else primaryFaintColor,
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .background(
                                    color = if (isSelected) primaryBgColor else surfaceFaintColor,
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .onGloballyPositioned { coordinates ->
                                    val position = coordinates.positionInRoot()
                                    val componentSize = coordinates.size
                                    buttonSizes[component] = componentSize
                                    buttonCenters[component] = Offset(
                                        x = position.x + componentSize.width / 2f,
                                        y = position.y + componentSize.height / 2f,
                                    )
                                }
                        },
                    )
                }
            }
        }

        // 3. Floating Property Panel (centered at the bottom to stay out of the way)
        if (selectedComponent != null) {
            val component = selectedComponent!!
            val config = buttonConfigs[component] ?: ButtonConfig.default(component)

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .width(360.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = component.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Visible", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 4.dp))
                            Switch(
                                checked = config.visible,
                                onCheckedChange = { onConfigChange(component, config.copy(visible = it)) },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }

                    if (config.visible) {
                        // Anchor Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Anchor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { expanded = true },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(config.anchor.displayName, fontSize = 12.sp)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    ButtonAnchor.entries.forEach { anchor ->
                                        DropdownMenuItem(
                                            text = { Text(anchor.displayName) },
                                            onClick = {
                                                onConfigChange(component, config.copy(anchor = anchor))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Opacity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Opacity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(90.dp))
                            Slider(
                                value = config.opacity,
                                valueRange = 0.2f..1.0f,
                                onValueChange = { onConfigChange(component, config.copy(opacity = it)) },
                                modifier = Modifier.weight(1f)
                            )
                            Text("${(config.opacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                        }

                        // Scale (Size)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Size (Scale)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(90.dp))
                            Slider(
                                value = config.scale,
                                valueRange = 0.5f..2.0f,
                                onValueChange = { onConfigChange(component, config.copy(scale = it)) },
                                modifier = Modifier.weight(1f)
                            )
                            Text(String.format("%.1fx", config.scale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val resetInteractionSource = remember { MutableInteractionSource() }
                        val resetIsPressed by resetInteractionSource.collectIsPressedAsState()
                        val resetScale by animateFloatAsState(targetValue = if (resetIsPressed) 0.96f else 1.0f, label = "resetScale")

                        TextButton(
                            onClick = {
                                val default = defaultButtonConfigs[component] ?: ButtonConfig.default(component)
                                onConfigChange(component, config.copy(offsetX = default.offsetX, offsetY = default.offsetY))
                            },
                            interactionSource = resetInteractionSource,
                            modifier = Modifier.scale(resetScale)
                        ) {
                            Text("Reset Pos", fontSize = 12.sp)
                        }

                        val closeInteractionSource = remember { MutableInteractionSource() }
                        val closeIsPressed by closeInteractionSource.collectIsPressedAsState()
                        val closeScale by animateFloatAsState(targetValue = if (closeIsPressed) 0.96f else 1.0f, label = "closeScale")

                        TextButton(
                            onClick = { selectedComponent = null },
                            interactionSource = closeInteractionSource,
                            modifier = Modifier.scale(closeScale)
                        ) {
                            Text("Close Panel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4. Top-center Close and Preview buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showPreview = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "Preview layout",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Customizer",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showPreview) {
            GamepadPreview(
                buttonConfigs = buttonConfigs,
                faceButtonsGrouped = faceButtonsGrouped,
                dpadGrouped = dpadGrouped,
                onDismiss = { showPreview = false }
            )
        }
    }
}

