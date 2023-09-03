package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LimitedSetTest {

    @Test
    fun test1() {
        val set = LimitedSet<Int>(2)
        assertFalse(0 in set)
        assertFalse(111 in set)
        assertFalse(222 in set)
        set.add(111)
        assertFalse(0 in set)
        assertTrue(111 in set)
        assertFalse(222 in set)
        set.add(222)
        assertFalse(0 in set)
        assertTrue(111 in set)
        assertTrue(222 in set)
        set.add(333)
        assertFalse(0 in set)
        assertFalse(111 in set)
        assertTrue(222 in set)
    }
}