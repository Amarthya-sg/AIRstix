package io.github.amarthyasg.airstix.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.amarthyasg.airstix.MainActivity
import io.github.amarthyasg.airstix.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun getString(id: Int, vararg formatArgs: Any): String {
        return composeTestRule.activity.getString(id, *formatArgs)
    }

    @Test
    fun testMainMenuNavigation() {
        // Main Menu to Connect Screen
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.connect_button)).assertIsDisplayed()
        composeTestRule.onNodeWithText(getString(R.string.connect_scan_qr)).assertIsDisplayed()
    }

    @Test
    fun testNavigationToSettings() {
        composeTestRule.onNodeWithText(getString(R.string.menu_settings)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.settings_title))
            .assertIsDisplayed() // Title
 
        composeTestRule.onNodeWithText(getString(R.string.cancel)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }

    @Test
    fun testNavigationToAbout() {
        composeTestRule.onNodeWithText(getString(R.string.menu_about)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.about_title)).assertIsDisplayed()

        composeTestRule.onNodeWithText(getString(R.string.back)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }

    @Test
    fun testNavigationToGamepadCustomization() {
        composeTestRule.onNodeWithText("Customize").performClick()
 
        // 1. Verify we are in the visual layout editor by finding and clicking the close button
        composeTestRule.onNodeWithContentDescription("Exit Customizer").performClick()
 
        // 2. This opens the options menu page. Verify we are on customization options screen
        composeTestRule.onNodeWithText(getString(R.string.customization_title)).assertIsDisplayed()
 
        // 3. Click cancel to return to Main Menu
        composeTestRule.onNodeWithText(getString(R.string.cancel)).performClick()
        composeTestRule.onNodeWithText(getString(R.string.menu_start)).assertIsDisplayed()
    }
}
