package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

//import org.junit.Test
//import org.junit.Assert.*

class MoveTest {
    @Test
    fun backwards_1() {
        val m = Move(2, 3)
        val b = m.backwards()
        assertEquals(3, b.from)
        assertEquals(2, b.to)
    }

    @Test
    fun equals_true() {
        val m1 = Move(7, 8)
        val m2 = Move(7, 8)
        // == Testet auf Datengleichheit in Kotlin
        assertTrue(m1 == m2)
    }

    @Test
    fun equals_false_unequal() {
        val m1 = Move(7, 8)
        val m2 = Move(7, 9)
        // == Testet auf Datengleichheit in Kotlin
        assertFalse(m1 == m2)
    }

    @Test
    fun equals_false_null() {
        val m1 = Move(7, 8)
        val m2:Move? = null
        // == Testet auf Datengleichheit in Kotlin
        assertFalse(m1 == m2)
    }

    @Test
    fun equals_false_not_a_move() {
        val m1 = Move(7, 8)
        val m2 = "Hello World"
        // == Testet auf Datengleichheit in Kotlin
        assertFalse(m1.equals(m2))
    }

    @Test
    fun backward_move_equals() {
        val m1 = Move(7, 8)
        val m2 = Move(8, 7)
        val b = m2.backwards()
        assertTrue(b == m1)
    }

    @Test
    fun fromAscii_1() {
        val m = Move()
        m.fromAscii(" 0->17")
        assertEquals(0, m.from)
        assertEquals(17, m.to)
    }

    @Test
    fun fromAscii_NumberFormat() {
        val m = Move()
        assertThrows(IllegalArgumentException::class.java) {
            m.fromAscii(" 0->f")
        }
    }

    @Test
    fun fromAscii_empty() {
        val m = Move()
        assertThrows(IllegalArgumentException::class.java) {
            m.fromAscii(" ")
        }
    }

    @Test
    fun fromAscii_2_arrows() {
        val m = Move()
        assertThrows(IllegalArgumentException::class.java) {
            m.fromAscii("1->2->3")
        }
    }

    @Test
    fun toAscii_1() {
        assertEquals("1->17", Move(1,17).toAscii())
    }

    /*
    Copy ist eine methode von data class.
    Brauche ich die ueberhaupt?
    @Test
    fun move_copy() {
        val m1 = Move(7, 1)
        val m2 = m1.copy()
        assertEquals(7, m2.from)
        assertEquals(1, m2.to)
    }
     */
}