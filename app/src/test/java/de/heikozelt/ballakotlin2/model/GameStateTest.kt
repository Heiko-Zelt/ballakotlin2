package de.heikozelt.ballakotlin2.model

//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertTrue
//import org.junit.Assert.assertFalse
//import org.junit.Test

import android.util.Log
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.IndexOutOfBoundsException

import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.test.runTest

//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.runBlocking geht nicht in Test
//import kotlinx.coroutines.test.runBlockingTest is deprecated


//@get:Rule
//val coroutineTestRule = CoroutineTestRule()

class GameStateTest {
    @Test
    fun gameState_constructor() {
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
    fun rainbow() {
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
    fun moveBall1() {
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
    fun moveBall2() {
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
    fun moveBallAndLog() {
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

    /*
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

    /*
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

    /*
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

    @Test
    fun allUsefulMoves_empty() {
        val gs = GameState().apply {
            resize(1, 1, 2)
            tubes[0].addBall(1)
        }
        assertEquals(emptyList<Move>(), gs.allUsefulMoves())
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
    fun cheat() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            cheat()
            assertEquals(3, numberOfTubes)
            assertEquals(3, tubes.size)
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
    fun isMoveAllowed_to_same_tube_true() {
        GameState().apply {
            resize(1, 1, 2)
            rainbow()
            assertTrue(isMoveAllowed(0, 0))
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
    fun areEqual_false() {
        val list = mutableListOf(0, 0, 0, 42)
        val gs = GameState()
        gs.resize(2, 3, 1)
        assertFalse(gs.areEqual(list))
    }

    @Test
    fun areEqual_true() {
        val list = mutableListOf(42)
        val gs = GameState()
        gs.resize(2, 3, 1)
        assertTrue(gs.areEqual(list))
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

        var result = SearchResult()
        runTest {
            val job: Job = GlobalScope.launch(Default) {
                result = gs.findSolutionNoBackAndForth(0)
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

        var result = SearchResult()
        runTest {
            val job = GlobalScope.launch(Default) {
                result = gs.findSolutionNoBackAndForth(1)
            }
            job.join()
        }

        assertEquals(SearchResult.STATUS_FOUND_SOLUTION, result.status)
        assertEquals(Move(2, 1), result.move)
        //todo: weitere result-properties pruefen
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
                result = gs.findSolution()
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

    /**
     * _ _ _ _ _ _
     * 1 2 _ _ _ _
     * 1 2 _ _ _ _
     * unloesbar, weil Ball von einfarbiger Röhre nicht zu leerer Röhre gezogen werden kann.
     */
    @ExperimentalCoroutinesApi
    @Test
    fun findSolution_unsolvable() {
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
                result = gs.findSolution()
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
        GameState().apply {
            fromAscii(txt)
            runTest {
                val job = GlobalScope.launch(Default) {
                    result = findSolution()
                }
                job.join()
            }
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
     * todo: method shortestList() is never used, delete method and tests
     */
    @Test
    fun shortesList_empty() {
        val gs = GameState()
        gs.resize(2, 1, 2)
        val listOfLists = mutableListOf<MutableList<Move>>()
        assertEquals(null, gs.shortestList(listOfLists))
    }

    @Test
    fun shortestList_one() {
        val gs = GameState()
        gs.resize(2, 1, 2)
        val moves = mutableListOf(Move(0, 1))
        val listOfLists = mutableListOf(moves)
        assertEquals(moves, gs.shortestList(listOfLists))
    }

    @Test
    fun shortestList_two() {
        val gs = GameState()
        gs.resize(2, 1, 2)
        val moves0 = mutableListOf(Move(0, 1))
        val moves1 = mutableListOf(Move(0, 1), Move(1, 0))
        val listOfLists = mutableListOf(moves0, moves1)
        assertEquals(moves0, gs.shortestList(listOfLists))
    }

    @Test
    fun shortestList_three() {
        val gs = GameState()
        val moves0 = mutableListOf(Move(0, 1))
        val moves1 = mutableListOf(Move(0, 1), Move(1, 0))
        val moves2 = mutableListOf<Move>()
        val listOfLists = mutableListOf(moves0, moves1, moves2)
        assertEquals(moves2, gs.shortestList(listOfLists))
    }

    @Test
    fun colorToChar() {
        val gs = GameState()
        assertEquals('_', gs.colorToChar(0))
        assertEquals('1', gs.colorToChar(1))
        assertEquals('9', gs.colorToChar(9))
        assertEquals('a', gs.colorToChar(10))
        assertEquals('f', gs.colorToChar(15))
    }

    fun charToColor() {
        val gs = GameState()
        assertEquals(0, gs.charToColor('_'))
        assertEquals(1, gs.charToColor('1'))
        assertEquals(9, gs.charToColor('9'))
        assertEquals(10, gs.charToColor('a'))
        assertEquals(15, gs.charToColor('f'))
    }

    @Test
    fun fromAsciiLines_with_spaces() {
        val lines = arrayOf<String>(
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

    // todo: Lücken bei den Zahlen testen
    @Test
    fun fromAsciiLines_no_spaces() {
        val lines = arrayOf<String>(
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

    /**
     * _ _ _ 3 _    _ _ _ 3 _
     * 3 2 2 1 _ => 3 _ 2 1 _
     * 1 2 3 1 _    1 2 3 1 2
     *          undo
     */
    @Test
    fun undo_one_of_2_moves() {
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
        val expectedAscii = """4->0, 4->11, 11->4"""
        GameState().apply {
            fromAscii(boardAscii)
            val u = allUsefulMoves()
            val result = Moves()
            result.fromList(u)
            assertEquals(expectedAscii, result.toAscii())
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

    companion object {
        private const val TAG = "balla.GameStateTest"
    }
}