package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MovesTest {

    @Test
    fun fromAscii_2_moves() {
        val ms = Moves()
        ms.fromAscii("1 -> 2,17->3")
        assertEquals(2, ms.size())
        assertEquals(Move(17,3), ms.pop())
        assertEquals(Move(1,2), ms.pop())
    }

    @Test
    fun fromAscii_6_moves() {
        val ms = Moves()
        ms.fromAscii("""2->0, 4->0, 4->11, 11->0, 11->4, 15->0, 16->0""")
        assertEquals(7, ms.size())
        val arr = ms.toArray()
        assertEquals(Move(2,0), arr[0])
        assertEquals(Move(4,0), arr[1])
        assertEquals(Move(4,11), arr[2])
        assertEquals(Move(11,0), arr[3])
        assertEquals(Move(11,4), arr[4])
        assertEquals(Move(15,0), arr[5])
        assertEquals(Move(16,0), arr[6])
    }

    @Test
    fun fromAscii_empty() {
        val ms = Moves()
        ms.fromAscii("")
        assertEquals(0, ms.size())
    }

    @Test
    fun fromAscii_space() {
        val ms = Moves()
        ms.fromAscii(" ")
        assertEquals(0, ms.size())
    }

    @Test
    fun toAscii_2_moves() {
        val ms = Moves()
        ms.push(Move(1,2))
        ms.push(Move(17,3))
        assertEquals("1->2, 17->3", ms.toAscii())
    }

    @Test
    fun toAscii_2_empty() {
        val ms = Moves()
        assertEquals("", ms.toAscii())
    }

    @Test
    fun iterator_1() {
        val moves = Moves().apply {
            push(Move(1,2))
            push(Move(3,4))
        }
        val iter = moves.iterator()
        Assertions.assertTrue(iter.hasNext())
        assertEquals(Move(1,2), iter.next())
        Assertions.assertTrue(iter.hasNext())
        assertEquals(Move(3,4), iter.next())
        Assertions.assertFalse(iter.hasNext())
    }
}