package de.heikozelt.ballakotlin2.view

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import de.heikozelt.ballakotlin2.R
import org.junit.Rule
import org.junit.Test
import android.widget.SeekBar
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import de.heikozelt.ballakotlin2.BallaApplication
import org.junit.Assert.*

class SettingsActivityTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<SettingsActivity>()

    @Test
    fun change_number_of_colors() {
        var activity: SettingsActivity? = null

        activityScenarioRule.scenario.onActivity {
            activity = it
        }

        //sleep(1000)
        assertNotEquals(null, activity)

        // Es existiert eine App und eine SettingsActivity.
        // Existiert auch eine MainActivity?
        val controller = (activity?.application as BallaApplication).gameController
        assertNotEquals(null, controller)
        assertEquals(false, controller?.isUp())

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
        //onView(withId(R.id.settings_btn_ok)).perform(click())
    }
}