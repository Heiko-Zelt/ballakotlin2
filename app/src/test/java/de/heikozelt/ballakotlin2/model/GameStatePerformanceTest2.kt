package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.IndexOutOfBoundsException
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.test.runTest

/**
 * JUnit 5-Tests.
 * Tests sind alphabetisch sortiert, weil es so viele sind.
 */
class GameStatePerformanceTest2 {

    /**
     * Tube.isFull() Performance
     * 50.000.000 repetitions
     * normal: 3.622...3.984 sec
     * inline fun: 3.719...3.836 sec
     */
    @Test
    fun tubes_isFull_performance() {
        val ITERATIONS = 50_000_000
        var count = 0
        GameState().apply {
            resize(12, 1, 6)
            rainbow()
            for(i in 0 until ITERATIONS) {
                for (t in tubes) {
                    if(t.isFull()) count++
                }
            }
        }
        assertEquals(ITERATIONS * 12, count)
    }

    companion object {
        private const val TAG = "balla.GameStatePerfTest"
    }
}