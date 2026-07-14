package io.github.amarthyasg.airstix.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.amarthyasg.airstix.MainActivity
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.TestGamepadServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun getString(id: Int, vararg formatArgs: Any): String {
        return composeTestRule.activity.getString(id, *formatArgs)
    }

    @Before
    fun setup() {
        TestGamepadServer.start(0) // Start on random port
    }

    @After
    fun teardown() {
        TestGamepadServer.stop()
    }

    @Test
    fun testSuccessfulConnection() {
        val port = TestGamepadServer.getPort().toString()

        composeTestRule.onNodeWithText(getString(R.string.menu_start)).performClick()

        composeTestRule.onNodeWithText(getString(R.string.connect_ip_label))
            .performTextInput("127.0.0.1")
        composeTestRule.onNodeWithText(getString(R.string.connect_port_label))
            .performTextInput(port)

        composeTestRule.onNodeWithText(getString(R.string.connect_button)).performClick()

        // Wait for connection success, which redirects to Main Menu and displays the Resume button
        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule.onNodeWithText("Resume")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Resume").performClick()

        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule.onNodeWithText(getString(R.string.button_l_shoulder))
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
