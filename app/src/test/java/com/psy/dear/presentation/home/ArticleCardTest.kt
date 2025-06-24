package com.psy.dear.presentation.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import com.psy.dear.domain.model.Article

class ArticleCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun articleCardDisplaysTitle() {
        composeRule.setContent { ArticleCard(Article("1","Title","url")) }
        composeRule.onNodeWithText("Title").assertIsDisplayed()
    }
}
