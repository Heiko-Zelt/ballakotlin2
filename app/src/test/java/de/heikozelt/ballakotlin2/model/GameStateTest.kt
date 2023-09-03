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
 *
 * tubes                                 MutableList          Array
 * findSolution_6_1_5_found()            0.048 ... 0.055 sec  0.046 ... 0.057 sec
 * findSolution_more_time_consuming_1()  0.673 ... 0.796 sec  0.545 ... 0.681 sec
 * findSolution_more_time_consuming_2()  1.142 ... 1.149 sec  0.895 ... 1.051 sec
 * findSolution_really_time_consuming()  1.770 ... 1.964 sec  1.597 ... 1.791 sec
 * Fazit: winzige Verbesserung
 */
class GameStateTest {
    /**
     * _ _    _ _
     * 1 _ => _ 1
     * Einzig möglicher Zug wäre, letzten Zug retour.
     * Es ist also kein sinnvoller Zug möglich.
     */
    @Test
    fun allPossibleBackwardMoves_back_and_forth() {
        val moves = GameState().run {
            resize(1, 1, 2)
            tubes[0].addBall(1)
            moveBallAndLog(Move(0, 1))
            allPossibleBackwardMoves()
        }
        assertEquals(0, moves.size)
    }

    /**
     * 1 _    _ _
     * 1 _ => 1 1
     */
    @Test
    fun allPossibleBackwardMoves_full_to_empty() {
        val moves = GameState().run {
            resize(1, 1, 2)
            rainbow()
            allPossibleBackwardMoves()
        }
        assertEquals(1, moves.size)
        assertEquals(0, moves[0].from)
        assertEquals(1, moves[0].to)
    }

    /**
     * _ _
     * 1 _ => Es ist kein sinnvoller Zug möglich.
     */
    @Test
    fun allPossibleBackwardMoves_ground_move() {
        val moves = GameState().run {
            resize(1, 1, 2)
            tubes[0].addBall(1)
            allPossibleBackwardMoves()
        }
        assertEquals(0, moves.size)
    }

    @Test
    fun allUsefulMoves_1x2dup_one() {
        val boardAscii = """
        1 _ _
        1 2 2
        """.trimIndent()
        val expected = arrayOf(Move(1, 2))
        GameState().apply {
            fromAscii(boardAscii)
            val u = allUsefulMoves()
            val uArray = u.toTypedArray()
            assertTrue(expected contentEquals uArray)
        }
    }

    @Test
    fun allUsefulMoves_1x3dup_one() {
        val boardAscii = """
        1 _ _ _
        1 2 2 2
        """.trimIndent()
        val expected = arrayOf(Move(1, 2))
        GameState().apply {
            fromAscii(boardAscii)
            val u = allUsefulMoves()
            val uArray = u.toTypedArray()
            assertTrue(expected contentEquals uArray)
        }
    }

    @Test
    fun allUsefulMoves_2x2dup_one() {
        val boardAscii = """
        1 1 _ _
        2 2 _ _
        """.trimIndent()
        val expected = arrayOf(Move(0, 2))
        GameState().apply {
            fromAscii(boardAscii)
            val u = allUsefulMoves()
            val uArray = u.toTypedArray()
            assertTrue(expected contentEquals uArray)
        }
    }

    /**
     * Beispiel aus der Praxis
     */
    @Test
    fun allUsefulMoves_big() {
        Log.d(TAG, "usefulMoves_big()")
        val boardAscii = """
            _ 7 _ 9 _ 5 8 d 1 _ 4 _ 2 c e _ _ b
            _ 7 3 9 _ 5 8 d 1 _ 4 _ 2 c e 6 f b
            _ 7 3 9 _ 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 a 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 a 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 3 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 6 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 f 5 8 d 1 _ 4 a 2 c e 6 f b
            """.trimIndent()
        val expectedAscii = """4->11"""
        GameState().apply {
            fromAscii(boardAscii)
            val u = allUsefulMoves()
            val result = Moves()
            result.fromList(u)
            assertEquals(expectedAscii, result.toAscii())
        }
    }

    /**
     * Beispiel aus der Praxis
     */
    @Test
    fun allUsefulMovesIntegrated_big() {
        Log.d(TAG, "usefulMovesIntegrated_big()")
        val boardAscii = """
            _ 7 _ 9 _ 5 8 d 1 _ 4 _ 2 c e _ _ b
            _ 7 3 9 _ 5 8 d 1 _ 4 _ 2 c e 6 f b
            _ 7 3 9 _ 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 a 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 a 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 3 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 6 5 8 d 1 _ 4 a 2 c e 6 f b
            _ 7 3 9 f 5 8 d 1 _ 4 a 2 c e 6 f b
            """.trimIndent()
        val expectedAscii = """4->11"""
        GameState().apply {
            fromAscii(boardAscii)
            val u = allUsefulMovesIntegrated()
            val result = Moves()
            result.fromList(u)
            assertEquals(expectedAscii, result.toAscii())
        }
    }

