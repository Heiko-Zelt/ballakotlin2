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
        val g = GameState(2,1,3)
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
        val g = GameState(1,1,2)
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
        val g = GameState(2,3,4)
        g.initTubes()
        assertTrue(g.isSolved())
    }

    @Test
    fun moveBall1() {
        val g = GameState(1,1,2)
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
        val g = GameState(1,1,2)
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
        val g = GameState(1,1,2)
        g.tubes[1].addBall(3)
        val m = Move(0, 1)
        assertThrows(IndexOutOfBoundsException::class.java) {
            g.moveBall(m)
        }
    }

    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_full_tube() {
        val g = GameState(1,1,2)
        g.initTubes()
        g.tubes[1].addBall(3)
        val m = Move(1, 0)
        assertThrows(IndexOutOfBoundsException::class.java) {
            g.moveBall(m)
        }
    }

    @Test //(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_not_existing_tube() {
        val g = GameState(1,1,2)
        g.initTubes()
        val m = Move(0, 2)
        assertThrows(IndexOutOfBoundsException::class.java) {
            g.moveBall(m)
        }
    }

    @Test
    fun sameColor_true() {
        val g = GameState(1,1,2)
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
        val g = GameState(1,1,2)
        g.initTubes()
        val moves= g.allPossibleBackwardMoves(null)
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
        val g = GameState(1,1,2)
        g.tubes[0].addBall(1)
        val moves= g.allPossibleBackwardMoves(null)
        assertEquals(0, moves.size)
    }

    @Test
    fun swapTubes() {
        val g = GameState(1,1,2)
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
        val g = GameState(1,1,2)
        g.initTubes()
        g.cheat()
        assertEquals(3, g.numberOfTubes)
        assertEquals(3, g.tubes.size)
    }

    @Test
    fun isMoveAllowed_to_empty_tube_true() {
        val g = GameState(1,1,2)
        g.initTubes()
        assertTrue(g.isMoveAllowed(0, 1))
    }

    @Test
    fun isMoveAllowed_to_same_tube_true() {
        val g = GameState(1,1,2)
        g.initTubes()
        assertTrue(g.isMoveAllowed(0, 0))
    }

    @Test
    fun isMoveAllowed_to_full_tube_wrong() {
        val g = GameState(2,1,3)
        g.initTubes()
        assertFalse(g.isMoveAllowed(0, 1))
    }

    @Test
    fun isMoveAllowed_to_same_color_true() {
        val g = GameState(2,1,3)
        g.initTubes()
        g.tubes[2].addBall(1)
        assertTrue(g.isMoveAllowed(0, 2))
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
        val gs = GameState(2,3,1)
        assertTrue(gs.areEqual(list))
    }
}