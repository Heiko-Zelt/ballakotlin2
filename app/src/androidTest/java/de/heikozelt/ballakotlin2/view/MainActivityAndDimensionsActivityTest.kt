package de.heikozelt.ballakotlin2.view

import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.ext.junit.rules.activityScenarioRule
import de.heikozelt.ballakotlin2.BallaApplication
import de.heikozelt.ballakotlin2.R
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.*
import java.lang.Thread.sleep


class MainActivityAndDimensionsActivityTest {

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
        assertNotNull(controller)
        assertEquals(false, controller.isUp())
        assertNotNull(controller.getGameState())

        // Popup-Menü öffnen:
        onView(withId(R.id.main_btn_burger_menu)).perform(click())
        sleep(1_000)

        //android.R.id.content
        /*
        activity?.findViewById<ViewGroup>(android.R.id.selectAll)?.let { vg ->
            logViewChilds(vg)
        }
         */

        // Menüpunkt "Dimensionen" auswählen:
        onData(anything()).atPosition(2).perform(click())

        sleep(1_000)

        // Werden die 3 Sekkbars angezeigt?
        onView(withId(R.id.dimensions_seekbar_number_of_colors)).check(matches(isDisplayed()))
        onView(withId(R.id.dimensions_seekbar_additional_tubes)).check(matches(isDisplayed()))
        onView(withId(R.id.dimensions_seekbar_height)).check(matches(isDisplayed()))

        // seekbar progress 0..13, mitte ist
        // klickt genau in die Mitte, also progress zwischen 6 und 7, entspricht 8 oder 9 Farben
        onView(withId(R.id.dimensions_seekbar_number_of_colors)).perform(click())

        // Wird 8 angezeigt?
        onView(withId(R.id.dimensions_label_number_of_colors)).check(
            matches(
                anyOf(
                    withText(containsString("8")),
                    withText(containsString("9"))
                )
            )
        )
        sleep(1000)
        // Activity schließen mit Ergebnis successful
        onView(withId(R.id.dimensions_btn_ok)).perform(click())

        //sleep(1000)
        /*
        val gs = controller.getGameState()
        assertNotNull(gs)
        gs?.apply {
            assertEquals(8, numberOfColors)
        }
         */
    }

    /*
    fun logViewChilds(vg: ViewGroup) {
        for(i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            Log.d(TAG, "#$i: firstChild.id=${child.id}")
            Log.d(TAG, "#$i: firstChild.tag=${child.tag}")
            if (child is ViewGroup) {
                val vg2 = child as ViewGroup
                Log.d(TAG, "is ViewGroup with childCount=${vg.childCount}:")
                logViewChilds(vg2)
            } else {
                Log.d(TAG, "is not a viewGroup")
            }
        }
    }
     */

    companion object {
        private const val TAG = "balla.MAADATest"
    }
}