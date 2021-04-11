package de.heikozelt.ballakotlin2

import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.Move
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
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

    @Test(expected=IndexOutOfBoundsException::class)
    fun moveBall_from_empty_tube() {
        val g = GameState(1,1,2)
        g.tubes[1].addBall(3)
        val m = Move(0, 1)
        g.moveBall(m)
    }

    @Test(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_full_tube() {
        val g = GameState(1,1,2)
        g.initTubes()
        g.tubes[1].addBall(3)
        val m = Move(1, 0)
        g.moveBall(m)
    }

    @Test(expected=IndexOutOfBoundsException::class)
    fun moveBall_to_not_existing_tube() {
        val g = GameState(1,1,2)
        g.initTubes()
        val m = Move(0, 2)
        g.moveBall(m)
    }

    @Test
    fun sameColor() {
        val g = GameState(1,1,2)
        g.initTubes()
        val m = Move(0, 1)
        g.moveBall(m)
        assertTrue(g.isSameColor(0, 1))
    }

    @Test
    fun allPossibleBackwardMoves() {
        val g = GameState(1,1,2)
        g.initTubes()
        val moves= g.allPossibleBackwardMoves(null)
        assertEquals(1, moves.size)
        assertEquals(0, moves[0].from)
        assertEquals(1, moves[0].to)
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
}