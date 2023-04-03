package com.arnyminerz.filmagentaproto.activity

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.core.app.launchActivityForResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arnyminerz.filmagentaproto.utils.DatabaseTest
import com.arnyminerz.filmagentaproto.utils.createAndroidIntentComposeRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestAdminEventActivity : DatabaseTest() {

    private lateinit var scenario: ActivityScenario<AdminEventActivity>

    @get:Rule
    val composeTestRule = createAndroidIntentComposeRule<AdminEventActivity> {
        Intent(it, AdminEventActivity::class.java).apply {
            putExtra(AdminEventActivity.EXTRA_EVENT, 1L)
        }
    }

    @Before
    fun prepare_event() {
        runBlocking { db.wooCommerceDao().insert(Event.EXAMPLE) }
    }

    @Test
    fun test_launch_withExtras_eventNotExist() {
        val intent = Intent(context, AdminEventActivity::class.java)
            .putExtra(AdminEventActivity.EXTRA_EVENT, 10L)
        scenario = launchActivityForResult(intent)
        assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
        val resultData = scenario.result.resultData
        val errorMessage = resultData.getStringExtra(AdminEventActivity.RESULT_ERROR_MESSAGE)
        assertEquals(AdminEventActivity.ERROR_EVENT_NOT_FOUND, errorMessage)
    }

    @Test
    fun test_launch_withExtras_eventExist() {
        val intent = Intent(context, AdminEventActivity::class.java)
            .putExtra(AdminEventActivity.EXTRA_EVENT, 1L)
        scenario = launchActivity(intent)
        assertEquals(Lifecycle.State.RESUMED, scenario.state)
    }

    @Test
    fun test_launch_withoutExtras() {
        scenario = launchActivityForResult()
        assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
        val resultData = scenario.result.resultData
        val errorMessage = resultData.getStringExtra(AdminEventActivity.RESULT_ERROR_MESSAGE)
        assertEquals(AdminEventActivity.ERROR_MISSING_EVENT, errorMessage)
    }

    @Test
    fun test_launch_back() {
        val intent = Intent(context, AdminEventActivity::class.java)
            .putExtra(AdminEventActivity.EXTRA_EVENT, 1L)
        scenario = launchActivityForResult(intent)
        scenario.onActivity { activity ->
            // Press back
            activity.onBackPressedDispatcher.onBackPressed()
        }
        assertEquals(scenario.result.resultCode, Activity.RESULT_CANCELED)
        val resultData = scenario.result.resultData
        val errorMessage = resultData.getStringExtra(AdminEventActivity.RESULT_ERROR_MESSAGE)
        assertEquals(AdminEventActivity.ERROR_BACK_PRESSED, errorMessage)
    }

    @Test
    fun test_launch_backButton() {
        val intent = Intent(context, AdminEventActivity::class.java)
            .putExtra(AdminEventActivity.EXTRA_EVENT, 1L)
        scenario = launchActivityForResult(intent)
        assertEquals(Lifecycle.State.RESUMED, scenario.state)

        composeTestRule
            .onNode(hasTestTag(AdminEventActivity.TEST_TAG_BACK))
            .performClick()

        val resultData = scenario.result.resultData
        val errorMessage = resultData.getStringExtra(AdminEventActivity.RESULT_ERROR_MESSAGE)
        assertEquals(AdminEventActivity.ERROR_BACK_PRESSED, errorMessage)
    }

    // Tests that all the contents have been loaded correctly in the UI
    @Test
    fun test_launch_load() {
        val intent = Intent(context, AdminEventActivity::class.java)
            .putExtra(AdminEventActivity.EXTRA_EVENT, 1L)
        scenario = launchActivity(intent)
        assertEquals(Lifecycle.State.RESUMED, scenario.state)

        composeTestRule.waitForIdle()

        composeTestRule
            .onNode(hasTestTag(AdminEventActivity.TEST_TAG_TITLE))
            .assertTextEquals(Event.EXAMPLE.title)

        composeTestRule.onNode(hasText("2023-04-12"))
            .assertExists("Could not find an element that displays the event's date.")

        composeTestRule.onNode(hasText("2023-03-23"))
            .assertExists("Could not find an element that displays the event's last reservation date.")
    }

    @After
    fun cleanup() {
        scenario.close()
    }
}
