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
        val g = GameState(2, 1, 3)
        assertEquals(2, g.numberOfColors)
        assertEquals(1, g.numberOfExtraTubes)
        assertEquals(3, g.tubeHeight)
        assertEquals(3, g.numberOfTubes)
        assertEquals(0, g.tubes[0].fillLevel)
        assertEquals(0, g.tubes[1].fillLevel)
        assertEquals(0, g.tubes[2].fillLevel)
    }

    @Test
    fun init_tubes() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        assertEquals(1, g.tubes[0].cells[0])
        assertEquals(1, g.tubes[0].cells[1])
        assertEquals(0, g.tubes[1].cells[0])
        assertEquals(0, g.tubes[1].cells[1])
        assertEquals(2, g.tubes[0].fillLevel)
        assertEquals(0, g.tubes[1].fillLevel)
    }

    @Test
    fun isSolved_true() {
        val g = GameState(2, 3, 4)
        g.initTubes()
        assertTrue(g.isSolved())
    }

    @Test
    fun moveBall1() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        val m = Move(0, 1)
        g.moveBall(m)
        assertEquals(1, g.tubes[0].cells[0])
        assertEquals(0, g.tubes[0].cells[1])
        assertEquals(1, g.tubes[1].cells[0])
        assertEquals(0, g.tubes[1].cells[1])
        assertEquals(1, g.tubes[0].fillLevel)
        assertEquals(1, g.tubes[1].fillLevel)
    }

    @Test
    fun moveBall2() {
        val g = GameState(1, 1, 2)
        g.tubes[1].addBall(3)
        val m = Move(1, 0)
        g.moveBall(m)
        assertEquals(3, g.tubes[0].cells[0])
        assertEquals(0, g.tubes[0].cells[1])
        assertEquals(0, g.tubes[1].cells[0])
        assertEquals(0, g.tubes[1].cells[1])
        assertEquals(1, g.tubes[0].fillLevel)
        assertEquals(0, g.tubes[1].fillLevel)
    }

    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_from_empty_tube() {
        val g = GameState(1, 1, 2)
        g.tubes[1].addBall(3)
        val m = Move(0, 1)
        assertThrows(IndexOutOfBoundsException::class.java) {
            g.moveBall(m)
        }
    }

    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_full_tube() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        g.tubes[1].addBall(3)
        val m = Move(1, 0)
        assertThrows(IndexOutOfBoundsException::class.java) {
            g.moveBall(m)
        }
    }

    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_not_existing_tube() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        val m = Move(0, 2)
        assertThrows(IndexOutOfBoundsException::class.java) {
            g.moveBall(m)
        }
    }

    @Test
    fun sameColor_true() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        val m = Move(0, 1)
        g.moveBall(m)
        assertTrue(g.isSameColor(0, 1))
    }

    /*
     * 1 _    _ _
     * 1 _ => 1 1
     */
    @Test
    fun allPossibleBackwardMoves_full_to_empty() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        val moves = g.allPossibleBackwardMoves()
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
        val g = GameState(1, 1, 2)
        g.tubes[0].addBall(1)
        val moves = g.allPossibleBackwardMoves()
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
        val g = GameState(1, 1, 2)
        g.tubes[0].addBall(1)
        g.moveBallAndLog(Move(0, 1))
        val moves = g.allPossibleBackwardMoves()
        assertEquals(0, moves.size)
    }

    @Test
    fun allUsefulMoves_empty() {
        val g = GameState(1, 1, 2)
        g.tubes[0].addBall(1)
        assertEquals(emptyList<Move>(), g.allUsefulMoves())
    }

    /**
     * _ _ _    _ _ _      2 _ _
     * 2 _ _ => _ _ 2 oder 2 _ _
     * 1 1 2    1 1 2      1 _ _
     */
    @Test
    fun allUsefulMoves_two() {
        val g = GameState(2, 1, 3)
        g.tubes[0].addBall(1)
        g.tubes[0].addBall(2)
        g.tubes[1].addBall(1)
        g.tubes[2].addBall(2)
        val moves = listOf(Move(0, 2), Move(2, 0))
        assertEquals(moves, g.allUsefulMoves())
    }

    /**
     * 2 _ _    _ _ _    _ _ _
     * 2 _ _ => 2 _ _ => _ _ 2
     * 1 1 _    1 1 2    1 1 2
     */
    @Test
    fun allUsefulMoves_no_back_and_forth() {
        val g = GameState(2, 1, 3)
        g.tubes[0].addBall(1)
        g.tubes[0].addBall(2)
        g.tubes[0].addBall(2)
        g.tubes[1].addBall(1)
        g.moveBallAndLog(Move(0, 2))
        val moves = listOf(Move(0, 2))
        assertEquals(moves, g.allUsefulMoves())
    }

    /**
     * nur ein einziger sinnvoller Zug möglich
     * 2 _ _    _ _ _    _ _ _
     * 2 _ _ => 2 _ _ => _ _ 2
     * 1 1 _    1 1 2    1 1 2
     */
    @Test
    fun isMoveUseful_one() {
        val g = GameState(2, 1, 3)
        g.tubes[0].addBall(1)
        g.tubes[0].addBall(2)
        g.tubes[0].addBall(2)
        g.tubes[1].addBall(1)
        g.moveBallAndLog(Move(0, 2))
        assertFalse(g.isMoveUseful(0, 0)) // selbe Roehre
        assertFalse(g.isMoveUseful(0, 1)) // falsche Farbe
        assertTrue(g.isMoveUseful(0, 2)) // einzig sinnvoller Zug
        assertFalse(g.isMoveUseful(1, 0)) // falsche Farbe
        assertFalse(g.isMoveUseful(1, 1)) // selbe Roehre
        assertFalse(g.isMoveUseful(1, 2)) // falsche Farbe
        assertFalse(g.isMoveUseful(2, 0)) // zurueck
        assertFalse(g.isMoveUseful(2, 1)) // falsche Farbe
        assertFalse(g.isMoveUseful(2, 2)) // selbe Roehre
    }

    /**
     * zwei Züge sinnvolle Züge möglich
     * _ _ _    _ _ _      2 _ _
     * 2 _ _ => _ _ 2 oder 2 _ _
     * 1 1 2    1 1 2      1 _ _
     */
    @Test
    fun isMoveUseful_two() {
        val g = GameState(2, 1, 3)
        g.tubes[0].addBall(1)
        g.tubes[0].addBall(2)
        g.tubes[1].addBall(1)
        g.tubes[2].addBall(2)
        assertFalse(g.isMoveUseful(0, 0)) // selbe Roehre
        assertFalse(g.isMoveUseful(0, 1)) // falsche Farbe
        assertTrue(g.isMoveUseful(0, 2)) // sinnvoller Zug
        assertFalse(g.isMoveUseful(1, 0)) // falsche Farbe
        assertFalse(g.isMoveUseful(1, 1)) // selbe Roehre
        assertFalse(g.isMoveUseful(1, 2)) // falsche Farbe
        assertTrue(g.isMoveUseful(2, 0)) // sinnvoller Zug
        assertFalse(g.isMoveUseful(2, 1)) // falsche Farbe
        assertFalse(g.isMoveUseful(2, 2)) // selbe Roehre
    }

    @Test
    fun swapTubes() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        g.swapTubes(0, 1)
        assertEquals(0, g.tubes[0].cells[0])
        assertEquals(0, g.tubes[0].cells[1])
        assertEquals(1, g.tubes[1].cells[0])
        assertEquals(1, g.tubes[1].cells[1])
        assertEquals(0, g.tubes[0].fillLevel)
        assertEquals(2, g.tubes[1].fillLevel)
    }

    @Test
    fun cheat() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        g.cheat()
        assertEquals(3, g.numberOfTubes)
        assertEquals(3, g.tubes.size)
    }

    @Test
    fun isMoveAllowed_to_empty_tube_true() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        assertTrue(g.isMoveAllowed(0, 1))
    }

    @Test
    fun isMoveAllowed_to_same_tube_true() {
        val g = GameState(1, 1, 2)
        g.initTubes()
        assertTrue(g.isMoveAllowed(0, 0))
    }

    @Test
    fun isMoveAllowed_to_full_tube_wrong() {
        val g = GameState(2, 1, 3)
        g.initTubes()
        assertFalse(g.isMoveAllowed(0, 1))
    }

    @Test
    fun isMoveAllowed_to_same_color_true() {
        val g = GameState(2, 1, 3)
        g.initTubes()
        g.tubes[2].addBall(1)
        assertTrue(g.isMoveAllowed(0, 2))
    }

    @Test
    fun areEqual_false() {
        val list = mutableListOf(0, 0, 0, 42)
        val g = GameState(2, 3, 1)
        assertFalse(g.areEqual(list))
    }

    @Test
    fun areEqual_true() {
        val list = mutableListOf(42)
        val g = GameState(2, 3, 1)
        assertTrue(g.areEqual(list))
    }

    /**
     * Die einfachste Lösung 1 besteht aus einem einzigen Zug.
     * 2 _ _    1 1 _
     * 2 1 _    2 1 _
     * 2 1 1 => 2 1 _
     */
    @Test
    fun findSolutionNoBackAndForth_recursionDepth_0_not_found() {
        val g = GameState(2, 1, 3)
        g.tubes[0].addBall(2)
        g.tubes[0].addBall(2)
        g.tubes[0].addBall(2)
        g.tubes[1].addBall(1)
        g.tubes[1].addBall(1)
        g.tubes[2].addBall(1)

        val solution = g.findSolutionNoBackAndForth(0)
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
        val g = GameState(2, 1, 3)
        g.tubes[0].addBall(2)
        g.tubes[0].addBall(2)
        g.tubes[0].addBall(2)
        g.tubes[1].addBall(1)
        g.tubes[1].addBall(1)
        g.tubes[2].addBall(1)
        val expectedSolution = arrayOf(Move(2, 1))

        val solution = g.findSolutionNoBackAndForth(1)
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
        val g = GameState(2, 1, 2)
        g.tubes[0].addBall(1)
        g.tubes[0].addBall(2)
        g.tubes[1].addBall(2)
        g.tubes[1].addBall(1)
        val solution1a = arrayOf(Move(0, 2), Move(1, 0), Move(1, 2))
        val solution1b = arrayOf(Move(0, 2), Move(1, 0), Move(2, 1))
        val solution2a = arrayOf(Move(1, 2), Move(0, 1), Move(0, 2))
        val solution2b = arrayOf(Move(1, 2), Move(0, 1), Move(2, 0))

        val solution = g.findSolution()
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
        val g = GameState(2, 1, 2)
        val listOfLists = mutableListOf<MutableList<Move>>()
        assertEquals(null, g.shortestList(listOfLists))
    }

    @Test
    fun shortesList_one() {
        val g = GameState(2, 1, 2)
        val moves = mutableListOf(Move(0, 1))
        val listOfLists = mutableListOf(moves)
        assertEquals(moves, g.shortestList(listOfLists))
    }

    @Test
    fun shortesList_two() {
        val g = GameState(2, 1, 2)
        val moves0 = mutableListOf(Move(0, 1))
        val moves1 = mutableListOf(Move(0, 1), Move(1, 0))
        val listOfLists = mutableListOf(moves0, moves1)
        assertEquals(moves0, g.shortestList(listOfLists))
    }

    @Test
    fun shortesList_three() {
        val g = GameState(2, 1, 2)
        val moves0 = mutableListOf(Move(0, 1))
        val moves1 = mutableListOf(Move(0, 1), Move(1, 0))
        val moves2 = mutableListOf<Move>()
        val listOfLists = mutableListOf(moves0, moves1, moves2)
        assertEquals(moves2, g.shortestList(listOfLists))
    }

}