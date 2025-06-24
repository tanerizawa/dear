package com.psy.dear.workflow

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import com.psy.dear.MainActivity

@HiltAndroidTest
class ChatWorkflowTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun sendMessageDisplaysInChat() {
        hiltRule.inject()

        // Login first
        composeRule.onNodeWithText("Mulai Sekarang").performClick()
        composeRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeRule.onNodeWithText("Password").performTextInput("password")
        composeRule.onNodeWithText("Login").performClick()

        // Navigate to chat screen
        composeRule.onNodeWithText("Chat").performClick()
        composeRule.onNodeWithText("Type a message").performTextInput("Hello")
        composeRule.onNodeWithContentDescription("Send Message").performClick()

        composeRule.onNodeWithText("Hello").assertIsDisplayed()
    }
}
