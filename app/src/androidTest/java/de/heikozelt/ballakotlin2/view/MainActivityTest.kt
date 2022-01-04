package de.heikozelt.ballakotlin2.view

import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import de.heikozelt.ballakotlin2.R
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.ViewAction
import org.junit.Assert.assertNotEquals
import java.lang.Thread.sleep


class MainActivityTest {
    private fun clickIn(x: Int, y: Int): ViewAction {
        return GeneralClickAction(
            Tap.SINGLE,
            {
                val screenPos = IntArray(2)
                println("Balla screenPos[0]=${screenPos[0]}, screenPos[1]=${screenPos[1]}")
                it.getLocationOnScreen(screenPos)
                val screenX = (screenPos[0] + x).toFloat()
                val screenY = (screenPos[1] + y).toFloat()
                floatArrayOf(screenX, screenY)
            },
            Press.FINGER,
            InputDevice.SOURCE_MOUSE,
            MotionEvent.BUTTON_PRIMARY
        )
    }


    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    // Todo: write a useful user interaction test, but it's difficult with MyDrawView, because of the complicated layout
    @Test
    fun firstTest() {
        var view: MyDrawView? = null
        var activity: MainActivity? = null

        activityScenarioRule.scenario.onActivity {
            activity = it
            view = it.findViewById(R.id.my_draw_view)
        }

        onView(withId(R.id.main_btn_reset_game)).perform(click())
        onView(withId(R.id.main_btn_undo)).perform(click())
        onView(withId(R.id.my_draw_view)).perform(clickIn(500,500))
        //sleep(1000)
        onView(withId(R.id.my_draw_view)).perform(clickIn(500,500))
        //sleep(1000)
        onView(withId(R.id.my_draw_view)).perform(clickIn(500,500))
        //sleep(1000)
        onView(withId(R.id.my_draw_view)).perform(clickIn(500,500))
        //sleep(1000)

        val v = view
        val a = activity
        assertNotEquals(null, a)
        assertNotEquals(null, v)

        if(v != null) {
            println("Balla left=${v.left}, top=${v.top}, width=${v.width}, height=${v.height}")
            // Samsung Galaxy A51,
            // portrait : left=12, top=98, width=786, height=1531
            // landscape: left=12, top=98, width=1617, height=700
        }
    }
}