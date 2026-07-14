package io.github.amarthyasg.airstix.ui.screens

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.amarthyasg.VGP_Data_Exchange.GamepadReading
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.ButtonComponent
import io.github.amarthyasg.airstix.data.ButtonConfig
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.data.SettingsRepository
import io.github.amarthyasg.airstix.data.defaultButtonConfigs
import io.github.amarthyasg.airstix.ui.composables.ButtonConfigEditor
import io.github.amarthyasg.airstix.ui.composables.DrawGamepad
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import io.github.amarthyasg.airstix.ui.composables.ResponsiveGrid
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import io.github.amarthyasg.airstix.ui.composables.FullScreenGamepadCustomizer
import io.github.amarthyasg.airstix.ui.composables.HUDViewfinder
import io.github.amarthyasg.airstix.ui.composables.dpadIndividualConfigsFromGroup
import io.github.amarthyasg.airstix.ui.composables.sanitizeUngroupedDpadConfigs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import io.github.amarthyasg.airstix.data.SCALE_VALUE_RANGE
import io.github.amarthyasg.airstix.data.OFFSET_VALUE_RANGE

private const val logTag = "GamepadCustomization"

private fun sanitizeButtonConfigs(configs: Map<ButtonComponent, ButtonConfig>): Map<ButtonComponent, ButtonConfig> {
    return ButtonComponent.entries.associateWith { component ->
        configs[component] ?: ButtonConfig.default(component)
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String {
    var name = "Imported_Layout.json"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val displayName = cursor.getString(nameIndex)
                if (!displayName.isNullOrBlank()) {
                    name = displayName
                }
            }
        }
    } catch (e: Exception) {
        Log.e("GamepadCustomization", "Failed to resolve filename from uri", e)
    }
    if (!name.endsWith(".json", ignoreCase = true)) {
        name += ".json"
    }
    return name
}

