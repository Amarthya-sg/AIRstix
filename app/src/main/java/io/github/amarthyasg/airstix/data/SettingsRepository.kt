package io.github.amarthyasg.airstix.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    private val dataStore = context.settingsDataStore
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _buttonConfigs = MutableStateFlow<Map<ButtonComponent, ButtonConfig>>(emptyMap())
    val buttonConfigs: StateFlow<Map<ButtonComponent, ButtonConfig>> = _buttonConfigs.asStateFlow()

    init {
        repositoryScope.launch {
            dataStore.data.collect { preferences ->
                val jsonString = preferences[BUTTON_CONFIGS]
                val configs = if (jsonString != null) {
                    try {
                        Json.decodeFromString<Map<ButtonComponent, ButtonConfig>>(jsonString)
                    } catch (e: Exception) {
                        defaultButtonConfigs
                    }
                } else {
                    defaultButtonConfigs
                }
                // Merge default button configs to ensure new components like SETTINGS_BUTTON are present
                val sanitizedConfigs = defaultButtonConfigs + configs
                _buttonConfigs.value = sanitizedConfigs
            }
        }
    }

    val minimalistPalette: Flow<MinimalistPalette> = dataStore.data.map { preferences ->
        MinimalistPalette.fromInt(preferences[MINIMALIST_PALETTE] ?: defaultMinimalistPalette.ordinal)
    }

    val pollingDelay: Flow<Int> = dataStore.data.map { preferences ->
        preferences[POLLING_DELAY] ?: defaultPollingDelay
    }

    val hapticFeedbackEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAPTIC_FEEDBACK_ENABLED] ?: defaultHapticFeedbackEnabled
    }

    val hapticIntensity: Flow<HapticIntensity> = dataStore.data.map { preferences ->
        HapticIntensity.fromInt(preferences[HAPTIC_INTENSITY] ?: defaultHapticIntensity.ordinal)
    }

    val lastConnectionIpAddress: Flow<String> = dataStore.data.map { preferences ->
        preferences[LAST_CONNECTION_IP_ADDRESS] ?: ""
    }

    val lastConnectionPort: Flow<String> = dataStore.data.map { preferences ->
        preferences[LAST_CONNECTION_PORT] ?: ""
    }

    val saveConnectionCredentials: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SAVE_CONNECTION_CREDENTIALS] ?: defaultSaveConnectionCredentials
    }

    val fullScreenEnabled: Flow<Boolean> = flowOf(true)

    val activeProfileName: Flow<String> = dataStore.data.map { preferences ->
        preferences[ACTIVE_PROFILE_NAME] ?: "Default Layout"
    }

    val faceButtonsGrouped: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FACE_BUTTONS_GROUPED] ?: true
    }

    val dpadGrouped: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DPAD_GROUPED] ?: true
    }

    suspend fun setMinimalistPalette(palette: MinimalistPalette) {
        dataStore.edit { preferences ->
            preferences[MINIMALIST_PALETTE] = palette.ordinal
        }
    }

    suspend fun setPollingDelay(delay: Int) {
        dataStore.edit { preferences ->
            preferences[POLLING_DELAY] = delay
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun setButtonConfig(component: ButtonComponent, config: ButtonConfig) {
        _buttonConfigs.update { current ->
            current + (component to config)
        }
        persistButtonConfigs()
    }

    suspend fun setAllButtonConfigs(configs: Map<ButtonComponent, ButtonConfig>) {
        _buttonConfigs.value = configs
        persistButtonConfigs()
    }

    private suspend fun persistButtonConfigs() {
        dataStore.edit { preferences ->
            preferences[BUTTON_CONFIGS] = Json.encodeToString(_buttonConfigs.value)
        }
    }

    suspend fun setLastConnectionCredentials(ipAddress: String, port: String) {
        dataStore.edit { preferences ->
            preferences[LAST_CONNECTION_IP_ADDRESS] = ipAddress
            preferences[LAST_CONNECTION_PORT] = port
        }
    }

    suspend fun setSaveConnectionCredentials(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SAVE_CONNECTION_CREDENTIALS] = enabled
        }
    }

    suspend fun setHapticIntensity(intensity: HapticIntensity) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_INTENSITY] = intensity.ordinal
        }
    }

    suspend fun setFullScreenEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FULL_SCREEN_ENABLED] = enabled
        }
    }

    suspend fun setActiveProfileName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        dataStore.edit { preferences ->
            preferences[ACTIVE_PROFILE_NAME] = trimmed
        }
    }

    suspend fun setFaceButtonsGrouped(grouped: Boolean) {
        dataStore.edit { preferences ->
            preferences[FACE_BUTTONS_GROUPED] = grouped
        }
    }

    suspend fun setDpadGrouped(grouped: Boolean) {
        dataStore.edit { preferences ->
            preferences[DPAD_GROUPED] = grouped
        }
    }

    suspend fun resetAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        _buttonConfigs.value = defaultButtonConfigs
    }

    companion object {
        private val MINIMALIST_PALETTE = intPreferencesKey("minimalist_palette")
        private val POLLING_DELAY = intPreferencesKey("polling_delay")
        private val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        private val HAPTIC_INTENSITY = intPreferencesKey("haptic_intensity")
        private val BUTTON_CONFIGS = stringPreferencesKey("button_configs")
        private val LAST_CONNECTION_IP_ADDRESS = stringPreferencesKey("last_connection_ip_address")
        private val LAST_CONNECTION_PORT = stringPreferencesKey("last_connection_port")
        private val SAVE_CONNECTION_CREDENTIALS =
            booleanPreferencesKey("save_connection_credentials")
        private val FULL_SCREEN_ENABLED = booleanPreferencesKey("full_screen_enabled")
        private val ACTIVE_PROFILE_NAME = stringPreferencesKey("active_profile_name")
        private val FACE_BUTTONS_GROUPED = booleanPreferencesKey("face_buttons_grouped")
        private val DPAD_GROUPED = booleanPreferencesKey("dpad_grouped")
    }
}
