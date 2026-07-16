package io.github.amarthyasg.airstix.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ButtonConfigSerializationTest {

    @Test
    fun testDefaultHapticEnabled() {
        val config = ButtonConfig(ButtonComponent.FACE_BUTTON_A)
        assertTrue(config.hapticEnabled)
    }

    @Test
    fun testSerializationRoundTrip() {
        val configTrue = ButtonConfig(ButtonComponent.FACE_BUTTON_A, hapticEnabled = true)
        val jsonTrue = Json.encodeToString(configTrue)
        val decodedTrue = Json.decodeFromString<ButtonConfig>(jsonTrue)
        assertTrue(decodedTrue.hapticEnabled)

        val configFalse = ButtonConfig(ButtonComponent.FACE_BUTTON_A, hapticEnabled = false)
        val jsonFalse = Json.encodeToString(configFalse)
        val decodedFalse = Json.decodeFromString<ButtonConfig>(jsonFalse)
        assertFalse(decodedFalse.hapticEnabled)
    }

    @Test
    fun testBackwardCompatibility() {
        // A serialized ButtonConfig missing the "hapticEnabled" field
        val legacyJson = """{"component":"FACE_BUTTON_A","visible":true,"scale":1.0,"opacity":1.0,"offsetX":0.0,"offsetY":0.0,"anchor":"CENTER"}"""
        val decoded = Json.decodeFromString<ButtonConfig>(legacyJson)
        // Verify it defaults to true
        assertTrue(decoded.hapticEnabled)
    }
}