    /**
     * Es gibt noch einen gleichfarbigen Ball in der Quell-Röhre.
     * Den als nächstes wegziehen.
     */
    @Test
    fun allUsefulMovesIntegrated_group() {
        Log.d(TAG, "usefulMovesIntegrated_big()")
        val boardAscii = """
            _ 7 _ 9 _ 5 8 1 _ 4 _ 2 _ a
            _ 7 3 9 _ 5 8 1 _ 4 _ 2 6 b
            _ 7 3 9 _ 5 8 1 _ 4 _ 2 6 b
            _ 7 3 9 a 5 8 1 _ 4 a 2 6 b
            _ 7 3 9 a 5 8 1 _ 4 b 2 6 b
            a 7 3 9 3 5 8 1 _ 4 a 2 6 b
            """.trimIndent()
        val expectedAscii = """4->0, 4->10"""
        GameState().apply {
            fromAscii(boardAscii)
            moveBallAndLog(Move(4, 10))
            val u = allUsefulMovesIntegrated()
            val result = Moves()
            result.fromList(u)
            assertEquals(expectedAscii, result.toAscii())
        }
    }


    @Test
    fun allUsefulMoves_empty() {
        val gs = GameState().apply {
            resize(1, 1, 2)
            tubes[0].addBall(1)
        }
        assertEquals(emptyList<Move>(), gs.allUsefulMoves())
    }

    /**
     * 2 _ _    _ _ _    _ _ _
     * 2 _ _ => 2 _ _ => _ _ 2
     * 1 1 _    1 1 2    1 1 2
     */
    @Test
    fun allUsefulMoves_no_back_and_forth() {
        val gs = GameState().apply {
            resize(2, 1, 3)
            tubes[0].addBall(1)
            tubes[0].addBall(2)
            tubes[0].addBall(2)
            tubes[1].addBall(1)
            moveBallAndLog(Move(0, 2))
        }
        val moves = listOf(Move(0, 2))
        assertEquals(moves, gs.allUsefulMoves())
    }

    @Test
    fun allUsefulMoves_only_two() {
        val boardAscii = """
            a _ 5 c 3 b d 7 f 6 _ _ e 4 8 _ _ 9
            a _ 5 c 3 b d 7 f 6 _ _ e 4 8 1 _ 9
            a _ 5 c 3 b d 7 f 6 _ _ e 4 8 1 2 9
            a _ 5 c 3 b d 7 f 6 _ _ e 4 8 1 2 9
            a _ 5 c 3 b d 7 f 6 _ _ e 4 8 1 2 9
            a 5 5 c 3 b d 7 f 6 _ _ e 4 8 1 2 9
            a 1 2 c 3 b d 7 f 6 _ _ e 4 8 1 2 9
            a 2 5 c 3 b d 7 f 6 _ _ e 4 8 1 2 9
        """.trimIndent()
        val expectedMoves = listOf(Move(1, 10), Move(2, 10))
        GameState().apply {
            fromAscii(boardAscii)
            val moves = allUsefulMoves()
            assertEquals(expectedMoves, moves)
        }
    }

    /**
     * todo: nicht 2 -> 0, wegen einfarbig
     */
    @Test
    fun allUsefulMoves_smaller() {
        val boardAscii = """
            _ 7 _ 9 _ _ 4 _ _
            _ 7 3 9 _ _ 4 _ 6
            _ 7 3 9 a _ 4 a 6
            _ 7 3 9 a _ 4 a 6
            """.trimIndent()
        val expectedAscii = """4->7"""
        GameState().apply {
            fromAscii(boardAscii)
            val u = allUsefulMoves()
            val result = Moves()
            result.fromList(u)
            assertEquals(expectedAscii, result.toAscii())
        }
    }

    /**
     * _ _ _    _ _ _      2 _ _
     * 2 _ _ => _ _ 2 oder 2 _ _
     * 1 1 2    1 1 2      1 _ _
     */
    @Test
    fun allUsefulMoves_two() {
        val gs = GameState().apply {
            resize(2, 1, 3)
            tubes[0].apply {
                addBall(1); addBall(2)
            }
            tubes[1].addBall(1)
            tubes[2].addBall(2)

        }
        val moves = listOf(Move(0, 2), Move(2, 0))
        assertEquals(moves, gs.allUsefulMoves())
    }

    @Test
    fun applyMoves_1() {
        val expectedAscii = """
            _ 2 3 _
            _ 2 3 1
            1 2 3 1
        """.trimIndent()
        val moves = Moves().apply {
            push(Move(0, 3))
            push(Move(0, 3))
        }
        GameState().apply {
            resize(3, 1, 3)
            rainbow()
            applyMoves(moves)
            assertEquals(expectedAscii, toAscii())
        }
    }

    @Test
    fun applyMoves_2() {
        val expectedAscii = """
            _ 2 3 _
            _ 2 3 1
            1 2 3 1
        """.trimIndent()
        val moves = """0 -> 3, 0 -> 3"""
        GameState().apply {
            resize(3, 1, 3)
            rainbow()
            applyMoves(moves)
            assertEquals(expectedAscii, toAscii())
        }
    }

