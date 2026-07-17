package io.github.amarthyasg.airstix.ui.screens

import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.data.PreviewHeightDp
import io.github.amarthyasg.airstix.data.PreviewWidthDp
import io.github.amarthyasg.airstix.data.SettingsRepository
import io.github.amarthyasg.airstix.data.defaultFullScreenEnabled
import io.github.amarthyasg.airstix.data.defaultHapticFeedbackEnabled
import io.github.amarthyasg.airstix.data.defaultHapticIntensity
import io.github.amarthyasg.airstix.data.HapticIntensity
import io.github.amarthyasg.airstix.data.defaultPollingDelay
import io.github.amarthyasg.airstix.data.defaultSaveConnectionCredentials
import io.github.amarthyasg.airstix.data.MinimalistPalette
import io.github.amarthyasg.airstix.data.defaultMinimalistPalette
import io.github.amarthyasg.airstix.ui.composables.ListItemPicker
import io.github.amarthyasg.airstix.ui.composables.SpinBox
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import io.github.amarthyasg.airstix.ui.composables.HUDViewfinder
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import io.github.amarthyasg.airstix.ui.utils.HapticUtils
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import io.github.amarthyasg.airstix.ui.theme.contrasting
import androidx.compose.foundation.background
import androidx.compose.material3.Surface

private const val logTag = "SettingsScreen"

