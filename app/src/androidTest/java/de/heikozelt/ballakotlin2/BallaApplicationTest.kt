package de.heikozelt.ballakotlin2

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

import org.junit.Test

/**
 * Android instrumentation tests with good old JUnit 4.
 * Tests run on Android device.
 */
class BallaApplicationTest {

    @Test
    fun getAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        //Log.d(TAG, "appContext is ${appContext::class.qualifiedName}")
        assertEquals("android.app.ContextImpl", appContext::class.qualifiedName)
    }

    @Test
    fun getApp() {
        val app = ApplicationProvider.getApplicationContext<Context>()
        //Log.d(TAG, "ctx is ${ctx::class.qualifiedName}")
        assertEquals("de.heikozelt.ballakotlin2.BallaApplication", app::class.qualifiedName)
    }

    /**
     * The package name of the app is important for publishing it in google play store.
     * It cannot change between updates of an app.
     */
    @Test
    fun getPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("de.heikozelt.ballakotlin2", appContext.packageName)
    }

    @Test
    fun getResourceString_app_name() {
        val app = ApplicationProvider.getApplicationContext<Context>()
        val str = app.getString(R.string.app_name)
        assertEquals("Balla Balla", str)
    }

    /* Initialisierung nicht mehr im Konstruktor von BallaApplication sondern in der onCreate()-Methode
    @Test
    fun getGameController() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val app = ctx.applicationContext as BallaApplication
        val controller = app.gameController

        assertFalse(controller.isUp())
        assertEquals(BallaApplication.NUMBER_OF_COLORS, controller.getNumberOfColors())
    }
    */

    companion object {
        private const val TAG = "balla.BallaApplicationTest"
    }
}