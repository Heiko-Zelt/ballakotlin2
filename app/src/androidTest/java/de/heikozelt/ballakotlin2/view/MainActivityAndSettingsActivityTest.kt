package de.heikozelt.ballakotlin2.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import de.heikozelt.ballakotlin2.BallaApplication
import de.heikozelt.ballakotlin2.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class MainActivityAndSettingsActivityTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun change_number_of_colors() {
        var activity: MainActivity? = null

        activityScenarioRule.scenario.onActivity {
            //println("Balla onActivity()")
            activity = it
        }

        //sleep(1000)
        assertNotEquals(null, activity)

        val controller = (activity?.application as BallaApplication).gameController
        assertNotEquals(null, controller)
        assertEquals(false, controller.isUp())
        assertNotEquals(null, controller.getGameState())

        onView(withId(R.id.main_btn_burger_menu)).perform(click())
        //sleep(1000)
        onView(withId(R.id.settings_seekbar_number_of_colors)).check(matches(isDisplayed()))
        onView(withId(R.id.settings_seekbar_additional_tubes)).check(matches(isDisplayed()))
        onView(withId(R.id.settings_seekbar_height)).check(matches(isDisplayed()))

        // klickt genau in die Mitte, also progress=6, entspricht 8 Farben
        onView(withId(R.id.settings_seekbar_number_of_colors)).perform(click())

        //sleep(1000)
        onView(withId(R.id.settings_btn_ok)).perform(click())

        //sleep(1000)
        assertEquals(8, controller.getGameState().numberOfColors)
    }
}