    @Test
    fun cheat_1() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            cheat()
            assertEquals(3, numberOfTubes)
            assertEquals(3, tubes.size)
            assertEquals(1, moveLog.size())
            assertEquals(Move(99, 0), moveLog.last())
        }
    }

    @Test
    fun constructor_1() {
        GameState().apply {
            resize(2, 1, 3)
            assertEquals(2, numberOfColors)
            assertEquals(1, numberOfExtraTubes)
            assertEquals(3, tubeHeight)
            assertEquals(3, numberOfTubes)
            assertEquals(0, tubes[0].fillLevel)
            assertEquals(0, tubes[1].fillLevel)
            assertEquals(0, tubes[2].fillLevel)
        }
    }

    @Test
    fun contentDistinctMoves_1of2() {
        val boardAscii = """
        1 _ _
        1 2 2
        """.trimIndent()
        val moves = mutableListOf(Move(1, 2), Move(2, 1))
        val expected = arrayOf(Move(1, 2))
        GameState().apply {
            fromAscii(boardAscii)
            val distinct = contentDistinctMoves(moves)
            val dArray = distinct.toTypedArray()
            assertTrue(expected contentEquals dArray)
        }
    }

    @Test
    fun contentDistinctMoves_big() {
        val boardAscii = """
            _ 7 _ 9 _ _ 4 _ _
            _ 7 3 9 _ _ 4 _ 6
            _ 7 3 9 a _ 4 a 6
            _ 7 3 9 a _ 4 a 6
            """.trimIndent()
        val expected = """2->0, 4->0, 4->7, 8->0"""
        val movesAscii = """2->0, 2->5, 4->0, 4->5, 4->7, 8->0, 8->5"""
        val moves = Moves()
        moves.fromAscii(movesAscii)
        GameState().apply {
            fromAscii(boardAscii)
            val distinct = contentDistinctMoves(moves.asMutableList())
            val result = Moves()
            result.fromList(distinct)
            Log.d(TAG, "result=${result.toAscii()}")
            assertEquals(expected, result.toAscii())
        }
    }

    @Test
    fun contentDistinctMovesBang_big() {
        val boardAscii = """
            _ 7 _ 9 _ _ 4 _ _
            _ 7 3 9 _ _ 4 _ 6
            _ 7 3 9 a _ 4 a 6
            _ 7 3 9 a _ 4 a 6
            """.trimIndent()
        val expectedAscii = """2->0, 4->0, 4->7, 8->0"""
        val movesAscii = """2->0, 2->5, 4->0, 4->5, 4->7, 8->0, 8->5"""
        val moves = Moves()
        val movesList = moves.asMutableList()
        moves.fromAscii(movesAscii)
        GameState().apply {
            fromAscii(boardAscii)
            contentDistinctMovesBang(movesList)
            val result = Moves()
            result.fromList(movesList)
            Log.d(TAG, "result=${result.toAscii()}")
            assertEquals(expectedAscii, result.toAscii())
        }
    }

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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }

        //val solutionArray = solution?.toTypedArray()
        //val match1a = solution1a contentEquals solutionArray
        //val match2a = solution2a contentEquals solutionArray


        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(0, 2), result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }

        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(1, 0), result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        Log.d(TAG, "result.move: ${result.move}")
        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        //beides richtig?
        assertEquals(Move(1, 3), result.move)
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
            val job = GlobalScope.launch(Default) {
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
        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(4, 11), result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        assertEquals(SearchResult.STATUS_UNSOLVABLE, result.status)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        Log.d(TAG, "result.move: ${result.move}")
        //beides richtig?
        //assertEquals(Move(15, 10), result.move)
        assertEquals(Move(12, 6), result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(7, 9), result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(12, 13), result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }
        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(16, 3), result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            // delay() und cancel() sind eigentlich unnötig, aber interessanter Testfall
            delay(100L)
            job.cancel()
            job.join()
        }

        assertEquals(SearchResult.STATUS_UNSOLVABLE, result.status)
        assertEquals(null, result.move)
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
            val job = GlobalScope.launch(Default) {
                result = StackSolver().findSolution(gs)
            }
            job.join()
        }

        assertEquals(SearchResult.STATUS_UNSOLVABLE, result.status)
        assertEquals(null, result.move)
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
            val job: Job = GlobalScope.launch(Default) {
                val previousGameStates = HashSet<SpecialArray>()
                StackSolver().findSolutionNoBackAndForth(gs, 0, result, previousGameStates)
            }
            job.join()
        }

        assertEquals(SearchResult.STATUS_OPEN, result.status)
        assertEquals(null, result.move)
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
            val job = GlobalScope.launch(Default) {
                val previousGameStates = HashSet<SpecialArray>()
                StackSolver().findSolutionNoBackAndForth(gs, 1, result, previousGameStates)
            }
            job.join()
        }

        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(2, 1), result.move)
        //todo: weitere result-properties pruefen
    }

    @Test
    fun fromAscii_additional_spaces() {
        val lines = """
            | _ 5 5 _ _ 1 _ 3 6
            |_ 4 7   _ _ 2 _ 6 5
            |3 7 2 3 2 6 2 4 7
            |1 4 3 4 5 6 7 1 1
            """.trimMargin()
        val gs = GameState()
        gs.fromAscii(lines)
        assertEquals(9, gs.numberOfTubes)
        assertEquals(7, gs.numberOfColors)
        assertEquals(2, gs.numberOfExtraTubes)
        assertEquals(4, gs.tubeHeight)
        gs.tubes[0].apply {
            assertEquals(0, cells[3])
            assertEquals(0, cells[2])
            assertEquals(3, cells[1])
            assertEquals(1, cells[0])
        }
        gs.tubes[8].apply {
            assertEquals(6, cells[3])
            assertEquals(5, cells[2])
            assertEquals(7, cells[1])
            assertEquals(1, cells[0])
        }
    }

    @Test
    fun fromAscii_alternativeNewlineCharacter() {
        val lines = """ _ 5 5 _ _ 1 _ 3 6|_ 4 7   _ _ 2 _ 6 5|3 7 2 3 2 6 2 4 7|1 4 3 4 5 6 7 1 1"""
        val gs = GameState()
        gs.fromAscii(lines, "|")
        assertEquals(9, gs.numberOfTubes)
        assertEquals(7, gs.numberOfColors)
        assertEquals(2, gs.numberOfExtraTubes)
        assertEquals(4, gs.tubeHeight)
        gs.tubes[0].apply {
            assertEquals(0, cells[3])
            assertEquals(0, cells[2])
            assertEquals(3, cells[1])
            assertEquals(1, cells[0])
        }
        gs.tubes[8].apply {
            assertEquals(6, cells[3])
            assertEquals(5, cells[2])
            assertEquals(7, cells[1])
            assertEquals(1, cells[0])
        }
    }

    // todo: Lücken bei den Zahlen testen
    @Test
    fun fromAsciiLines_no_spaces() {
        val lines = arrayOf(
            "_2_",
            "12_",
            "121"
        )
        val gs = GameState()
        gs.fromAsciiLines(lines)
        assertEquals(3, gs.numberOfTubes)
        assertEquals(2, gs.numberOfColors)
        assertEquals(1, gs.numberOfExtraTubes)
        assertEquals(3, gs.tubeHeight)
        gs.tubes[0].apply {
            assertEquals(0, cells[2])
            assertEquals(1, cells[1])
            assertEquals(1, cells[0])
        }
        gs.tubes[2].apply {
            assertEquals(0, cells[2])
            assertEquals(0, cells[1])
            assertEquals(1, cells[0])
        }
    }

    @Test
    fun fromAsciiLines_with_spaces() {
        val lines = arrayOf(
            "_ 5 5 _ _ 1 _ 3 6",
            "_ 4 7 _ _ 2 _ 6 5",
            "3 7 2 3 2 6 2 4 7",
            "1 4 3 4 5 6 7 1 1"
        )
        val gs = GameState()
        gs.fromAsciiLines(lines)
        assertEquals(9, gs.numberOfTubes)
        assertEquals(7, gs.numberOfColors)
        assertEquals(4, gs.tubeHeight)
        gs.tubes[0].apply {
            assertEquals(0, cells[3])
            assertEquals(0, cells[2])
            assertEquals(3, cells[1])
            assertEquals(1, cells[0])
        }
        gs.tubes[8].apply {
            assertEquals(6, cells[3])
            assertEquals(5, cells[2])
            assertEquals(7, cells[1])
            assertEquals(1, cells[0])
        }
    }

    @Test
    fun toBytesNormalized() {
        val lines = arrayOf(
            "4 2 _ 3 3",
            "3 2 _ 4 2",
            "1 4 _ 1 1"
        )
        // sortiert:
        // _ 3 4 3 2
        // _ 2 3 4 2
        // _ 1 1 1 4
        val gs = GameState()
        gs.fromAsciiLines(lines)
        val bytes = gs.toBytesNormalized()
        val expected = arrayOf(
            (0 + (0 shl 4)).toByte(),
            (0 + (1 shl 4)).toByte(),
            (2 + (3 shl 4)).toByte(),
            (1 + (3 shl 4)).toByte(),
            (4 + (1 shl 4)).toByte(),
            (4 + (3 shl 4)).toByte(),
            (4 + (2 shl 4)).toByte(),
            2.toByte()
        )
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun fromBytes() {
        val bytes = arrayOf(
            (0 + (0 shl 4)).toByte(),
            (0 + (1 shl 4)).toByte(),
            (2 + (3 shl 4)).toByte(),
            (1 + (3 shl 4)).toByte(),
            (4 + (1 shl 4)).toByte(),
            (4 + (3 shl 4)).toByte(),
            (4 + (2 shl 4)).toByte(),
            2.toByte()
        )
        val gs = GameState()
        gs.resize(4, 1, 3)
        gs.fromBytes(bytes)
        assertArrayEquals(arrayOf(0.toByte(), 0.toByte(), 0.toByte()), gs.tubes[0].cells)
        assertArrayEquals(arrayOf(1.toByte(), 2.toByte(), 3.toByte()), gs.tubes[1].cells)
        assertArrayEquals(arrayOf(1.toByte(), 3.toByte(), 4.toByte()), gs.tubes[2].cells)
        assertArrayEquals(arrayOf(1.toByte(), 4.toByte(), 3.toByte()), gs.tubes[3].cells)
        assertArrayEquals(arrayOf(4.toByte(), 2.toByte(), 2.toByte()), gs.tubes[4].cells)
    }

    @Test
    fun toBytesNormalized_equals() {
        val lines1 = arrayOf(
            "_ 2 _ 3 3",
            "3 2 _ 4 2",
            "1 4 _ 1 1"
        )
        val lines2 = arrayOf(
            "3 _ 2 _ 3",
            "2 3 2 _ 4",
            "1 1 4 _ 1"
        )
        val gs1 = GameState()
        val gs2 = GameState()
        gs1.fromAsciiLines(lines1)
        gs2.fromAsciiLines(lines2)
        val bytes1 = gs1.toBytesNormalized()
        Log.d(TAG, "bytes1: $bytes1")
        val bytes2 = gs2.toBytesNormalized()
        Log.d(TAG, "bytes2: $bytes2")
        assertArrayEquals(bytes1, bytes2)
        Log.d(TAG, "bytes1.hashCode=${bytes1.hashCode()}")
        Log.d(TAG, "bytes2.hashCode=${bytes2.hashCode()}")
        //val hash = SHA1.Create().ComputeHash(bytes1);
        val set = hashSetOf(SpecialArray(bytes1))
        assertTrue(SpecialArray(bytes2) in set)
    }

    @Test
    fun isDifferentColoredOrUnicolorAndHighest_1() {
        val lines = arrayOf(
            "_ _ 4 _ _ 3 _ _",
            "_ _ 4 2 _ 2 1 _",
            "_ _ 2 4 _ 3 1 _",
            "1 _ 2 3 _ 3 1 4"
        )
        GameState().apply {
            fromAsciiLines(lines)
            assertFalse(isDifferentColoredOrUnicolorAndHighest(0, 1)) // Spalte 6 ist höher
            assertTrue(isDifferentColoredOrUnicolorAndHighest(0, 2)) // egal, geht eh nicht
            assertFalse(isDifferentColoredOrUnicolorAndHighest(1, 1)) // Spalte 6 ist höher
            assertTrue(
                isDifferentColoredOrUnicolorAndHighest(
                    1,
                    2
                )
            ) // ja, es gibt keine einfarbige Spalte mit Farbe 2
            assertTrue(isDifferentColoredOrUnicolorAndHighest(2, 2)) // egal, geht eh nicht
            assertTrue(isDifferentColoredOrUnicolorAndHighest(3, 1)) // egal, geht eh nicht
            assertTrue(isDifferentColoredOrUnicolorAndHighest(3, 2)) // unterschiedliche Farben
            assertTrue(
                isDifferentColoredOrUnicolorAndHighest(
                    6,
                    1
                )
            ) // einfarbig und höchster Stapel
        }
    }

    @Test
    fun isMoveAllowed_to_empty_tube_true() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            assertTrue(isMoveAllowed(0, 1))
        }
    }

    @Test
    fun isMoveAllowed_to_full_tube_wrong() {
        GameState().apply {
            resize(2, 1, 3)
            rainbow()
            assertFalse(isMoveAllowed(0, 1))
        }
    }

    @Test
    fun isMoveAllowed_to_same_color_true() {
        GameState().apply {
            resize(2, 1, 3)
            rainbow()
            tubes[2].addBall(1)
            assertTrue(isMoveAllowed(0, 2))
        }
    }

    @Test
    fun isMoveAllowed_to_same_tube_true() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            assertTrue(isMoveAllowed(0, 0))
        }
    }

    /**
     * <pre>
     * 1 _ _ _
     * 2 _ _ 2
     * 3 1 1 2
     * 2 3 1 3
     * 2 3 1 3
     * </pre>
     * 0 -> 1 gut, es ist nur einzelner
     * 0 -> 2 gut, es ist nur einzelner
     * 1 -> 2 gut, es ist nur einzelner
     * 2 -> 1 schlecht, es können nicht alle raus
     */
    @Test
    fun isMoveUseful_all_or_none() {
        val boardAscii = """
          1 _ _ _
          2 _ _ 2
          3 1 1 2
          2 3 1 3
          2 3 1 3
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            assertFalse(isMoveUseful(0, 0)) // Unsinn
            assertTrue(isMoveUseful(0, 1)) // gut
            assertTrue(isMoveUseful(0, 2)) // gut
            assertFalse(isMoveUseful(0, 3)) // geht nicht
            assertFalse(isMoveUseful(1, 0)) // geht nicht
            assertFalse(isMoveUseful(1, 1)) // Unsinn
            assertTrue(isMoveUseful(1, 2)) // gut
            assertFalse(isMoveUseful(1, 3)) // geht nicht
            assertFalse(isMoveUseful(2, 0)) // geht nicht
            assertFalse(isMoveUseful(2, 1)) // <--- es können nicht alle raus
            assertFalse(isMoveUseful(2, 2)) // Unsinn
            assertFalse(isMoveUseful(2, 3)) // geht nicht
            assertFalse(isMoveUseful(3, 0)) // geht nicht
            assertFalse(isMoveUseful(3, 1)) // geht nicht
            assertFalse(isMoveUseful(3, 2)) // geht nicht
            assertFalse(isMoveUseful(3, 3)) // Unsinn
        }
    }

    /**
     * <pre>
     * chain of 2 moves
     * _ _ _    _ _ _    _ _ 1
     * 1 _ 1 => _ 1 1 => _ _ 1
     * 2 1 3    2 1 3    2 1 3
     * ^ ^ ok     ^ ^ chain
     * </pre>
     * is equivalent but detour to one direct move:
     * _ _ _    _ _ 1
     * 1 _ 1 => _ _ 1
     * 2 1 3    2 1 3
     */
    @Test
    fun isMoveUseful_chain() {
        val boardAscii = """
            _ _ _
            1 _ 1
            2 1 3
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            moveBallAndLog(Move(0, 1))
            assertFalse(isMoveUseful(0, 0)) // Unsinn
            assertFalse(isMoveUseful(0, 1)) // geht nicht
            assertFalse(isMoveUseful(0, 2)) // geht nicht
            assertFalse(isMoveUseful(1, 0)) // geht nicht
            assertFalse(isMoveUseful(1, 1)) // Unsinn
            assertFalse(isMoveUseful(1, 2)) // chain <----
            assertFalse(isMoveUseful(2, 0)) // geht nicht
            assertTrue(isMoveUseful(2, 1)) // gut
            assertFalse(isMoveUseful(2, 2)) // Unsinn
        }
    }

    /**
     * <pre>
     * chain of 2 moves
     * _ _ _    _ _ 1    _ _ 1
     * 1 _ 1 => 1 _ 1 => _ _ 1
     * 2 1 3    2 _ 3    2 1 3
     *   ^ ^ ok ^ ^ replacement
     * </pre>
     * is equivalent but detour to one direct move:
     * _ _ _    _ _ 1
     * 1 _ 1 => _ _ 1
     * 2 1 3    2 1 3
     */
    @Test
    fun isMoveUseful_replacement() {
        val boardAscii = """
            _ _ _
            1 _ 1
            2 1 3
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            moveBallAndLog(Move(1, 2))
            assertFalse(isMoveUseful(0, 0)) // Unsinn
            assertFalse(isMoveUseful(0, 1)) // replacement <----
            assertFalse(isMoveUseful(0, 2)) // voll, geht nicht
            assertFalse(isMoveUseful(1, 0)) // leer, geht nicht
            assertFalse(isMoveUseful(1, 1)) // Unsinn
            assertFalse(isMoveUseful(1, 2)) // leer + voll, geht nicht
            assertFalse(isMoveUseful(2, 0)) // Farbe, geht nicht
            assertFalse(isMoveUseful(2, 1)) // hin und zureuck
            assertFalse(isMoveUseful(2, 2)) // Unsinn
        }
    }

    /**
     * <pre>
     * _ _    _ _
     * 1 _ => _ 1
     * 1 1    1 1 sinnlos
     * </pre>
     *
     * <pre>
     * _ _    1 _
     * 1 _ => 1 _
     * 1 1    1 _ gut
     * </pre>
     */
    @Test
    fun isMoveUseful_from_unicolor_to_unicolor() {
        val boardAscii = """
            _ _
            1 _
            1 1
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            assertFalse(isMoveUseful(0, 0)) // Unsinn
            assertFalse(isMoveUseful(0, 1)) // bringt nix
            assertTrue(isMoveUseful(1, 0)) // sehr gut
            assertFalse(isMoveUseful(1, 1)) // Unsinn
        }
    }

    /**
     * nur ein einziger sinnvoller Zug möglich
     * 2 _ _    _ _ _    _ _ _
     * 2 _ _ => 2 _ _ => _ _ 2
     * 1 1 _    1 1 2    1 1 2
     */
    @Test
    fun isMoveUseful_one() {
        GameState().apply {
            resize(2, 1, 3)
            tubes[0].addBall(1)
            tubes[0].addBall(2)
            tubes[0].addBall(2)
            tubes[1].addBall(1)
            moveBallAndLog(Move(0, 2))
            assertFalse(isMoveUseful(0, 0)) // selbe Roehre
            assertFalse(isMoveUseful(0, 1)) // falsche Farbe
            assertTrue(isMoveUseful(0, 2)) // einzig sinnvoller Zug
            assertFalse(isMoveUseful(1, 0)) // falsche Farbe
            assertFalse(isMoveUseful(1, 1)) // selbe Roehre
            assertFalse(isMoveUseful(1, 2)) // falsche Farbe
            assertFalse(isMoveUseful(2, 0)) // zurueck
            assertFalse(isMoveUseful(2, 1)) // falsche Farbe
            assertFalse(isMoveUseful(2, 2)) // selbe Roehre
        }
    }

    /**
     * zwei Züge sinnvolle Züge möglich
     * _ _ _    _ _ _      2 _ _
     * 2 _ _ => _ _ 2 oder 2 _ _
     * 1 1 2    1 1 2      1 _ _
     */
    @Test
    fun isMoveUseful_two() {
        GameState().apply {
            resize(2, 1, 3)
            tubes[0].addBall(1)
            tubes[0].addBall(2)
            tubes[1].addBall(1)
            tubes[2].addBall(2)
            assertFalse(isMoveUseful(0, 0)) // selbe Roehre
            assertFalse(isMoveUseful(0, 1)) // falsche Farbe
            assertTrue(isMoveUseful(0, 2)) // sinnvoller Zug
            assertFalse(isMoveUseful(1, 0)) // falsche Farbe
            assertFalse(isMoveUseful(1, 1)) // selbe Roehre
            assertFalse(isMoveUseful(1, 2)) // falsche Farbe
            assertTrue(isMoveUseful(2, 0)) // sinnvoller Zug
            assertFalse(isMoveUseful(2, 1)) // falsche Farbe
            assertFalse(isMoveUseful(2, 2)) // selbe Roehre
        }
    }

    @Test
    fun isMoveUseful_unicolor_1() {
        val boardAscii = """
            _ _
            1 _
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            assertFalse(isMoveUseful(0, 0)) // Unsinn
            assertFalse(isMoveUseful(0, 1)) // bringt nix
            assertFalse(isMoveUseful(1, 0)) // geht nicht
            assertFalse(isMoveUseful(1, 1)) // Unsinn
        }
    }

    @Test
    fun isMoveUseful_unicolor_2() {
        val boardAscii = """
            1 _
            1 _
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            assertFalse(isMoveUseful(0, 0)) // Unsinn
            assertFalse(isMoveUseful(0, 1)) // bringt nix
            assertFalse(isMoveUseful(1, 0)) // geht nicht
            assertFalse(isMoveUseful(1, 1)) // Unsinn
        }
    }

    @Test
    fun isMoveUseful_unicolor_3() {
        val boardAscii = """
            1 _ _
            1 _ _
            1 _ _
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            assertFalse(isMoveUseful(0, 0)) // Unsinn
            assertFalse(isMoveUseful(0, 1)) // bringt nix
            assertFalse(isMoveUseful(0, 2)) // bringt nix
            assertFalse(isMoveUseful(1, 0)) // geht nicht
            assertFalse(isMoveUseful(1, 1)) // Unsinn
            assertFalse(isMoveUseful(1, 2)) // geht nicht
            assertFalse(isMoveUseful(2, 0)) // geht nicht
            assertFalse(isMoveUseful(2, 1)) // geht nicht
            assertFalse(isMoveUseful(2, 2)) // Unsinn
        }
    }

    @Test
    fun isSolved_true() {
        GameState().apply {
            resize(2, 3, 4)
            rainbow()
            assertTrue(isSolved())
        }
    }

    /**
     * 1 _    _ _
     * 1 _ => 1 1
     */
    @Test
    fun moveBall_1() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            moveBall(Move(0, 1))
            assertEquals(1, tubes[0].cells[0])
            assertEquals(0, tubes[0].cells[1])
            assertEquals(1, tubes[1].cells[0])
            assertEquals(0, tubes[1].cells[1])
            assertEquals(1, tubes[0].fillLevel)
            assertEquals(1, tubes[1].fillLevel)
            assertEquals(0, moveLog.size())
        }
    }

    /**
     * _ _    _ _
     * _ 3 => 3 _
     */
    @Test
    fun moveBall_2() {
        GameState().apply {
            resize(1, 1, 2)
            tubes[1].addBall(3)
            moveBall(Move(1, 0))
            assertEquals(3, tubes[0].cells[0])
            assertEquals(0, tubes[0].cells[1])
            assertEquals(0, tubes[1].cells[0])
            assertEquals(0, tubes[1].cells[1])
            assertEquals(1, tubes[0].fillLevel)
            assertEquals(0, tubes[1].fillLevel)
        }
    }

    /**
     * _ _
     * _ 3 => Exception
     */
    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_from_empty_tube() {
        GameState().apply {
            resize(1, 1, 2)
            tubes[1].addBall(3)
            assertThrows(IndexOutOfBoundsException::class.java) {
                moveBall(Move(0, 1))
            }
        }
    }

    /**
     * 1 2
     * 1 2 => Exception
     */
    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_full_tube() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            tubes[1].addBall(3)
            assertThrows(IndexOutOfBoundsException::class.java) {
                moveBall(Move(1, 0))
            }
        }
    }

    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_nonexistent_tube() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            assertThrows(IndexOutOfBoundsException::class.java) {
                moveBall(Move(0, 2))
            }
        }
    }

    /**
     * 1 _    _ _
     * 1 _ => 1 1
     */
    @Test
    fun moveBallAndLog_1() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            moveBallAndLog(Move(0, 1))
            assertEquals(1, tubes[0].cells[0])
            assertEquals(0, tubes[0].cells[1])
            assertEquals(1, tubes[1].cells[0])
            assertEquals(0, tubes[1].cells[1])
            assertEquals(1, tubes[0].fillLevel)
            assertEquals(1, tubes[1].fillLevel)
            assertEquals(1, moveLog.size())
            assertEquals(Move(0, 1), moveLog.last())
        }
    }

    @Test
    fun rainbow_1() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            assertEquals(1, tubes[0].cells[0])
            assertEquals(1, tubes[0].cells[1])
            assertEquals(0, tubes[1].cells[0])
            assertEquals(0, tubes[1].cells[1])
            assertEquals(2, tubes[0].fillLevel)
            assertEquals(0, tubes[1].fillLevel)
        }
    }

    /**
     * 1 _    _ _
     * 1 _ => 1 1
     */
    @Test
    fun sameColor_true() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            moveBall(Move(0, 1))
            assertTrue(isSameColor(0, 1))
        }
    }

    @Test
    fun swapTubes() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            swapTubes(0, 1)
            assertEquals(0, tubes[0].cells[0])
            assertEquals(0, tubes[0].cells[1])
            assertEquals(1, tubes[1].cells[0])
            assertEquals(1, tubes[1].cells[1])
            assertEquals(0, tubes[0].fillLevel)
            assertEquals(2, tubes[1].fillLevel)
        }
    }

    @Test
    fun toAscii_alternativeDelimiters() {
        val gs = GameState().apply {
            resize(2, 1, 3)
            tubes[0].apply {
                addBall(1); addBall(2)
            }
            tubes[1].addBall(1)
            tubes[2].addBall(2)

        }
        val str = gs.toAscii("|", ",")
        val expected = """_,_,_|2,_,_|1,1,2"""
        assertEquals(expected, str)
    }

    @Test
    fun toAscii_small() {
        val gs = GameState().apply {
            resize(2, 1, 3)
            tubes[0].apply {
                addBall(1); addBall(2)
            }
            tubes[1].addBall(1)
            tubes[2].addBall(2)

        }
        val str = gs.toAscii()
        val expected = """
            |_ _ _
            |2 _ _
            |1 1 2
            """.trimMargin()
        assertEquals(expected, str)
    }

    @Test
    fun topUnicolorRemovable_1() {
        val boardAscii = """
          1 _ _ _
          2 _ _ 2
          3 1 1 2
          2 3 1 3
          2 3 1 3
        """.trimIndent()
        GameState().apply {
            fromAscii(boardAscii)
            assertTrue(topUnicolorRemovable(0)) // gut
            assertTrue(topUnicolorRemovable(1)) // gut
            assertFalse(topUnicolorRemovable(2)) // <--- Problemfall
            assertFalse(topUnicolorRemovable(3)) // geht gar nicht
        }
    }

    /**
     * erwartet:
     * _ 2 _ _ 2 _
     * _ 3 2 _ 3 4
     * 1 1 4 _ 3 4
     * -----------
     * 0 1 2 3 6 7 <<<
     */
    @Test
    fun tubesSet_2_duplicates() {
        val boardAscii = """
        _ 2 _ _ _ _ 2 _    
        _ 3 2 _ _ _ 3 4 
        1 1 4 _ 1 _ 3 4
        """.trimIndent()
        val expected = arrayOf(0, 1, 2, 3, 6, 7)
        GameState().apply {
            fromAscii(boardAscii)
            val tSet = tubesSet()
            Log.d(TAG, "tSet=$tSet")
            val tSetArray = tSet.toTypedArray()
            assertTrue(expected contentEquals tSetArray)
        }
    }

    @Test
    fun undoCheat_1() {
        val expectedAscii = """
            _ _ 2
            1 _ 2
            1 _ 2
        """.trimIndent()
        val boardAscii = """
            _ _ 2 _
            1 _ 2 _
            1 _ 2 _
        """.trimIndent()
        val movesAscii = "99->0"
        GameState().apply {
            fromAscii(boardAscii)
            moveLog.fromAscii(movesAscii)
            undoCheat()
            moveLog.pop()
            val resultAscii = toAscii()
            assertEquals(expectedAscii, resultAscii)
        }
    }

    /**
     * _ _ _ 3 _    _ _ _ 3 _
     * 3 2 2 1 _ => 3 _ 2 1 _
     * 1 2 3 1 _    1 2 3 1 2
     *          undo
     */
    @Test
    fun undoLastMove_one_of_2_moves() {
        val boardAscii = """
            _ _ _ 3 _
            3 2 2 1 _
            1 2 3 1 _
        """.trimIndent()
        val movesAscii = "0->3, 4->1"
        GameState().apply {
            fromAscii(boardAscii)
            moveLog.fromAscii(movesAscii)
            undoLastMove()
            assertEquals(1, tubes[1].fillLevel)
            assertEquals(1, tubes[4].fillLevel)
            assertEquals(2, tubes[4].cells[0])
            assertEquals(1, moveLog.size())
        }
    }

    /*
    @Test
    fun usefulSourceTubes_5_out_of_7() {
        val boardAscii = """
        _ 2 _ _ _ _ 2 _
        _ 3 2 _ _ _ 3 4
        1 1 4 _ 1 _ 3 4
        """.trimIndent()
        val expected = arrayOf(0, 1, 2, 6, 7)
        GameState().apply {
            fromAscii(boardAscii)
            val tSet = tubesSet()
            Log.d(TAG, "tSet=$tSet")
            val uSet = usefulSourceTubes()
            Log.d(TAG, "uSet=$uSet")
            val uSetArray = uSet.toTypedArray()
            assertTrue(expected contentEquals uSetArray)
        }
    }
    */


    @Test
    fun usefulTargetTubes_5_out_of_8() {
        val boardAscii = """
        _ 2 _ _ _ _ 2 _    
        _ 3 2 _ _ _ 3 4 
        1 1 4 _ 1 _ 3 4
        """.trimIndent()
        val expected = arrayOf(0, 2, 3, 4, 5, 7)
        GameState().apply {
            fromAscii(boardAscii)
            val tSet = tubesSet()
            Log.d(TAG, "tSet=$tSet")
            val uSet = usefulTargetTubes()
            Log.d(TAG, "uSet=$uSet")
            val uSetArray = uSet.toTypedArray()
            assertTrue(expected contentEquals uSetArray)
        }
    }

    companion object {
        private const val TAG = "balla.GameStateTest"
    }
}