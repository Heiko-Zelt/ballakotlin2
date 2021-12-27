package de.heikozelt.ballakotlin2.model

import org.junit.Test
import org.junit.Assert.*

class MoveTest {
    @Test
    fun backwards() {
        val m = Move(2, 3)
        val b = m.backwards()
        assertEquals(3, b.from)
        assertEquals(2, b.to)
    }

    @Test
    fun move_equals() {
        val m1 = Move(7, 8)
        val m2 = Move(7, 8)
        // == Testet auf Datengleichheit in Kotlin
        assertTrue(m1 == m2)
    }

    @Test
    fun backward_move_equals() {
        val m1 = Move(7, 8)
        val m2 = Move(8, 7)
        val b = m2.backwards()
        assertTrue(b == m1)
    }

    @Test
    fun move_copy() {
        val m1 = Move(7, 1)
        val m2 = m1.copy()
        assertEquals(7, m2.from)
        assertEquals(1, m2.to)
    }
}