/**
 * Full-screen gamepad preview overlay
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun GamepadPreview(
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
    faceButtonsGrouped: Boolean = true,
    dpadGrouped: Boolean = true,
    onDismiss: () -> Unit,
) {
    val gamepadState = remember { GamepadReading() }

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
        )

        // Overlay a small Close floating button at the top center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Preview",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.back),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun GamepadCustomizationScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val savedButtonConfigs by settingsRepository.buttonConfigs.collectAsState(initial = emptyMap())
    val savedActiveProfileName by settingsRepository.activeProfileName.collectAsState(initial = "")
    val savedFaceButtonsGrouped by settingsRepository.faceButtonsGrouped.collectAsState(initial = true)
    val savedDpadGrouped by settingsRepository.dpadGrouped.collectAsState(initial = true)

    var tempButtonConfigs by remember { mutableStateOf<Map<ButtonComponent, ButtonConfig>>(emptyMap()) }
    var initialButtonConfigs by remember { mutableStateOf<Map<ButtonComponent, ButtonConfig>>(emptyMap()) }
    var tempActiveProfileName by remember { mutableStateOf("") }
    var tempFaceButtonsGrouped by remember { mutableStateOf(true) }
    var initialFaceButtonsGrouped by remember { mutableStateOf(true) }
    var tempDpadGrouped by remember { mutableStateOf(true) }
    var initialDpadGrouped by remember { mutableStateOf(true) }

    var showMenuPage by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var showImportErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(savedButtonConfigs, savedActiveProfileName, savedFaceButtonsGrouped, savedDpadGrouped) {
        if (tempButtonConfigs.isEmpty() && savedButtonConfigs.isNotEmpty() && savedActiveProfileName.isNotEmpty()) {
            val loadedConfigs = if (!savedDpadGrouped) {
                sanitizeUngroupedDpadConfigs(savedButtonConfigs)
            } else {
                savedButtonConfigs
            }
            tempButtonConfigs = loadedConfigs
            initialButtonConfigs = loadedConfigs
            tempActiveProfileName = savedActiveProfileName
            tempFaceButtonsGrouped = savedFaceButtonsGrouped
            initialFaceButtonsGrouped = savedFaceButtonsGrouped
            tempDpadGrouped = savedDpadGrouped
            initialDpadGrouped = savedDpadGrouped
        }
    }

    // File pick launcher for importing configurations
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonText = inputStream?.bufferedReader()?.use { it.readText() }
                if (jsonText.isNullOrBlank()) {
                    throw IllegalArgumentException("The selected file is empty.")
                }

                // Parse configurations Map
                val configs: Map<ButtonComponent, ButtonConfig> = Json.decodeFromString(jsonText)

                // Validate config structure matches components
                configs.forEach { (component, config) ->
                    if (component != config.component) {
                        throw IllegalArgumentException("Config key mismatch: $component is mapped to ${config.component}")
                    }
                }

                // Resolve file name
                val fileName = getFileName(context, uri)

                // Sanitize and load into local session state (does not write to DB/storage until saved)
                val sanitizedConfigs = sanitizeButtonConfigs(configs)
                val profileName = fileName.removeSuffix(".json")
                tempButtonConfigs = sanitizedConfigs
                tempActiveProfileName = profileName
                importErrorMessage = null
                showMenuPage = false // Go back to customization editor to preview changes
            } catch (e: Exception) {
                Log.e(logTag, "Failed to import JSON config file", e)
                importErrorMessage = e.message ?: "Invalid JSON configuration format"
                showImportErrorDialog = true
            }
        }
    }

    // File save launcher for exporting configurations
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = Json.encodeToString(tempButtonConfigs.ifEmpty { defaultButtonConfigs })
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                Log.i(logTag, "Configuration exported successfully to: $uri")
            } catch (e: Exception) {
                Log.e(logTag, "Failed to export JSON file", e)
            }
        }
    }

    // Intercept back presses to toggle between visual editor and menu screen with confirmation checks
    BackHandler {
        if (showMenuPage) {
            val hasUnsavedChanges = tempButtonConfigs != initialButtonConfigs
                || tempFaceButtonsGrouped != initialFaceButtonsGrouped
                || tempDpadGrouped != initialDpadGrouped
            if (hasUnsavedChanges) {
                showExitConfirmation = true
            } else {
                onNavigateBack()
            }
        } else {
            showMenuPage = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showMenuPage) {
            FullScreenGamepadCustomizer(
                buttonConfigs = remember(tempButtonConfigs, tempDpadGrouped) {
                    val configs = tempButtonConfigs.ifEmpty { defaultButtonConfigs }
                    if (tempDpadGrouped) configs else sanitizeUngroupedDpadConfigs(configs)
                },
                faceButtonsGrouped = tempFaceButtonsGrouped,
                dpadGrouped = tempDpadGrouped,
                onConfigChange = { component, newConfig ->
                    tempButtonConfigs = tempButtonConfigs + (component to newConfig)
                },
                onDismiss = { showMenuPage = true }
            )
        } else {
            // Symmetrical Options Menu Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                // Add the HUD viewfinder corner brackets motif to options screen
                HUDViewfinder()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.widthIn(max = 520.dp).padding(vertical = 32.dp, horizontal = 24.dp)
                ) {
                    Text(
                        text = "GAMEPAD CUSTOMIZATION",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .semantics { text = AnnotatedString(context.getString(R.string.customization_title)) }
                    )

                    // Row 1: Resume Customizing & Save & Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val resumeInteractionSource = remember { MutableInteractionSource() }
                        val resumeIsPressed by resumeInteractionSource.collectIsPressedAsState()
                        val resumeScale by animateFloatAsState(targetValue = if (resumeIsPressed) 0.96f else 1.0f, label = "resumeScale")

                        OutlinedButton(
                            onClick = { showMenuPage = false },
                            modifier = Modifier.weight(1f).scale(resumeScale),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            interactionSource = resumeInteractionSource
                        ) {
                            Text(
                                text = "RESUME",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        val saveInteractionSource = remember { MutableInteractionSource() }
                        val saveIsPressed by saveInteractionSource.collectIsPressedAsState()
                        val saveScale by animateFloatAsState(targetValue = if (saveIsPressed) 0.96f else 1.0f, label = "saveScale")

                        OutlinedButton(
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    // 1. Commit all temporary changes to SettingsRepository DataStore
                                    settingsRepository.setAllButtonConfigs(tempButtonConfigs)
                                    
                                    // 2. Commit the active profile name to DataStore
                                    settingsRepository.setActiveProfileName(tempActiveProfileName)
                                    settingsRepository.setFaceButtonsGrouped(tempFaceButtonsGrouped)
                                    settingsRepository.setDpadGrouped(tempDpadGrouped)
                                    
                                    // 3. Save to profile JSON internally (if not Default Layout)
                                    if (tempActiveProfileName.isNotBlank() && tempActiveProfileName != "Default Layout") {
                                        try {
                                            val jsonString = Json.encodeToString(tempButtonConfigs)
                                            val profilesDir = context.filesDir.resolve("profiles")
                                            if (!profilesDir.exists()) {
                                                profilesDir.mkdirs()
                                            }
                                            profilesDir.resolve("$tempActiveProfileName.json").writeText(jsonString)
                                            Log.i(logTag, "Saved active profile JSON internally: $tempActiveProfileName")
                                        } catch (e: Exception) {
                                            Log.e(logTag, "Failed to save profile JSON internally", e)
                                        }
                                    }
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.weight(1f).scale(saveScale),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            interactionSource = saveInteractionSource
                        ) {
                            Text(
                                text = "SAVE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        val exitInteractionSource = remember { MutableInteractionSource() }
                        val exitIsPressed by exitInteractionSource.collectIsPressedAsState()
                        val exitScale by animateFloatAsState(targetValue = if (exitIsPressed) 0.96f else 1.0f, label = "exitScale")

                        OutlinedButton(
                            onClick = {
                                val hasUnsavedChanges = tempButtonConfigs != initialButtonConfigs
                                    || tempFaceButtonsGrouped != initialFaceButtonsGrouped
                                    || tempDpadGrouped != initialDpadGrouped
                                if (hasUnsavedChanges) {
                                    showExitConfirmation = true
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .scale(exitScale)
                                .semantics { text = AnnotatedString("Cancel") },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            interactionSource = exitInteractionSource
                        ) {
                            Text(
                                text = "EXIT",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // Row 2: Import Config & Export Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val importInteractionSource = remember { MutableInteractionSource() }
                        val importIsPressed by importInteractionSource.collectIsPressedAsState()
                        val importScale by animateFloatAsState(targetValue = if (importIsPressed) 0.96f else 1.0f, label = "importScale")

                        OutlinedButton(
                            onClick = {
                                try {
                                    importLauncher.launch("application/json")
                                } catch (e: Exception) {
                                    importLauncher.launch("*/*")
                                }
                            },
                            modifier = Modifier.weight(1f).scale(importScale),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                            interactionSource = importInteractionSource
                        ) {
                            Text(
                                text = "IMPORT CONFIG",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        val exportInteractionSource = remember { MutableInteractionSource() }
                        val exportIsPressed by exportInteractionSource.collectIsPressedAsState()
                        val exportScale by animateFloatAsState(targetValue = if (exportIsPressed) 0.96f else 1.0f, label = "exportScale")

                        OutlinedButton(
                            onClick = { exportLauncher.launch("gamepad_config.json") },
                            modifier = Modifier.weight(1f).scale(exportScale),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                            interactionSource = exportInteractionSource
                        ) {
                            Text(
                                text = "EXPORT CONFIG",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // Row 3: Group Face Buttons & Group D-Pad side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // --- Face Buttons toggle ---
                        val groupInteractionSource = remember { MutableInteractionSource() }
                        val groupIsPressed by groupInteractionSource.collectIsPressedAsState()
                        val groupScale by animateFloatAsState(targetValue = if (groupIsPressed) 0.96f else 1.0f, label = "groupScale")

                        OutlinedButton(
                            onClick = {
                                val wasGrouped = tempFaceButtonsGrouped
                                tempFaceButtonsGrouped = !wasGrouped
                                if (wasGrouped) {
                                    val gc = tempButtonConfigs[ButtonComponent.FACE_BUTTONS] ?: ButtonConfig.default(ButtonComponent.FACE_BUTTONS)
                                    val s = gc.scale; val a = gc.anchor; val v = gc.visible; val o = gc.opacity
                                    tempButtonConfigs = tempButtonConfigs.toMutableMap().apply {
                                        put(ButtonComponent.FACE_BUTTON_A, ButtonConfig(ButtonComponent.FACE_BUTTON_A, v, s, o, gc.offsetX - 0.135f * s, gc.offsetY + 0.27f * s, a))
                                        put(ButtonComponent.FACE_BUTTON_B, ButtonConfig(ButtonComponent.FACE_BUTTON_B, v, s, o, gc.offsetX, gc.offsetY + 0.135f * s, a))
                                        put(ButtonComponent.FACE_BUTTON_X, ButtonConfig(ButtonComponent.FACE_BUTTON_X, v, s, o, gc.offsetX - 0.135f * s, gc.offsetY, a))
                                        put(ButtonComponent.FACE_BUTTON_Y, ButtonConfig(ButtonComponent.FACE_BUTTON_Y, v, s, o, gc.offsetX - 0.27f * s, gc.offsetY + 0.135f * s, a))
                                    }
                                } else {
                                    val cA = tempButtonConfigs[ButtonComponent.FACE_BUTTON_A] ?: ButtonConfig.default(ButtonComponent.FACE_BUTTON_A)
                                    val cB = tempButtonConfigs[ButtonComponent.FACE_BUTTON_B] ?: ButtonConfig.default(ButtonComponent.FACE_BUTTON_B)
                                    val cX = tempButtonConfigs[ButtonComponent.FACE_BUTTON_X] ?: ButtonConfig.default(ButtonComponent.FACE_BUTTON_X)
                                    val cY = tempButtonConfigs[ButtonComponent.FACE_BUTTON_Y] ?: ButtonConfig.default(ButtonComponent.FACE_BUTTON_Y)
                                    val avgX = ((cX.offsetX + 0.135f * cX.scale) + (cA.offsetX + 0.135f * cA.scale) + (cY.offsetX + 0.27f * cY.scale) + cB.offsetX) / 4f
                                    val avgY = (cX.offsetY + (cA.offsetY - 0.27f * cA.scale) + (cY.offsetY - 0.135f * cY.scale) + (cB.offsetY - 0.135f * cB.scale)) / 4f
                                    tempButtonConfigs = tempButtonConfigs.toMutableMap().apply {
                                        put(ButtonComponent.FACE_BUTTONS, ButtonConfig(ButtonComponent.FACE_BUTTONS, cA.visible || cB.visible || cX.visible || cY.visible, (cA.scale + cB.scale + cX.scale + cY.scale) / 4f, (cA.opacity + cB.opacity + cX.opacity + cY.opacity) / 4f, avgX, avgY, cY.anchor))
                                    }
                                }
                                showMenuPage = false
                            },
                            modifier = Modifier.weight(1f).scale(groupScale),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (tempFaceButtonsGrouped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground),
                            border = BorderStroke(1.dp, if (tempFaceButtonsGrouped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            interactionSource = groupInteractionSource
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("FACE BUTTONS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, maxLines = 1)
                                Switch(checked = tempFaceButtonsGrouped, onCheckedChange = null, modifier = Modifier.scale(0.8f))
                            }
                        }

                        // --- D-Pad toggle ---
                        val dpadGroupInteractionSource = remember { MutableInteractionSource() }
                        val dpadGroupIsPressed by dpadGroupInteractionSource.collectIsPressedAsState()
                        val dpadGroupScale by animateFloatAsState(targetValue = if (dpadGroupIsPressed) 0.96f else 1.0f, label = "dpadGroupScale")

                        OutlinedButton(
                            onClick = {
                                val wasGrouped = tempDpadGrouped
                                tempDpadGrouped = !wasGrouped
                                if (wasGrouped) {
                                    val gc = tempButtonConfigs[ButtonComponent.DPAD] ?: ButtonConfig.default(ButtonComponent.DPAD)
                                    tempButtonConfigs = tempButtonConfigs.toMutableMap().apply { putAll(dpadIndividualConfigsFromGroup(gc)) }
                                } else {
                                    val gc = tempButtonConfigs[ButtonComponent.DPAD] ?: ButtonConfig.default(ButtonComponent.DPAD)
                                    val cU = tempButtonConfigs[ButtonComponent.DPAD_UP] ?: ButtonConfig.default(ButtonComponent.DPAD_UP)
                                    val cD = tempButtonConfigs[ButtonComponent.DPAD_DOWN] ?: ButtonConfig.default(ButtonComponent.DPAD_DOWN)
                                    val cL = tempButtonConfigs[ButtonComponent.DPAD_LEFT] ?: ButtonConfig.default(ButtonComponent.DPAD_LEFT)
                                    val cR = tempButtonConfigs[ButtonComponent.DPAD_RIGHT] ?: ButtonConfig.default(ButtonComponent.DPAD_RIGHT)
                                    tempButtonConfigs = tempButtonConfigs.toMutableMap().apply {
                                        put(ButtonComponent.DPAD, gc.copy(visible = cU.visible || cD.visible || cL.visible || cR.visible, scale = (cU.scale + cD.scale + cL.scale + cR.scale) / 4f, opacity = (cU.opacity + cD.opacity + cL.opacity + cR.opacity) / 4f))
                                        putAll(dpadIndividualConfigsFromGroup(gc))
                                    }
                                }
                                showMenuPage = false
                            },
                            modifier = Modifier.weight(1f).scale(dpadGroupScale),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (tempDpadGrouped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground),
                            border = BorderStroke(1.dp, if (tempDpadGrouped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            interactionSource = dpadGroupInteractionSource
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("D-PAD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, maxLines = 1)
                                Switch(checked = tempDpadGrouped, onCheckedChange = null, modifier = Modifier.scale(0.8f))
                            }
                        }
                    }

                    // Row 4: Reset Layout to Defaults (bottom)
                    val resetDefaultsInteractionSource = remember { MutableInteractionSource() }
                    val resetDefaultsIsPressed by resetDefaultsInteractionSource.collectIsPressedAsState()
                    val resetDefaultsScale by animateFloatAsState(targetValue = if (resetDefaultsIsPressed) 0.96f else 1.0f, label = "resetDefaultsScale")

                    OutlinedButton(
                        onClick = {
                            tempButtonConfigs = defaultButtonConfigs
                            tempActiveProfileName = "Default Layout"
                            tempFaceButtonsGrouped = true
                            tempDpadGrouped = true
                            showMenuPage = false
                        },
                        modifier = Modifier.fillMaxWidth().scale(resetDefaultsScale),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        interactionSource = resetDefaultsInteractionSource
                    ) {
                        Text("RESET DEFAULTS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, textAlign = TextAlign.Center, maxLines = 1)
                    }


                }
            }
        }

        // Faulty JSON Import Error Dialog
        if (showImportErrorDialog) {
            AlertDialog(
                onDismissRequest = { showImportErrorDialog = false },
                title = { Text("Import Failed", color = MaterialTheme.colorScheme.error) },
                text = {
                    Text(
                        text = "The selected configuration file is faulty and cannot be imported:\n\n${importErrorMessage ?: "Unknown Error"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(onClick = { showImportErrorDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Unsaved Changes Confirmation Dialog
        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showExitConfirmation = false },
                title = { Text("Unsaved Changes", color = MaterialTheme.colorScheme.error) },
                text = {
                    Text(
                        text = "Do you want to leave without saving? You will lose your progress.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            showExitConfirmation = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Leave")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showExitConfirmation = false }) {
                        Text("Keep Editing")
                    }
                }
            )
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 400
)
@Composable
fun GamepadCustomizationScreenPreview() {
    PreviewBase {
        GamepadCustomizationScreen(
            onNavigateBack = {},
            settingsRepository = SettingsRepository(LocalContext.current)
        )
    }
}
