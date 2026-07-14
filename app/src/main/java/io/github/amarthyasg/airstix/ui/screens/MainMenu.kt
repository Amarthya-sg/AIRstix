package io.github.amarthyasg.airstix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.data.PreviewHeightDp
import io.github.amarthyasg.airstix.data.PreviewWidthDp
import io.github.amarthyasg.airstix.network.ConnectionViewModel
import io.github.amarthyasg.airstix.ui.theme.SuccessGreen
import io.github.amarthyasg.airstix.data.SettingsRepository
import io.github.amarthyasg.airstix.data.ButtonComponent
import io.github.amarthyasg.airstix.data.ButtonConfig
import io.github.amarthyasg.airstix.data.defaultButtonConfigs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import io.github.amarthyasg.airstix.ui.composables.HUDViewfinder
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private fun sanitizeButtonConfigs(configs: Map<ButtonComponent, ButtonConfig>): Map<ButtonComponent, ButtonConfig> {
    return ButtonComponent.entries.associateWith { component ->
        configs[component] ?: ButtonConfig.default(component)
    }
}

@Composable
fun MainMenu(
    connectionViewModel: ConnectionViewModel?,
    settingsRepository: SettingsRepository,
    onNavigateToConnectScreen: () -> Unit,
    onNavigateToSettingsScreen: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToGamepadCustomization: () -> Unit,
    onNavigateToGamepad: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val connectionState by connectionViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "0.5.0"
        }
    }

    var showProfileDialog by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder Corner Brackets
            HUDViewfinder(modifier = Modifier.padding(16.dp))

            // Pulse ring animations
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale1 by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 2.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, easing = EaseOutQuad),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scale1"
            )
            val opacity1 by infiniteTransition.animateFloat(
                initialValue = 0.55f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, easing = EaseOutQuad),
                    repeatMode = RepeatMode.Restart
                ),
                label = "opacity1"
            )
            val scale2 by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 2.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, delayMillis = 860, easing = EaseOutQuad),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scale2"
            )
            val opacity2 by infiniteTransition.animateFloat(
                initialValue = 0.55f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, delayMillis = 860, easing = EaseOutQuad),
                    repeatMode = RepeatMode.Restart
                ),
                label = "opacity2"
            )

            // Symmetrical HUD Body
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                // Left Nav Column
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HUDLink(index = "01", label = "Profile", isLeft = true, onClick = { showProfileDialog = true })
                    HUDLink(index = "02", label = "Customize", isLeft = true, onClick = onNavigateToGamepadCustomization)
                }

                // Center circular start core action button
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNavigateToConnectScreen
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Outer Rings
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .scale(scale1)
                            .alpha(opacity1)
                            .background(Color.Transparent, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .scale(scale2)
                            .alpha(opacity2)
                            .background(Color.Transparent, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )

                    // Core Button
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Top Dot
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 14.dp)
                                .size(4.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "AIRstix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(9.dp)
                                )
                                Text(
                                    text = stringResource(R.string.menu_start),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.14.sp
                                )
                            }
                        }
                    }
                }

                // Right Nav Column
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HUDLink(index = "03", label = "Settings", isLeft = false, onClick = onNavigateToSettingsScreen)
                    HUDLink(index = "04", label = "About", isLeft = false, onClick = onNavigateToAboutScreen)
                }
            }

            // Connection Status (Top Center)
            val (statusColor, statusText) = when {
                connectionState?.connected == true -> {
                    SuccessGreen to "CONNECTED"
                }
                connectionState?.isConnecting == true -> {
                    Color(0xFFFFB300) to "CONNECTING"
                }
                connectionState?.error != null -> {
                    MaterialTheme.colorScheme.error to "ERROR"
                }
                else -> {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) to "NOT CONNECTED"
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color = statusColor, shape = CircleShape)
                )
                Text(
                    text = statusText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    letterSpacing = 0.04.sp
                )
            }

            // Conditional "Resume" button — visible only when a connection is already live.
            // Uses AnimatedVisibility for a smooth fade+slide entry/exit.
            AnimatedVisibility(
                visible = connectionState?.connected == true,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 152.dp),
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            animationSpec = tween(300, easing = EaseOutCubic),
                            initialOffsetY = { -it / 3 }
                        ),
                exit = fadeOut(animationSpec = tween(200)) +
                        slideOutVertically(
                            animationSpec = tween(200, easing = EaseInCubic),
                            targetOffsetY = { -it / 3 }
                        )
            ) {
                HUDLink(
                    index = "",
                    label = "Resume",
                    isLeft = false,
                    accentColor = SuccessGreen,
                    onClick = onNavigateToGamepad
                )
            }

            // Exit Button (HUD Glowing Rounded Corner Rect - Bottom Center)
            val exitInteractionSource = remember { MutableInteractionSource() }
            val exitIsPressed by exitInteractionSource.collectIsPressedAsState()
            val exitScale by animateFloatAsState(
                targetValue = if (exitIsPressed) 0.96f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "exitScale"
            )
            val exitBorderColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.error.copy(alpha = if (exitIsPressed) 0.9f else 0.6f),
                animationSpec = tween(180),
                label = "exitBorderColor"
            )
            val exitBgColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.error.copy(alpha = if (exitIsPressed) 0.1f else 0.05f),
                animationSpec = tween(180),
                label = "exitBgColor"
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
                    .scale(exitScale)
                    .shadow(
                        elevation = if (exitIsPressed) 3.dp else 6.dp,
                        shape = RoundedCornerShape(4.dp),
                        clip = false,
                        ambientColor = MaterialTheme.colorScheme.error,
                        spotColor = MaterialTheme.colorScheme.error
                    )
                    .background(
                        color = exitBgColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = exitBorderColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable(
                        interactionSource = exitInteractionSource,
                        indication = null,
                        onClick = onExit
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .clearAndSetSemantics {
                        text = AnnotatedString(context.getString(R.string.menu_exit))
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Exit",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "EXIT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.04.sp
                )
            }

            // Profiles Dialog (HUD Restyled flat list with dividers and left accent bar)
            if (showProfileDialog) {
                val currentProfileName by settingsRepository.activeProfileName.collectAsState(initial = "Default Layout")
                val profilesDir = context.filesDir.resolve("profiles")
                var profileFiles by remember {
                    mutableStateOf(
                        if (profilesDir.exists()) {
                            profilesDir.listFiles { _, name -> 
                                name.endsWith(".json", ignoreCase = true) && name.removeSuffix(".json").isNotBlank()
                            }?.toList() ?: emptyList()
                        } else {
                            emptyList()
                        }
                    )
                }

                AlertDialog(
                    onDismissRequest = { showProfileDialog = false },
                    title = { Text("Layout Profiles", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Default layout
                            val isDefaultActive = (currentProfileName == "Default Layout")
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                settingsRepository.setAllButtonConfigs(defaultButtonConfigs)
                                                settingsRepository.setActiveProfileName("Default Layout")
                                            }
                                            showProfileDialog = false
                                            Toast.makeText(context, "Default Layout loaded", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 2px wide vertical left accent color bar indicating active row
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(24.dp)
                                            .background(if (isDefaultActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = if (isDefaultActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "Default Layout",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDefaultActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isDefaultActive) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }

                            // Saved Profiles
                            if (profileFiles.isEmpty()) {
                                Text(
                                    text = "No saved profiles found. Import layout configs in the customize screen to save them here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                profileFiles.forEach { file ->
                                    val profileName = file.nameWithoutExtension
                                    val isActive = (currentProfileName == profileName)
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    try {
                                                        val jsonText = file.readText()
                                                        val configs: Map<ButtonComponent, ButtonConfig> = Json.decodeFromString(jsonText)
                                                        val sanitized = sanitizeButtonConfigs(configs)
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            settingsRepository.setAllButtonConfigs(sanitized)
                                                            settingsRepository.setActiveProfileName(profileName)
                                                        }
                                                        showProfileDialog = false
                                                        Toast.makeText(context, "Loaded Profile: $profileName", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        Log.e("MainMenu", "Failed to load profile", e)
                                                        Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Left vertical accent bar
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(24.dp)
                                                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))

                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = profileName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (isActive) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Active",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    file.delete()
                                                    profileFiles = if (profilesDir.exists()) {
                                                        profilesDir.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) }?.toList() ?: emptyList()
                                                    } else {
                                                        emptyList()
                                                    }
                                                    Toast.makeText(context, "Deleted profile: $profileName", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Profile",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showProfileDialog = false }) {
                            Text("Close")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showCreateProfileDialog = true }) {
                            Text("Create Profile")
                        }
                    }
                )
            }

            // Create New Profile Input Dialog
            if (showCreateProfileDialog) {
                val profilesDir = context.filesDir.resolve("profiles")
                var newProfileName by remember { mutableStateOf("") }
                var nameErrorMessage by remember { mutableStateOf<String?>(null) }
                
                AlertDialog(
                    onDismissRequest = { showCreateProfileDialog = false },
                    title = { Text("Create New Profile", style = MaterialTheme.typography.titleMedium) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Enter a name for the new profile:")
                            TextField(
                                value = newProfileName,
                                onValueChange = { 
                                    newProfileName = it
                                    nameErrorMessage = null
                                },
                                placeholder = { Text("e.g. MyLayout") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (nameErrorMessage != null) {
                                Text(
                                    text = nameErrorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val cleanedName = newProfileName.trim()
                                when {
                                    cleanedName.isBlank() -> {
                                        nameErrorMessage = "Name cannot be empty."
                                    }
                                    cleanedName.contains("/") || cleanedName.contains("\\") || cleanedName.contains("..") -> {
                                        nameErrorMessage = "Invalid character in profile name."
                                    }
                                    cleanedName == "Default Layout" -> {
                                        nameErrorMessage = "Cannot overwrite Default Layout."
                                    }
                                    profilesDir.resolve("$cleanedName.json").exists() -> {
                                        nameErrorMessage = "A profile with this name already exists."
                                    }
                                    else -> {
                                        try {
                                            if (!profilesDir.exists()) {
                                                profilesDir.mkdirs()
                                            }
                                            val defaultJson = Json.encodeToString(defaultButtonConfigs)
                                            profilesDir.resolve("$cleanedName.json").writeText(defaultJson)
                                            
                                            CoroutineScope(Dispatchers.Main).launch {
                                                // Reset DataStore with defaults
                                                settingsRepository.setAllButtonConfigs(defaultButtonConfigs)
                                                settingsRepository.setActiveProfileName(cleanedName)
                                                
                                                showCreateProfileDialog = false
                                                showProfileDialog = false
                                                onNavigateToGamepadCustomization()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("MainMenu", "Failed to create new profile", e)
                                            nameErrorMessage = "Failed to create profile: ${e.message}"
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showCreateProfileDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HUDLink(
    index: String,
    label: String,
    isLeft: Boolean,
    onClick: () -> Unit,
    accentColor: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActive = isHovered || isPressed

    // Spring scale — snappy press, elastic release
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    val baseColor = accentColor ?: MaterialTheme.colorScheme.primary

    // Smooth color transitions on hover/press
    val shadowColor by animateColorAsState(
        targetValue = if (isActive) baseColor else baseColor.copy(alpha = 0.3f),
        animationSpec = tween(180),
        label = "shadowColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) baseColor else baseColor.copy(alpha = 0.25f),
        animationSpec = tween(180),
        label = "borderColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive) baseColor.copy(alpha = 0.12f) else baseColor.copy(alpha = 0.03f),
        animationSpec = tween(180),
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) baseColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "textColor"
    )

    Row(
        modifier = Modifier
            .width(155.dp)
            .scale(scale)
            .shadow(
                elevation = if (isActive) 8.dp else 4.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.semantics { text = AnnotatedString(label) }
        )
    }
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun MainMenuPreview() {
    PreviewBase {
        MainMenu(
            connectionViewModel = null,
            settingsRepository = SettingsRepository(LocalContext.current),
            onNavigateToConnectScreen = {},
            onNavigateToSettingsScreen = {},
            onNavigateToAboutScreen = {},
            onNavigateToGamepadCustomization = {},
            onNavigateToGamepad = {},
            onExit = {}
        )
    }
}
