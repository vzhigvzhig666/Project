package com.escodro.alkaa.course

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.UUID
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlkaaBlackBoxTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    @Before
    fun launchAlkaa() {
        val context = instrumentation.targetContext
        val launch = requireNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName))
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launch)
        visible(By.desc("Add task"))
    }

    @Test
    fun createsTaskOpensDetailsAndFindsItThroughSearch() {
        val query = token()
        val title = "UI Automator $query"
        createTask(title)

        visible(By.text(title)).click()
        visible(By.desc("Description"))
        visible(By.clazz(EditText).text(title))
        visible(By.desc("Back")).click()
        visible(By.desc("Add task"))
        visible(By.text(title))

        openSearch().text = query
        visible(By.text(title)).click()
        visible(By.desc("Description"))
        visible(By.clazz(EditText).text(title))
    }

    @Test
    fun searchWithUnknownTitleDisplaysNoResults() {
        val query = token()
        val title = "Visible $query"
        createTask(title)
        openSearch().text = query
        visible(By.text(title))

        visible(By.clazz(EditText)).text = "Missing ${token()}"
        visible(By.text("No tasks found"))
        assertTrue(device.wait(Until.gone(By.text(title)), 8_000))
    }

    private fun createTask(title: String) {
        visible(By.desc("Add task")).click()
        visible(By.clazz(EditText)).text = title
        visible(By.text("Add")).click()
        visible(By.text(title))
    }

    private fun openSearch(): UiObject2 {
        visible(By.text("Search")).click()
        return visible(By.clazz(EditText))
    }

    private fun visible(selector: BySelector): UiObject2 =
        requireNotNull(device.wait(Until.findObject(selector), 8_000)) {
            "Element not found: $selector"
        }

    private fun token() = UUID.randomUUID().toString().take(8)

    private companion object {
        const val EditText = "android.widget.EditText"
    }
}
