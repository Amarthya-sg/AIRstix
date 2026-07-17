package io.github.amarthyasg.airstix

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.amarthyasg.airstix.data.SettingsRepository
import io.github.amarthyasg.airstix.data.defaultMinimalistPalette
import io.github.amarthyasg.airstix.data.defaultFullScreenEnabled
import io.github.amarthyasg.airstix.data.defaultHapticFeedbackEnabled
import io.github.amarthyasg.airstix.data.defaultHapticIntensity
import io.github.amarthyasg.airstix.data.defaultSaveConnectionCredentials
import io.github.amarthyasg.airstix.network.ConnectionViewModel
import io.github.amarthyasg.airstix.network.ConnectionViewModelFactory
import io.github.amarthyasg.airstix.ui.screens.AboutScreen
import io.github.amarthyasg.airstix.ui.screens.ConnectMenu
import io.github.amarthyasg.airstix.ui.screens.ConnectingScreen
import io.github.amarthyasg.airstix.ui.screens.GamePad
import io.github.amarthyasg.airstix.ui.screens.GamepadCustomizationScreen
import io.github.amarthyasg.airstix.ui.screens.MainMenu
import io.github.amarthyasg.airstix.ui.screens.SettingsScreen
import io.github.amarthyasg.airstix.ui.screens.ConnectionLostScreen
import io.github.amarthyasg.airstix.ui.theme.VirtualGamePadMobileTheme
import io.github.amarthyasg.airstix.ui.utils.HapticUtils
import kotlinx.coroutines.flow.first
import kotlin.system.exitProcess


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        val settingsRepository = SettingsRepository(this)

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same ConnectionViewModel instance created by the first activity.
        // Use the 'by viewModels()' Kotlin property delegate with factory
        // from the activity-ktx artifact
        val connectionViewModel: ConnectionViewModel by viewModels {
            ConnectionViewModelFactory { ip, port ->
                if (settingsRepository.saveConnectionCredentials.first()) {
                    settingsRepository.setLastConnectionCredentials(ip, port.toString())
                }
            }
        }
        setContent {
            AppUI(
                connectionViewModel = connectionViewModel,
                settingsRepository = settingsRepository,
            )
        }
    }

    @Composable
    private fun AppUI(
        connectionViewModel: ConnectionViewModel,
        settingsRepository: SettingsRepository,
    ) {
        val hapticEnabled = settingsRepository.hapticFeedbackEnabled.collectAsState(
            initial = defaultHapticFeedbackEnabled
        )
        val hapticIntensity = settingsRepository.hapticIntensity.collectAsState(
            initial = defaultHapticIntensity
        )

        LaunchedEffect(hapticEnabled.value, hapticIntensity.value) {
            HapticUtils.isEnabled = hapticEnabled.value
            HapticUtils.intensity = hapticIntensity.value
        }

        val fullScreenEnabled = settingsRepository.fullScreenEnabled.collectAsState(
            initial = defaultFullScreenEnabled
        )

        LaunchedEffect(fullScreenEnabled.value) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (fullScreenEnabled.value) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        VirtualGamePadMobileTheme(
            minimalistPalette = settingsRepository.minimalistPalette.collectAsState(
                initial = defaultMinimalistPalette
            ).value
        ) {
            NavTree(
                connectionViewModel = connectionViewModel,
                settingsRepository = settingsRepository,
            )
        }
    }

    @Composable
    private fun NavTree(
        connectionViewModel: ConnectionViewModel,
        settingsRepository: SettingsRepository,
        navController: NavHostController = rememberNavController(),
    ) {
        val fullScreenEnabled by settingsRepository.fullScreenEnabled.collectAsState(
            initial = defaultFullScreenEnabled
        )

        DisposableEffect(navController, fullScreenEnabled) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            val listener = NavController.OnDestinationChangedListener { _, _, _ ->
                if (fullScreenEnabled) {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }

        NavHost(
            navController = navController,
            startDestination = "main_menu",
            // Default push: new screen slides up from below, exiting scales + fades out
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(380, easing = FastOutSlowInEasing),
                    initialOffset = { it / 6 }
                ) + fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.93f,
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(220, easing = FastOutLinearInEasing))
            },
            // Default pop: exiting screen slides down, previous screen scales back in
            popEnterTransition = {
                scaleIn(
                    initialScale = 0.93f,
                    animationSpec = tween(340, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(280, easing = LinearOutSlowInEasing))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(340, easing = FastOutLinearInEasing),
                    targetOffset = { it / 6 }
                ) + fadeOut(animationSpec = tween(240, easing = FastOutLinearInEasing))
            }
        ) {
            composable("main_menu") {
                MainMenu(
                    connectionViewModel = connectionViewModel,
                    settingsRepository = settingsRepository,
                    onNavigateToConnectScreen = { navController.navigate("connect_screen") },
                    onNavigateToSettingsScreen = { navController.navigate("settings_screen") },
                    onNavigateToAboutScreen = { navController.navigate("about_screen") },
                    onNavigateToGamepadCustomization = { navController.navigate("gamepad_customization") },
                    onNavigateToGamepad = { navController.navigate("gamepad") },
                    onExit = { exitProcess(0) }
                )
            }

            composable("connect_screen") {
                val lastIpAddress by settingsRepository.lastConnectionIpAddress.collectAsState(initial = "")
                val lastPort by settingsRepository.lastConnectionPort.collectAsState(initial = "")
                val saveCredentials by settingsRepository.saveConnectionCredentials.collectAsState(
                    initial = defaultSaveConnectionCredentials
                )
                val initialIp = if (saveCredentials) lastIpAddress else ""
                val initialPort = if (saveCredentials) lastPort else ""

                ConnectMenu(
                    onNavigateToConnectingScreen = { ipAddress, port ->
                        navController.navigate("connecting_screen/$ipAddress/$port")
                    },
                    onNavigateBack = { navController.popBackStack() },
                    initialIp = initialIp,
                    initialPort = initialPort
                )
            }

            // Connecting: lateral flow-forward slide; back uses default pop
            composable(
                "connecting_screen/{ipAddress}/{port}",
                arguments = listOf(
                    navArgument("ipAddress") { type = NavType.StringType },
                    navArgument("port") { type = NavType.StringType }
                ),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(360, easing = FastOutSlowInEasing),
                        initialOffset = { it / 5 }
                    ) + fadeIn(animationSpec = tween(280, easing = LinearOutSlowInEasing))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300, easing = FastOutLinearInEasing),
                        targetOffset = { it / 5 }
                    ) + scaleOut(
                        targetScale = 0.95f,
                        animationSpec = tween(300, easing = FastOutLinearInEasing)
                    ) + fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                },
                popEnterTransition = {
                    scaleIn(
                        initialScale = 0.93f,
                        animationSpec = tween(340, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(280, easing = LinearOutSlowInEasing))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(340, easing = FastOutLinearInEasing),
                        targetOffset = { it / 6 }
                    ) + fadeOut(animationSpec = tween(240, easing = FastOutLinearInEasing))
                }
            ) { backStackEntry ->
                val ipAddress = backStackEntry.arguments?.getString("ipAddress") ?: ""
                val port = backStackEntry.arguments?.getString("port") ?: ""
                ConnectingScreen(
                    onNavigateToMainMenu = {
                        navController.navigate("main_menu") {
                            popUpTo("main_menu") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    connectionViewModel = connectionViewModel,
                    ipAddress = ipAddress,
                    port = port
                )
            }

            // Gamepad: pure cross-fade — no slide over the controller UI
            composable(
                "gamepad",
                enterTransition = {
                    fadeIn(animationSpec = tween(400, easing = LinearOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(400, easing = LinearOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing))
                }
            ) {
                GamePad(
                    connectionViewModel = connectionViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMainMenu = {
                        navController.navigate("main_menu") {
                            popUpTo("main_menu") { inclusive = true }
                        }
                    },
                    onNavigateToConnectionLost = { ipAddress, port, error ->
                        val encodedError = java.net.URLEncoder.encode(error ?: "", "UTF-8")
                        navController.navigate("connection_lost/$ipAddress/$port?error=$encodedError") {
                            popUpTo("main_menu")
                        }
                    },
                    settingsRepository = settingsRepository
                )
            }

            composable(
                "connection_lost/{ipAddress}/{port}?error={error}",
                arguments = listOf(
                    navArgument("ipAddress") { type = NavType.StringType },
                    navArgument("port") { type = NavType.StringType },
                    navArgument("error") { type = NavType.StringType; nullable = true; defaultValue = null }
                ),
                enterTransition = {
                    fadeIn(animationSpec = tween(400, easing = LinearOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(400, easing = LinearOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing))
                }
            ) { backStackEntry ->
                val ipAddress = backStackEntry.arguments?.getString("ipAddress") ?: ""
                val port = backStackEntry.arguments?.getString("port") ?: ""
                val error = backStackEntry.arguments?.getString("error")
                val decodedError = error?.let {
                    try {
                        java.net.URLDecoder.decode(it, "UTF-8")
                    } catch (e: Exception) {
                        it
                    }
                }
                ConnectionLostScreen(
                    errorMessage = decodedError,
                    onReconnect = {
                        navController.navigate("connect_screen") {
                            popUpTo("main_menu") { inclusive = false }
                        }
                    },
                    onNavigateToMainMenu = {
                        navController.navigate("main_menu") {
                            popUpTo("main_menu") { inclusive = true }
                        }
                    }
                )
            }

            composable("settings_screen") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGamepadCustomization = { navController.navigate("gamepad_customization") },
                    settingsRepository = settingsRepository
                )
            }
            composable("gamepad_customization") {
                GamepadCustomizationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    settingsRepository = settingsRepository
                )
            }
            composable("about_screen") {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
