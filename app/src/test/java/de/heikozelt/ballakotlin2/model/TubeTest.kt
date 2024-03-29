package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

//import org.junit.jupiter.api.Test
//import org.junit.Test
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertTrue
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertNotSame

class TubeTest {
    @Test
    fun canTake_empty_true() {
        Tube(3).apply {
            assertTrue(canTake(1))
        }
    }

    @Test
    fun canTake_full_false() {
        Tube(3).apply {
            addBall(1); addBall(1); addBall(1)
            assertFalse(canTake(1))
        }
    }

    @Test
    fun canTake_same_color_true() {
        Tube(3).apply {
            addBall(2); addBall(1)
            assertTrue(canTake(1))
        }
    }

    @Test
    fun canTake_different_color_false() {
        Tube(3).apply {
            addBall(1); addBall(2)
            assertFalse(canTake(1))
        }
    }

    @Test
    fun tube_constructor() {
        val t = Tube(3)
        assertEquals(0, t.cells[0])
        assertEquals(0, t.cells[1])
        assertEquals(0, t.cells[2])
        assertEquals(3, t.tubeHeight)
    }

    @Test
    fun isFull_wrong() {
        val t = Tube(3)
        assertEquals(false, t.isFull())
    }

    @Test
    fun isEmpty_true() {
        val t = Tube(3)
        assertEquals(true, t.isEmpty())
    }

    @Test
    fun fillWithOneColor() {
        val t = Tube(3)
        t.fillWithOneColor(5)
        assertEquals(5, t.cells[0])
        assertEquals(5, t.cells[1])
        assertEquals(5, t.cells[2])
        assertEquals(3, t.fillLevel)
        assertTrue(t.isFull())
        assertFalse(t.isEmpty())
    }

    @Test
    fun addBalls() {
        val t = Tube(3)
        t.addBall(4)
        t.addBall(5)
        t.addBall(6)
        assertEquals(4, t.cells[0])
        assertEquals(5, t.cells[1])
        assertEquals(6, t.cells[2])
        assertEquals(3, t.fillLevel)
    }

    @Test
    fun addBall_and_removeBall() {
        val t = Tube(3)
        t.addBall(4)
        val color = t.removeBall()
        assertEquals(4, color)
        assertEquals(true, t.isEmpty())
    }

    @Test
    fun addBall_and_colorOfTopmostBall() {
        val t = Tube(3)
        t.addBall(4)
        val color = t.colorOfTopmostBall()
        assertEquals(4, color)
    }

    @Test
    fun addBalls_and_colorOfTopSecondBall() {
        val t = Tube(3)
        t.addBall(4)
        t.addBall(5)
        val color = t.colorOfTopSecondBall()
        assertEquals(4, color)
    }

    @Test
    fun addBalls_and_copy() {
        val t1 = Tube(3)
        t1.addBall(4)
        t1.addBall(5)
        t1.addBall(6)
        val t2 = t1.clone()
        assertEquals(4, t2.cells[0])
        assertEquals(5, t2.cells[1])
        assertEquals(6, t2.cells[2])
        assertEquals(3, t2.fillLevel)
        assertNotSame(t1, t2)
        assertNotSame(t1.cells, t2.cells)
    }

    @Test
    fun empty_tube_isReverseDonorCandidate_wrong() {
        val t = Tube(6)
        assertFalse(t.isReverseDonorCandidate())
    }

    @Test
    fun one_ball_isReverseDonorCandidate_true() {
        val t = Tube(6)
        t.addBall(4)
        assertTrue(t.isReverseDonorCandidate())
    }

    @Test
    fun same_color_isReverseDonorCandidate_true() {
        val t = Tube(6)
        t.addBall(9)
        t.addBall(9)
        assertTrue(t.isReverseDonorCandidate())
    }

    @Test
    fun different_color_isReverseDonorCandidate_wrong() {
        val t = Tube(6)
        t.addBall(9)
        t.addBall(3)
        assertFalse(t.isReverseDonorCandidate())
    }

    @Test
    fun empty_tube_isReverseReceiverCandidate_true() {
        val t = Tube(4)
        assertTrue(t.isReverseReceiverCandidate())
    }

    @Test
    fun full_tube_isReverseReceiverCandidate_wrong() {
        val t = Tube(4)
        t.fillWithOneColor(5)
        assertFalse(t.isReverseReceiverCandidate())
    }

    @Test
    fun unicolor_isSolved_true() {
        val t = Tube(4)
        t.fillWithOneColor(5)
        assertTrue(t.isSolved())
    }

    @Test
    fun countTopBallsWithSameColor_0() {
        val t = Tube(3)
        assertEquals(0, t.countTopBallsWithSameColor())
    }

    @Test
    fun countTopBallsWithSameColor_2() {
        val t = Tube(5)
        t.addBall(4)
        t.addBall(1)
        t.addBall(1)
        assertEquals(2, t.countTopBallsWithSameColor())
    }

    @Test
    fun countTopBallsWithSameColor_4() {
        val t = Tube(4)
        t.fillWithOneColor(3)
        assertEquals(4, t.countTopBallsWithSameColor())
    }

    @Test
    fun freeCells_0() {
        val t = Tube(4)
        t.fillWithOneColor(3)
        assertEquals(0, t.freeCells())
    }

    @Test
    fun freeCells_3() {
        val t = Tube(3)
        assertEquals(3, t.freeCells())
    }

    @Test
    fun contentEquals_true() {
        val t1 = Tube(3).apply{
            addBall(1); addBall(2)
        }
        val t2 = Tube(3).apply{
            addBall(1); addBall(2)
        }
        assertTrue(t1.contentEquals(t2))
    }

    @Test
    fun contentEquals_false() {
        val t1 = Tube(3).apply{
            addBall(1); addBall(2)
        }
        val t2 = Tube(3).apply{
            addBall(1)
        }
        assertFalse(t1.contentEquals(t2))
    }

    @Test
    fun isUnicolorOrEmpty_empty() {
        Tube(3).apply {
            assertTrue(isUnicolorOrEmpty())
        }
    }

    @Test
    fun isUnicolorOrEmpty_one_ball() {
        Tube(3).apply {
            addBall(1)
            assertTrue(isUnicolorOrEmpty())
        }
    }

    @Test
    fun isUnicolorOrEmpty_two_samecolor_balls() {
        Tube(3).apply {
            addBall(1); addBall(1)
            assertTrue(isUnicolorOrEmpty())
        }
    }

    @Test
    fun isUnicolorOrEmpty_false() {
        Tube(3).apply {
            addBall(1); addBall(2)
            assertFalse(isUnicolorOrEmpty())
        }
    }

    @Test
    fun repairFillLevel0() {
        val t = Tube(2)
        t.fillLevel = 13
        t.repairFillLevel()
        assertEquals(0, t.fillLevel)
    }

    @Test
    fun repairFillLevel1() {
        val t = Tube(2)
        t.cells[0] = 1
        t.fillLevel = 13
        t.repairFillLevel()
        assertEquals(1, t.fillLevel)
    }

    @Test
    fun repairFillLevel2() {
        val t = Tube(2)
        t.cells[0] = 3
        t.cells[1] = 4
        t.fillLevel = 13
        t.repairFillLevel()
        assertEquals(2, t.fillLevel)
    }

}