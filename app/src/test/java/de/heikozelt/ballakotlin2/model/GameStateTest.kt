package de.heikozelt.ballakotlin2.model

//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertTrue
//import org.junit.Assert.assertFalse
//import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.IndexOutOfBoundsException


class GameStateTest {
    @Test
    fun gameState_constructor() {
        GameState(2, 1, 3).apply {
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
    fun init_tubes() {
        GameState(1, 1, 2).apply {
            initTubes()
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
        GameState(2, 3, 4).apply {
            initTubes()
            assertTrue(isSolved())
        }
    }

    /**
     * 1 _    _ _
     * 1 _ => 1 1
     */
    @Test
    fun moveBall1() {
        GameState(1, 1, 2).apply {
            initTubes()
            moveBall(Move(0, 1))
            assertEquals(1, tubes[0].cells[0])
            assertEquals(0, tubes[0].cells[1])
            assertEquals(1, tubes[1].cells[0])
            assertEquals(0, tubes[1].cells[1])
            assertEquals(1, tubes[0].fillLevel)
            assertEquals(1, tubes[1].fillLevel)
        }
    }

    /**
     * _ _    _ _
     * _ 3 => 3 _
     */
    @Test
    fun moveBall2() {
        GameState(1, 1, 2).apply {
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
        GameState(1, 1, 2).apply {
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
        GameState(1, 1, 2).apply {
            initTubes()
            tubes[1].addBall(3)
            assertThrows(IndexOutOfBoundsException::class.java) {
                moveBall(Move(1, 0))
            }
        }
    }

    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_nonexistent_tube() {
        GameState(1, 1, 2).apply {
            initTubes()
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
    fun sameColor_true() {
        GameState(1, 1, 2).apply {
            initTubes()
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
        val moves = GameState(1, 1, 2).run {
            initTubes()
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
        val moves = GameState(1, 1, 2).run {
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
        val moves = GameState(1, 1, 2).run {
            tubes[0].addBall(1)
            moveBallAndLog(Move(0, 1))
            allPossibleBackwardMoves()
        }
        assertEquals(0, moves.size)
    }

    @Test
    fun allUsefulMoves_empty() {
        val gs = GameState(1, 1, 2).apply {
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
        val gs = GameState(2, 1, 3).apply {
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
        val gs = GameState(2, 1, 3).apply {
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
        GameState(2, 1, 3).apply {
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
        GameState(2, 1, 3).apply {
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
    fun swapTubes() {
        GameState(1, 1, 2).apply {
            initTubes()
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
        GameState(1, 1, 2).apply {
            initTubes()
            cheat()
            assertEquals(3, numberOfTubes)
            assertEquals(3, tubes.size)
        }
    }

    @Test
    fun isMoveAllowed_to_empty_tube_true() {
        GameState(1, 1, 2).apply {
            initTubes()
            assertTrue(isMoveAllowed(0, 1))
        }
    }

    @Test
    fun isMoveAllowed_to_same_tube_true() {
        GameState(1, 1, 2).apply {
            initTubes()
            assertTrue(isMoveAllowed(0, 0))
        }
    }

    @Test
    fun isMoveAllowed_to_full_tube_wrong() {
        GameState(2, 1, 3).apply {
            initTubes()
            assertFalse(isMoveAllowed(0, 1))
        }
    }

    @Test
    fun isMoveAllowed_to_same_color_true() {
        GameState(2, 1, 3).apply {
            initTubes()
            tubes[2].addBall(1)
            assertTrue(isMoveAllowed(0, 2))
        }
    }

    @Test
    fun areEqual_false() {
        val list = mutableListOf(0, 0, 0, 42)
        val gs = GameState(2, 3, 1)
        assertFalse(gs.areEqual(list))
    }

    @Test
    fun areEqual_true() {
        val list = mutableListOf(42)
        val gs = GameState(2, 3, 1)
        assertTrue(gs.areEqual(list))
    }

    /**
     * Die einfachste Lösung 1 besteht aus einem einzigen Zug.
     * 2 _ _    1 1 _
     * 2 1 _    2 1 _
     * 2 1 1 => 2 1 _
     */
    @Test
    fun findSolutionNoBackAndForth_recursionDepth_0_not_found() {
        var gs = GameState(2, 1, 3).apply {
            tubes[0].apply {
                addBall(2); addBall(2); addBall(2)
            }
            tubes[1].apply {
                addBall(1); addBall(1)
            }
            tubes[2].addBall(1)
        }

        val solution = gs.findSolutionNoBackAndForth(0)
        val solutionStr = if (solution == null) {
            "null"
        } else {
            solution.joinToString(prefix = "[", separator = ", ", postfix = "]")
        }
        println("solution: $solutionStr")

        assertEquals(null, solution)
    }

    /**
     * Die einfachste Lösung besteht aus einem einzigen Zug.
     * 2 _ _    1 1 _
     * 2 1 _    2 1 _
     * 2 1 1 => 2 1 _
     */
    @Test
    fun findSolutionNoBackAndForth_recursionDepth_1_found() {
        var gs = GameState(2, 1, 3).apply {
            tubes[0].apply {
                addBall(2); addBall(2); addBall(2)
            }
            tubes[1].apply {
                addBall(1); addBall(1)
            }
            tubes[2].addBall(1)
        }
        val expectedSolution = arrayOf(Move(2, 1))

        val solution = gs.findSolutionNoBackAndForth(1)
        val solutionStr = if (solution == null) {
            "null"
        } else {
            solution.joinToString(prefix = "[", separator = ", ", postfix = "]")
        }
        println("solution: $solutionStr")

        val solutionArray = solution?.toTypedArray()
        val match = expectedSolution contentEquals solutionArray
        assertTrue(match)
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
     *
     * Lösung 2a
     * 1 2 _    1 _ _    _ 1 _    _ 1 2
     * 2 1 _ => 2 1 2 => 2 1 2 => _ 1 2
     *
     * Lösung 2b
     * 1 2 _    1 _ _    _ 1 _    2 1 _
     * 2 1 _ => 2 1 2 => 2 1 2 => 2 1 _
     */
    @Test
    fun findSolution_3_moves() {
        val gs = GameState(2, 1, 2).apply {
            tubes[0].apply {
                addBall(1); addBall(2)
            }
            tubes[1].apply {
                addBall(2); addBall(1)
            }
        }
        val solution1a = arrayOf(Move(0, 2), Move(1, 0), Move(1, 2))
        val solution1b = arrayOf(Move(0, 2), Move(1, 0), Move(2, 1))
        val solution2a = arrayOf(Move(1, 2), Move(0, 1), Move(0, 2))
        val solution2b = arrayOf(Move(1, 2), Move(0, 1), Move(2, 0))

        val solution = gs.findSolution()
        val solutionStr = if (solution == null) {
            "null"
        } else {
            solution.joinToString(prefix = "[", separator = ", ", postfix = "]")
        }
        println("solution: $solutionStr")

        val solutionArray = solution?.toTypedArray()
        val match1a = solution1a contentEquals solutionArray
        val match1b = solution1b contentEquals solutionArray
        val match2a = solution2a contentEquals solutionArray
        val match2b = solution2b contentEquals solutionArray

        assertTrue(match1a || match1b || match2a || match2b)
    }

    @Test
    fun shortesList_empty() {
        val gs = GameState(2, 1, 2)
        val listOfLists = mutableListOf<MutableList<Move>>()
        assertEquals(null, gs.shortestList(listOfLists))
    }

    @Test
    fun shortesList_one() {
        val gs = GameState(2, 1, 2)
        val moves = mutableListOf(Move(0, 1))
        val listOfLists = mutableListOf(moves)
        assertEquals(moves, gs.shortestList(listOfLists))
    }

    @Test
    fun shortesList_two() {
        val gs = GameState(2, 1, 2)
        val moves0 = mutableListOf(Move(0, 1))
        val moves1 = mutableListOf(Move(0, 1), Move(1, 0))
        val listOfLists = mutableListOf(moves0, moves1)
        assertEquals(moves0, gs.shortestList(listOfLists))
    }

    @Test
    fun shortesList_three() {
        val gs = GameState(2, 1, 2)
        val moves0 = mutableListOf(Move(0, 1))
        val moves1 = mutableListOf(Move(0, 1), Move(1, 0))
        val moves2 = mutableListOf<Move>()
        val listOfLists = mutableListOf(moves0, moves1, moves2)
        assertEquals(moves2, gs.shortestList(listOfLists))
    }

}