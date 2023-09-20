package de.heikozelt.ballakotlin2.model

import android.util.Log
import de.heikozelt.ballakotlin2.model.solver.BreadthFirstSearchSolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BreadthFirstSearchSolverTest {

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
                addBall(2); addBall(1)
            }
            tubes[1].apply {
                addBall(1); addBall(2)
            }
        }

        var result = SearchResult()
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = BreadthFirstSearchSolver().findSolution(gs)
            }
            job.join()
        }

        //val solutionArray = solution?.toTypedArray()
        //val match1a = solution1a contentEquals solutionArray
        //val match2a = solution2a contentEquals solutionArray

        Log.d(TAG, "result.move: ${result.move}")
        Assertions.assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Assertions.assertEquals(Move(0, 2), result.move)
        //todo: weitere result-properties pruefen
    }

    @ExperimentalCoroutinesApi
    @Test
    fun findSolution_already_solved() {
        val gs = GameState().apply {
            resize(2, 1, 2)
            tubes[0].apply {
                addBall(2); addBall(2)
            }
            tubes[1].apply {
                addBall(1); addBall(1)
            }
        }

        var result = SearchResult()
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                result = BreadthFirstSearchSolver().findSolution(gs)
            }
            job.join()
        }

        Assertions.assertEquals(SearchResult.STATUS_ALREADY_SOLVED, result.status)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cancel_job() {
        val boardAscii = """
          _ 9 _ _ _ _ _ _ _ e _ _ _ _ _ 9 8 _
          3 9 1 1 _ _ a e _ e _ b 2 _ _ c 8 7
          1 a e 4 _ 5 1 d _ 3 _ b 7 f d c 8 7
          8 c 7 5 6 4 2 6 _ 4 1 b e f d c 8 7
          3 c d a 6 8 2 e 3 4 1 b 2 f 3 e 8 9
          d c a a 6 5 2 7 3 4 8 b 5 f 6 2 1 9
          d c a b 6 9 2 7 3 4 f b 4 f d 5 6 9
          d c a 5 6 5 2 7 3 4 1 b a f e 5 f 9
        """.trimIndent()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        var result = SearchResult()
        val solver = BreadthFirstSearchSolver()
        // cancel even before it started, so there is no run condition
        solver.cancelJob()
        runTest {
            val job = GlobalScope.launch(Dispatchers.Default) {
                Log.d(TAG, "start job")
                result = solver.findSolution(gs)
                Log.d(TAG, "finished job")
            }
            job.join()
        }
        assertEquals(SearchResult.STATUS_CANCELED, result.status)
    }

    companion object {
        private const val TAG = "balla.BreadthFirstSearchSolverTest"
    }
}