@Parcelize
private data class SettingsChanges(
    val minimalistPalette: MinimalistPalette? = null,
    val pollingDelay: Int? = null,
    val hapticFeedbackEnabled: Boolean? = null,
    val hapticIntensity: HapticIntensity? = null,
    val saveConnectionCredentials: Boolean? = null,
    val fullScreenEnabled: Boolean? = null
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGamepadCustomization: () -> Unit,
    settingsRepository: SettingsRepository
) {
    var settingsChanges by rememberSaveable { mutableStateOf(SettingsChanges()) }

    Scaffold { paddingValues ->
        val pollingDelay by settingsRepository.pollingDelay.collectAsState(initial = defaultPollingDelay)
        val hapticEnabled by settingsRepository.hapticFeedbackEnabled.collectAsState(initial = defaultHapticFeedbackEnabled)
        val hapticIntensity by settingsRepository.hapticIntensity.collectAsState(initial = defaultHapticIntensity)
        val saveCredentials by settingsRepository.saveConnectionCredentials.collectAsState(initial = defaultSaveConnectionCredentials)
        val fullScreenEnabled by settingsRepository.fullScreenEnabled.collectAsState(initial = defaultFullScreenEnabled)
        val minimalistPalette by settingsRepository.minimalistPalette.collectAsState(initial = defaultMinimalistPalette)

        Box(modifier = Modifier.fillMaxSize()) {
            // Viewfinder Corner Brackets
            HUDViewfinder(modifier = Modifier.padding(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fixed title at the top
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Scrollable settings content
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: DISPLAY
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "01 · DISPLAY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        val stagedPalette = settingsChanges.minimalistPalette ?: minimalistPalette
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_theme_color),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = stringResource(stagedPalette.nameRes),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = stagedPalette.accent
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            val chunked = MinimalistPalette.entries.chunked(5)
                            chunked.forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    rowItems.forEach { item ->
                                        PaletteCircle(
                                            palette = item,
                                            isSelected = stagedPalette == item,
                                            onClick = {
                                                settingsChanges = settingsChanges.copy(minimalistPalette = item)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                    }

                    // Right Column: BEHAVIOR
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "02 · BEHAVIOR",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        // 1. Polling Rate Interval
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_polling_interval),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace
                                )

                                val toolTipState = rememberTooltipState()
                                val scope = rememberCoroutineScope()
                                TooltipBox(
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Start
                                    ),
                                    tooltip = {
                                        PlainTooltip(shadowElevation = 10.dp) {
                                            Text(
                                                stringResource(R.string.settings_polling_interval_desc),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    },
                                    state = toolTipState
                                ) {
                                    IconButton(
                                        onClick = { scope.launch { toolTipState.show() } },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_info),
                                            contentDescription = stringResource(R.string.settings_polling_interval_info),
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            val currentDelay = settingsChanges.pollingDelay ?: pollingDelay
                            var textValue by remember(currentDelay) { mutableStateOf(currentDelay.toString()) }
                            var isError by remember { mutableStateOf(false) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // Decrement Button (-)
                                IconButton(
                                    onClick = {
                                        val currentVal = textValue.toIntOrNull() ?: defaultPollingDelay
                                        val newVal = (currentVal - 5).coerceIn(10, 500)
                                        textValue = newVal.toString()
                                        isError = false
                                        settingsChanges = settingsChanges.copy(pollingDelay = newVal)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                                        contentDescription = stringResource(R.string.content_desc_decrease)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Editable Text Field
                                BasicTextField(
                                    value = textValue,
                                    onValueChange = { newValue ->
                                        val filtered = newValue.filter { it.isDigit() }
                                        if (filtered.length <= 3) {
                                            textValue = filtered
                                            val intVal = filtered.toIntOrNull()
                                            if (intVal != null && intVal in 10..500) {
                                                isError = false
                                                settingsChanges = settingsChanges.copy(pollingDelay = intVal)
                                            } else {
                                                isError = true
                                            }
                                        }
                                    },
                                    textStyle = TextStyle(
                                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isError) MaterialTheme.colorScheme.error 
                                                        else MaterialTheme.colorScheme.outline
                                            ),
                                            color = Color.Transparent,
                                            modifier = Modifier.width(70.dp).height(36.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                innerTextField()
                                            }
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // Increment Button (+)
                                IconButton(
                                    onClick = {
                                        val currentVal = textValue.toIntOrNull() ?: defaultPollingDelay
                                        val newVal = (currentVal + 5).coerceIn(10, 500)
                                        textValue = newVal.toString()
                                        isError = false
                                        settingsChanges = settingsChanges.copy(pollingDelay = newVal)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_keyboard_arrow_up),
                                        contentDescription = stringResource(R.string.content_desc_increase)
                                    )
                                }
                            }
                        }

                        // 2. Remember IP Address and Port
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.settings_save_connection_credentials),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                            Switch(
                                checked = settingsChanges.saveConnectionCredentials ?: saveCredentials,
                                onCheckedChange = {
                                    settingsChanges = settingsChanges.copy(saveConnectionCredentials = it)
                                }
                            )
                        }

                        // 2. Haptic Feedback (Vibrations)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.settings_haptic_feedback),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                            Switch(
                                checked = settingsChanges.hapticFeedbackEnabled ?: hapticEnabled,
                                onCheckedChange = {
                                    settingsChanges = settingsChanges.copy(hapticFeedbackEnabled = it)
                                }
                            )
                        }

                        // 3. Haptic Intensity & TEST Row
                        val isHapticActive = settingsChanges.hapticFeedbackEnabled ?: hapticEnabled
                        if (isHapticActive) {
                            val currentIntensity = settingsChanges.hapticIntensity ?: hapticIntensity
                            val view = LocalView.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ListItemPicker(
                                        list = HapticIntensity.entries.asIterable(),
                                        selectedItem = currentIntensity,
                                        label = stringResource(R.string.settings_haptic_intensity),
                                        formattedDisplay = { item ->
                                            Text(text = stringResource(item.nameRes), fontFamily = FontFamily.Monospace)
                                        },
                                        onItemSelected = {
                                            settingsChanges = settingsChanges.copy(hapticIntensity = it)
                                        }
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val oldIntensity = HapticUtils.intensity
                                        val oldEnabled = HapticUtils.isEnabled
                                        HapticUtils.isEnabled = true
                                        HapticUtils.intensity = currentIntensity
                                        HapticUtils.performButtonPressFeedback(view)
                                        HapticUtils.intensity = oldIntensity
                                        HapticUtils.isEnabled = oldEnabled
                                    },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "TEST",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Symmetrical Buttons at the bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button (Left)
                    val resetInteractionSource = remember { MutableInteractionSource() }
                    val resetIsPressed by resetInteractionSource.collectIsPressedAsState()
                    val resetScale by animateFloatAsState(targetValue = if (resetIsPressed) 0.96f else 1.0f, label = "resetScale")

                    OutlinedButton(
                        onClick = {
                            settingsChanges = SettingsChanges()
                            runBlocking { settingsRepository.resetAllSettings() }
                            Log.i(logTag, "Settings reset to defaults")
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        interactionSource = resetInteractionSource,
                        modifier = Modifier.weight(1f).scale(resetScale)
                    ) {
                        Text(
                            text = stringResource(R.string.reset),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Save Button (Center)
                    val saveInteractionSource = remember { MutableInteractionSource() }
                    val saveIsPressed by saveInteractionSource.collectIsPressedAsState()
                    val saveScale by animateFloatAsState(targetValue = if (saveIsPressed) 0.96f else 1.0f, label = "saveScale")

                    OutlinedButton(
                        onClick = {
                            var changesSaved = 0
                            runBlocking {
                                try {
                                    settingsChanges.minimalistPalette?.let { settingsRepository.setMinimalistPalette(it); ++changesSaved }
                                    settingsChanges.pollingDelay?.let {
                                        settingsRepository.setPollingDelay(
                                            it
                                        ); ++changesSaved
                                    }
                                    settingsChanges.hapticFeedbackEnabled?.let {
                                        settingsRepository.setHapticFeedbackEnabled(
                                            it
                                        ); ++changesSaved
                                    }
                                    settingsChanges.hapticIntensity?.let {
                                        settingsRepository.setHapticIntensity(
                                            it
                                        ); ++changesSaved
                                    }
                                    settingsChanges.saveConnectionCredentials?.let {
                                        settingsRepository.setSaveConnectionCredentials(
                                            it
                                        ); ++changesSaved
                                    }
                                    settingsChanges.fullScreenEnabled?.let {
                                        settingsRepository.setFullScreenEnabled(
                                            it
                                        ); ++changesSaved
                                    }
                                } catch (e: Exception) {
                                    Log.e(logTag, "Error saving settings", e)
                                }
                            }
                            Log.i(logTag, "Saved settings: $changesSaved")
                            onNavigateBack()
                        },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        interactionSource = saveInteractionSource,
                        modifier = Modifier.weight(1.2f).scale(saveScale) // slightly wider for central prominence
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Cancel Button (Right)
                    val cancelInteractionSource = remember { MutableInteractionSource() }
                    val cancelIsPressed by cancelInteractionSource.collectIsPressedAsState()
                    val cancelScale by animateFloatAsState(targetValue = if (cancelIsPressed) 0.96f else 1.0f, label = "cancelScale")

                    OutlinedButton(
                        onClick = onNavigateBack,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        interactionSource = cancelInteractionSource,
                        modifier = Modifier.weight(1f).scale(cancelScale)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun SettingsScreenPreview() {
    PreviewBase {
        SettingsScreen(
            onNavigateBack = {},
            onNavigateToGamepadCustomization = {},
            settingsRepository = SettingsRepository(LocalContext.current)
        )
    }
}

@Composable
private fun PaletteCircle(
    palette: MinimalistPalette,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        palette.background,
        palette.surface,
        palette.muted,
        palette.text,
        palette.accent
    )
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Selection Ring
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        // Concentric sectors Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isSelected) 4.dp else 0.dp)
        ) {
            val sizePx = size.minDimension
            val radius = sizePx / 2

            // Draw a neutral gray translucent background chip (20% opacity)
            drawCircle(
                color = Color(0x33808080),
                radius = radius,
                center = center
            )

            val strokeWidth = sizePx * 0.35f
            val arcRadius = radius - strokeWidth / 2

            for (i in 0 until 5) {
                val startAngle = i * 72f - 90f // Start from the top (12 o'clock)
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = 72f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(radius - arcRadius, radius - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2)
                )
            }
        }

        // Selection Checkmark Badge
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .shadow(1.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = contrasting(palette.accent),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
