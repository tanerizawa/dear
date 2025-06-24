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
class JournalCreationWorkflowTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createJournalShowsInList() {
        hiltRule.inject()

        // Go through login first
        composeRule.onNodeWithText("Mulai Sekarang").performClick()
        composeRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeRule.onNodeWithText("Password").performTextInput("password")
        composeRule.onNodeWithText("Login").performClick()

        // Open editor
        composeRule.onNodeWithContentDescription("Entri Baru").performClick()
        composeRule.onNodeWithText("Judul").performTextInput("Test Entry")
        composeRule.onNodeWithText("Apa yang kamu rasakan?").performTextInput("Isi")
        composeRule.onNodeWithText("Simpan").performClick()

        // Verify entry displayed on home
        composeRule.onNodeWithText("Test Entry").assertIsDisplayed()
    }
}
