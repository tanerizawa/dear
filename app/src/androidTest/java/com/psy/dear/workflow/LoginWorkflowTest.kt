package com.psy.dear.workflow

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import com.psy.dear.MainActivity

@HiltAndroidTest
class LoginWorkflowTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginNavigatesToHome() {
        hiltRule.inject()

        // Complete onboarding
        composeRule.onNodeWithText("Mulai Sekarang").performClick()

        // Enter credentials on login screen
        composeRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeRule.onNodeWithText("Password").performTextInput("password")
        composeRule.onNodeWithText("Login").performClick()

        // Verify home screen shown
        composeRule.onNodeWithText("Beranda").assertIsDisplayed()
    }
}
