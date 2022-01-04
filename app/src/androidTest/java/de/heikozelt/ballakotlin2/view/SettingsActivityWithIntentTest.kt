package de.heikozelt.ballakotlin2.view

import android.content.Intent
import android.widget.SeekBar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import de.heikozelt.ballakotlin2.R
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.lang.Thread.sleep

class SettingsActivityWithIntentTest {

    private lateinit var activityScenario: ActivityScenario<SettingsActivity>

    @After
    fun tearDown() {
        activityScenario.close()
    }

    @Test
    fun change_number_of_colors() {
        var activity: SettingsActivity? = null
        val intent = Intent(getApplicationContext(), SettingsActivity::class.java)
        intent.putExtra("number_of_colors", 12)
        intent.putExtra("extra_tubes", 3)
        intent.putExtra("height", 3)
        activityScenario = ActivityScenario.launch<SettingsActivity>(intent)
        activityScenario.onActivity {
            activity = it
        }

        assertNotEquals(null, activity)

        val expectedStr12 = activity?.getString(R.string.label_number_of_colors, 12)
        onView(withId(R.id.settings_label_number_of_colors)).check(matches(withText(expectedStr12)))

        //sleep(1000)
        activity?.findViewById<SeekBar>(R.id.settings_seekbar_number_of_colors)?.progress = 13
        val expectedStr15 = activity?.getString(R.string.label_number_of_colors, 15)
        onView(withId(R.id.settings_label_number_of_colors)).check(matches(withText(expectedStr15)))

        //sleep(1000)
        activity?.findViewById<SeekBar>(R.id.settings_seekbar_number_of_colors)?.progress = 0
        val expectedStr2 = activity?.getString(R.string.label_number_of_colors, 2)
        onView(withId(R.id.settings_label_number_of_colors)).check(matches(withText(expectedStr2)))

        //sleep(1000)
        onView(withId(R.id.settings_btn_defaults)).perform(click())
        val expectedStr7 = activity?.getString(R.string.label_number_of_colors, 7)
        onView(withId(R.id.settings_label_number_of_colors)).check(matches(withText(expectedStr7)))

        //sleep(1000)
        // Test haengt ein wenig
        //onView(withId(R.id.settings_btn_ok)).perform(click())
    }
}