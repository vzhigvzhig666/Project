package com.escodro.alkaa.course

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import com.escodro.alkaa.MainActivity
import java.util.UUID
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class AlkaaInstrumentedTest {
    @get:Rule
    val compose: ComposeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createdTaskAppearsInTasksList() {
        val title = "Compose ${token()}"
        addTask(title)

        compose.onNodeWithText(title).assertIsDisplayed()
        compose.onNodeWithContentDescription("Add task").assertIsDisplayed()
    }

    @Test
    fun taskDetailsShowSelectedTaskAndReturnToList() {
        val title = "Details ${token()}"
        addTask(title)
        compose.onNodeWithText(title).performClick()

        compose.waitUntilExactlyOneExists(hasSetTextAction() and hasText(title), timeoutMillis = 8_000)
        compose.onNode(hasSetTextAction() and hasText(title)).assertIsDisplayed()
        compose.onNodeWithContentDescription("Description").assertIsDisplayed()
        Espresso.pressBack()
        compose.waitUntilExactlyOneExists(hasText(title), timeoutMillis = 8_000)
        compose.onNodeWithContentDescription("Add task").assertIsDisplayed()
    }

    @Test
    fun searchShowsMatchingTask() {
        val query = token()
        val title = "Find $query"
        addTask(title)
        openSearch()
        compose.onNodeWithTag("search_bar").performTextInput(query)

        compose.waitUntilExactlyOneExists(hasText(title), timeoutMillis = 8_000)
        compose.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun searchShowsEmptyStateForUnknownTitle() {
        val query = token()
        val title = "Existing $query"
        addTask(title)
        openSearch()
        compose.onNodeWithTag("search_bar").performTextInput(query)
        compose.waitUntilExactlyOneExists(hasText(title), timeoutMillis = 8_000)

        compose.onNodeWithTag("search_bar").performTextReplacement("Missing ${token()}")
        compose.waitUntilExactlyOneExists(hasText("No tasks found"), timeoutMillis = 8_000)
        compose.onNodeWithText("No tasks found").assertIsDisplayed()
        compose.onNodeWithText(title).assertDoesNotExist()
    }

    private fun addTask(title: String) {
        compose.onNodeWithContentDescription("Add task").performClick()
        compose.waitUntilExactlyOneExists(hasSetTextAction(), timeoutMillis = 8_000)
        compose.onNode(hasSetTextAction()).performTextInput(title)
        compose.onNodeWithText("Add", substring = false).performClick()
        compose.waitUntilExactlyOneExists(hasText(title), timeoutMillis = 8_000)
    }

    private fun openSearch() {
        compose.onNodeWithContentDescription("Search", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("search_bar").assertIsDisplayed()
    }

    private fun token() = UUID.randomUUID().toString().take(8)
}
