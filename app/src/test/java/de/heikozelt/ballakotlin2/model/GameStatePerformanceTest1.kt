package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.IndexOutOfBoundsException
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled

/**
 * JUnit 5-Tests.
 * Tests sind alphabetisch sortiert, weil es so viele sind.
 */
class GameStatePerformanceTest1 {

    /**
     * Tube.isEmpty() Performance
     * 100.000.000 repetitions
     * normal: 7.584...7.804 sec
     * inline fun: 6.837...7.634 sec
     */
    @Test
    @Disabled
    fun tubes_isEmpty_performance() {
        val ITERATIONS = 100_000_000
        var count = 0
        GameState().apply {
            resize(12, 1, 6)
            rainbow()
            for(i in 0 until ITERATIONS) {
                for (t in tubes) {
                    if(t.isEmpty()) count++
                }
            }
        }
        assertEquals(ITERATIONS, count)
    }

    companion object {
        private const val TAG = "balla.GameStatePerfTest"
    }
}