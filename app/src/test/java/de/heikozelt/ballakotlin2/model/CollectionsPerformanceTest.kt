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
class CollectionsPerformanceTest {

    /**
     *               Array                List                 MutableList
     * interator:    3.474 ... 3.533 sec  6.646 ... 6.704 sec  8.262 ... 8.445 sec
     * indices:      3.469 ... 3.487 sec  4.198 ... 4.251 sec  4.659 ... 4.709 sec
     * 0 until size: 3.473 ... 3.531 sec  4.204 ... 4.229 sec  4.663 ... 4.706 sec
     * Fazit:
     *   1. gezählte Schleifen mit "indices" oder "0 until size" sind gleich schnell
     *   2. Arrays sind am schnellsten, MutableLists am langsamsten
     *   3. Bei Arrays sind iterator und gezählte Schleifen gleich schnell
     *   4. Bei List und MutableList sind gezählte Schleifen schneller als interator
     *   5. Wenn sich die Anzahl der Elemente selten ändern, es aber viele Zugriffe gibt, Arrays verwenden?!?
     *
     *   Welche Auswirkung hat var statt val? gering
     *   Welche Auswirkungen hat nullable? .?let { it -> ... } gering
     *   Welche Asuwirkungen hat Objekt-Variable statt lokale Variable? gering
     */

    var tubes: Array<Tube>? = null

    init {
        // listOf
        // mutableListOf

    }

    @Test
    fun tubes_isEmpty_performance() {
        val ITERATIONS = 100_000_000
        var count = 0
        tubes?.get(0)?.let {
            if (it.colorOfTopmostBall() == 7.toByte()) count++
        }

        tubes = arrayOf(
            Tube(8).apply { addBall(1) },
            Tube(8).apply { addBall(2) },
            Tube(8).apply { addBall(3) },
            Tube(8).apply { addBall(4) },
            Tube(8).apply { addBall(5) },
            Tube(8).apply { addBall(6) },
            Tube(8).apply { addBall(7) },
            Tube(8).apply { fillWithOneColor(8) },
            Tube(8).apply { fillWithOneColor(9) },
            Tube(8).apply { fillWithOneColor(10) },
            Tube(8).apply { fillWithOneColor(11) },
            Tube(8).apply { fillWithOneColor(13) },
            Tube(8).apply { fillWithOneColor(14) },
            Tube(8).apply { fillWithOneColor(15) }
        )

        for(repetition in 0 until ITERATIONS) {
            //for (t in tubes) {
            //for (i in tubes.indices) {
                tubes?.let {
                    for (i in 0 until it.size) {
                        //if(t.colorOfTopmostBall() == 7.toByte()) count++
                        if (it[i].colorOfTopmostBall() == 7.toByte()) count++
                    }
                }
        }
        assertEquals(ITERATIONS, count)
    }

    companion object {
        private const val TAG = "balla.CollectionsPerfTest"
    }
}