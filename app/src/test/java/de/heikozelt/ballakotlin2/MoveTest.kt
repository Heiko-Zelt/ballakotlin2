package de.heikozelt.ballaballa_kotlin

import de.heikozelt.ballakotlin2.model.Move
import org.junit.Test

import org.junit.Assert.*

class MoveTest {
    @Test
    fun backwards() {
        val m = Move(2,3)
        val b = m.backwards()
        assertEquals(3, b.from)
        assertEquals(2, b.to)
    }

    @Test
    fun move_equals() {
        val m1 = Move(7,8)
        val m2 = Move(7, 8)
        val e = m1.equals(m2)
        assertEquals(true, e)
    }

    @Test
    fun backward_move_equals() {
        val m1 = Move(7,8)
        val m2 = Move(8, 7)
        val b = m2.backwards()
        val e = b.equals(m1)
        assertEquals(true, e)
    }

    @Test
    fun move_copy() {
        val m1 = Move(7, 1)
        val m2 = m1.copy()
        assertEquals(7, m2.from)
        assertEquals(1, m2.to)
    }
}