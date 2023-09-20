package de.heikozelt.ballakotlin2.model

import android.util.Log
import de.heikozelt.ballakotlin2.model.solver.StackSolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StackSolverTest {

    /**
     * Liefert die Lösung als Liste von Zügen oder null, falls es keine Lösung gibt
     *
     * Lösung 1a
     * 1 2 _    _ 2 _    2 _ _    2 _ 1
     * 2 1 _ => 2 1 1 => 2 1 1 => 2 _ 1
     *
     * Lösung 1b
     *
     * 1 2 _    _ 2 _    2 _ _    2 1 _
     * 2 1 _ => 2 1 1 => 2 1 1 => 2 1 _
     *                       ^ scheidet aus
     *
     * Lösung 2a
     * 1 2 _    1 _ _    _ 1 _    _ 1 2
     * 2 1 _ => 2 1 2 => 2 1 2 => _ 1 2
     *
     * Lösung 2b
     * 1 2 _    1 _ _    _ 1 _    2 1 _
     * 2 1 _ => 2 1 2 => 2 1 2 => 2 1 _
     * *                     ^ scheidet aus
     */
    @ExperimentalCoroutinesApi
    @Test
    fun findSolution_3_moves() {
        val gs = GameState().apply {
            resize(2, 1, 2)
            tubes[0].apply {
                addBall(1); addBall(2)
            }
            tubes[1].apply {
                addBall(2); addBall(1)
            }
        }
        //val solution1a = arrayOf(Move(0, 2), Move(1, 0), Move(1, 2))
        //val solution2a = arrayOf(Move(1, 2), Move(0, 1), Move(0, 2))

        var result = SearchResult()
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }

        //val solutionArray = solution?.toTypedArray()
        //val match1a = solution1a contentEquals solutionArray
        //val match2a = solution2a contentEquals solutionArray


        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(0, 2), result.move)
        //todo: weitere result-properties pruefen
    }

    @Test
    fun findSolution_4_1_5_found() {
        Log.d(TAG, "findSolution_big()")
        val txt = """
            _ 3 _ _ 4
            _ 2 1 1 2
            _ 1 2 2 3
            3 4 4 3 4
            1 1 4 2 3
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }

        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(1, 0), result.move)
    }

    @Test
    fun findSolution_6_1_5_found() {
        Log.d(TAG, "findSolution_big()")
        val txt = """
            3 _ 1 _ _ 5 2
            6 _ 2 4 _ 3 3
            6 4 2 3 4 1 6
            1 5 6 4 5 1 2
            5 4 6 5 3 1 2
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        Log.d(TAG, "result.move: ${result.move}")
        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        //beides richtig?
        Assertions.assertEquals(Move(1, 3), result.move)
        //assertEquals(Move(1, 4), result.move)
    }


    /**
     * einzig sinnvoller Zug ist 4 (a) -> 11 (auf a)
     */
    @Test
    fun findSolution_big() {
        Log.d(TAG, "findSolution_big()")
        val txt = """
        _ 7 _ 9 _ 5 8 d 1 _ 4 _ 2 c e _ _ b
        _ 7 3 9 _ 5 8 d 1 _ 4 _ 2 c e 6 f b
        _ 7 3 9 _ 5 8 d 1 _ 4 a 2 c e 6 f b
        _ 7 3 9 a 5 8 d 1 _ 4 a 2 c e 6 f b
        _ 7 3 9 a 5 8 d 1 _ 4 a 2 c e 6 f b
        _ 7 3 9 3 5 8 d 1 _ 4 a 2 c e 6 f b
        _ 7 3 9 6 5 8 d 1 _ 4 a 2 c e 6 f b
        _ 7 3 9 f 5 8 d 1 _ 4 a 2 c e 6 f b
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)

        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }

        when (result.status) {
            SearchResult.STATUS_FOUND_SOLUTION ->
                Log.d(TAG, "Lösung gefunden :-) ${result.move}")

            SearchResult.STATUS_UNSOLVABLE ->
                Log.d(TAG, "unlösbar :-(")

            SearchResult.STATUS_OPEN ->
                Log.d(
                    TAG,
                    "offen / maximale Zeit oder Rekursionstiefe überschritten :-("
                )

            else ->
                Log.e(TAG, "undefiniertes Ergebnis!")
        }
        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(4, 11), result.move)
    }

    /**
     * dead end with endless cycle
     * abwechselnd 3 hin, 5 hin, 3 her, 5 her, ...
     */
    @Test
    fun findSolution_cycle() {
        val txt = """
            _ _ _ _ _ _ 3 5 _
            _ 4 5 4 7 1 3 5 4
            3 2 5 2 6 1 7 6 2
            2 7 1 6 4 1 7 3 6
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)

        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        Assertions.assertEquals(SearchResult.STATUS_UNSOLVABLE, result.status)
    }

    /**
     * 2.470 ... 2.555 sec if (maxRecursionDepth > 5) yield()
     * 2.139 ... 2.363 sec if (maxRecursionDepth > 7) yield()
     * 1.319 ... 1.444 sec ohne zusätzliche Zykluserkennung
     */
    @Test
    fun findSolution_more_time_consuming_1() {
        val txt = """
            f 7 3 4 _ a _ e _ _ _ _ 2 _ 5 _ 7
            f 5 3 4 8 a _ e d 6 _ 9 1 _ b c 9
            f 1 3 4 8 a _ e 5 6 c 9 8 b d d 2
            f 1 3 4 8 a 2 e 5 6 c 9 c b 7 d 2
            f 1 3 4 8 a 1 e 5 6 c 9 7 b 7 d 2
            f 1 3 4 8 a 9 e 5 6 c 6 b b 7 d 2
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Log.d(TAG, "result.move: ${result.move}")
        //beides richtig?
        //assertEquals(Move(15, 10), result.move)
        Assertions.assertEquals(Move(12, 6), result.move)
    }

    /**
     * 3.166 ... 3.281 sec if (maxRecursionDepth > 5) yield()
     * 3.012 ... 3.155 sec if (maxRecursionDepth > 7) yield()
     * 1.244 ... 1.314 sec ohne zusätzliche Zykluserkennung
     */
    @Test
    fun findSolution_more_time_consuming_2() {
        val txt = """
            c _ _ e 1 b _ _ d _ _ d 3 e _ _
            c 6 9 e 1 b 7 8 2 _ _ d a e _ 2
            9 6 9 3 d b 7 4 1 _ _ 5 2 e _ 8
            5 6 9 8 3 b 7 c 1 8 4 2 a e 5 a
            d 6 9 a 3 b 7 c 1 8 4 2 5 e 5 a
            d 6 9 4 3 b 7 c 1 8 4 2 6 b 5 a
            d 6 9 7 3 4 7 c 1 8 4 2 3 c 5 a
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(7, 9), result.move)
    }

    @Test
    fun findSolution_really_time_consuming() {
        val txt = """
            9 4 _ b 2 6 _ _ 8 _ e _ _ _ 3 _ f _
            9 4 1 b 2 6 _ a 8 c e d _ _ 3 _ f _
            9 4 1 b 2 6 _ a 8 c e d _ 5 3 7 c _
            9 4 1 b 2 6 _ a 8 c e d _ 5 3 7 7 _
            9 4 1 b 2 6 _ a 8 c e d 5 5 1 f 7 f
            9 4 1 b 2 6 _ a 8 c e d 3 5 3 7 7 f
            9 4 1 b 2 6 _ a 8 c e d 3 5 a d 7 f
            9 4 1 b 2 6 _ a 8 c e d 3 5 f 5 7 f
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(12, 13), result.move)
    }

    /**
     * Duration:
     * 3.757 ... 4.132 sec with frequent yield() invocations
     * 2.945 ... 3.324 sec with if(maxRecursionDepth > 5) yield()
     * 2.744 ... 3.317 sec without yield()
     * 2.506 ... 3.100 sec recycling of SearchResult
     * 2.538 ... 3.036 sec allUsefulMoves() for(from in tubes.indices)
     * 2.419 ... 2.530 sec allUsefulMovesIntegrated() instead of allUsefulMoves() & contentDistinctMoves()
     * 1.839 ... 1.973 sec replacement move is not useful
     * 1.574 ....1.747 sec without counting open ends
     * 0.522 ... 0.782 sec allUsefulMovesIntegrated() mit Priorisierung: weiterer Ball in Quellröhre mit gleicher Farbe
     * 0.047 ... 0.061 sec isDifferentColoredOrUnicolorAndHighest()
     */
    @Test
    fun findSolution_time_consuming() {
        Log.d(TAG, "findSolution_time_consuming()")
        val txt = """
            a _ _ _ 3 b _ 7 _ 6 _ _ _ 4 _ 2 _ d
            a 1 5 _ 3 b d 7 f 6 _ _ e 4 8 2 _ e
            a 1 5 c 3 b d 7 f 6 _ _ e 4 8 2 _ 5
            a 1 5 c 3 b d 7 f 6 _ _ e 4 8 2 _ 1
            a 1 5 c 3 b d 7 f 6 _ 9 e 4 8 2 c 9
            a 1 5 c 3 b d 7 f 6 _ 9 e 4 8 2 c 9
            a 1 5 c 3 b d 7 f 6 _ 9 e 4 8 2 8 9
            a 1 5 c 3 b d 7 f 6 _ 9 e 4 8 2 f 9
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(16, 3), result.move)
    }

    /**
     * _ _ _ _ _ _
     * 1 2 _ _ _ _
     * 1 2 _ _ _ _
     * unloesbar, weil Ball von einfarbiger Röhre nicht zu leerer Röhre gezogen werden kann.
     */
    @ExperimentalCoroutinesApi
    @Test
    fun findSolution_unsolvable_1() {
        val gs = GameState().apply {
            resize(2, 4, 3)
            tubes[0].apply {
                addBall(1); addBall(1)
            }
            tubes[1].apply {
                addBall(2); addBall(2)
            }
        }

        var result = SearchResult()
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            // delay() und cancel() sind eigentlich unnötig, aber interessanter Testfall
            delay(100L)
            job.cancel()
            job.join()
        }

        Assertions.assertEquals(SearchResult.STATUS_UNSOLVABLE, result.status)
        Assertions.assertEquals(null, result.move)
        //todo: weitere result-properties pruefen
    }

    @ExperimentalCoroutinesApi
    @Test
    fun findSolution_unsolvable_2() {
        val txt = """
            5 _ 7 _ _ 2 _ _
            5 5 1 1 6 7 3 4
            7 5 2 3 6 5 2 4
            7 6 2 4 6 3 1 4
            7 3 2 1 6 3 1 4
        """.trimIndent()
        var result = SearchResult()
        val gs = GameState()
        gs.fromAscii(txt)
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }

        Assertions.assertEquals(SearchResult.STATUS_UNSOLVABLE, result.status)
        Assertions.assertEquals(null, result.move)
        //todo: weitere result-properties pruefen
    }

    /**
     * Die einfachste Lösung besteht aus null Zügen. Das Puzzle ist bereits gelöst.
     * Die zweit-einfachste Lösung besteht aus einem einzigen Zug.
     * 2 _ _    2 1 _
     * 2 1 _    2 1 _
     * 2 1 1 => 2 1 _
     */
    @ExperimentalCoroutinesApi
    @Test
    fun findSolutionNoBackAndForth_recursionDepth_0_open() {
        val gs = GameState().apply {
            resize(2, 1, 3)
            tubes[0].apply {
                addBall(2); addBall(2); addBall(2)
            }
            tubes[1].apply {
                addBall(1); addBall(1)
            }
            tubes[2].addBall(1)
        }

        val result = SearchResult()
        runTest {
            val job: Job = GlobalScope.launch(Dispatchers.Default) {
                val previousGameStates = HashSet<SpecialArray>()
                StackSolver().findSolutionNoBackAndForth(gs, 0, result, previousGameStates)
            }
            job.join()
        }

        Assertions.assertEquals(SearchResult.STATUS_OPEN, result.status)
        Assertions.assertEquals(null, result.move)
        //todo: weitere result-properties pruefen
    }

    /**
     * Die einfachste Lösung besteht aus einem einzigen Zug.
     * 2 _ _    1 1 _
     * 2 1 _    2 1 _
     * 2 1 1 => 2 1 _
     */
    @Test
    fun findSolutionNoBackAndForth_recursionDepth_1_found() {
        val gs = GameState().apply {
            resize(2, 1, 3)
            tubes[0].apply {
                addBall(2); addBall(2); addBall(2)
            }
            tubes[1].apply {
                addBall(1); addBall(1)
            }
            tubes[2].addBall(1)
        }
        // es interessiert nur der nächste Zug, nicht alle Züge zur Lösung
        //val expectedSolution = arrayOf(Move(2, 1))

        val result = SearchResult()
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                val previousGameStates = HashSet<SpecialArray>()
                StackSolver().findSolutionNoBackAndForth(gs, 1, result, previousGameStates)
            }
            job.join()
        }

        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(2, 1), result.move)
        //todo: weitere result-properties pruefen
    }

    companion object {
        private const val TAG = "balla.StackSolverTest"
    }
}