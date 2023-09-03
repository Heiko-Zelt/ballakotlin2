package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
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


    companion object {
        private const val TAG = "balla.BreadthFirstSearchSolverTest"
    }
}