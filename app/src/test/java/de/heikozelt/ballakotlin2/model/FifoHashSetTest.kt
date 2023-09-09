package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import android.util.Log
import org.junit.jupiter.api.Assertions.assertNotNull

class FifoHashSetTest {

    private fun equalsFunction(a: String, b: String): Boolean {
        return a.lowercase() == b.lowercase()
    }

    private fun hashCodeFunction(s: String): Int {
        return s.lowercase().hashCode()
    }

    @Test
    fun equal() {
        assertTrue(equalsFunction("Hallo", "hallo"))
    }

    @Test
    fun not_equal() {
        assertFalse(equalsFunction("Hallo", "Hi"))
    }

    @Test
    fun hash_same() {
        assertEquals(hashCodeFunction("Hallo"), hashCodeFunction("hallo"))
    }

    @Test
    fun hash_different() {
        assertNotEquals(hashCodeFunction("Hallo"), hashCodeFunction("Hi"))
    }

    @Test
    fun test_empty() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10)
        assertEquals(0, set.size())
        assertTrue(set.isEmpty())
        assertFalse("Hallo" in set)
        assertNull(set.get("Hallo"))
    }

    @Test
    fun test_one_element() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10)
        val firstElement = "Hallo"
        val prior = set.put(firstElement)
        assertNull(prior)
        assertEquals(1, set.size())
        assertFalse(set.isEmpty())
        assertTrue("Hallo" in set)
        assertTrue(firstElement === set.get("Hallo"))
    }

    @Test
    fun test_two_equal_elements() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10)
        val firstElement = "Hallo"
        val secondElement = "hallo"
        set.put(firstElement)
        val prior = set.put(secondElement)
        assertTrue(prior === firstElement)
        assertEquals(1, set.size())
        assertFalse(set.isEmpty())
        assertTrue("hallo" in set)
        assertTrue(secondElement === set.get("Hallo"))
    }

    @Test
    fun test_minimal_capacity() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10,1)
        val firstElement = "1st"
        val secondElement = "2nd"
        val prior1 = set.put(firstElement)
        assertNull(prior1)
        val prior2 = set.put(secondElement)
        assertNull(prior2)
        assertEquals(2, set.size())
        assertFalse(set.isEmpty())
        assertTrue("1st" in set)
        assertTrue("2nd" in set)
        assertTrue(firstElement === set.get("1st"))
        assertTrue(secondElement === set.get("2nd"))
        Log.d(TAG, "capacity: ${set.getCapacity()}")
        assertEquals(3, set.getCapacity())
    }

    @Test
    fun test_100_elements() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 100,1)
        for(i in 0 until 100) {
            val prior = set.put("element #$i")
            assertNull(prior)
            assertEquals(i + 1, set.size())
        }
        assertFalse(set.isEmpty())
        assertEquals(100, set.size())
        assertFalse("element #101" in set)
        for(i in 0 until 100) {
            assertTrue("element #$i" in set)
            assertNotNull(set.get("element #$i"))
        }
        Log.d(TAG, "capacity: ${set.getCapacity()}")
        assertEquals(255, set.getCapacity())
    }

    /**
     * The remove method does not remove the element from the queue.
     * That could cause a memory leak. So it's better to make it private.
    @Test
    fun test_remove() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10,1)
        val first = "1st"
        val second = "2nd"
        val third = "3rd"
        set.put(first)
        set.put(second)
        set.put(third)
        val removed = set.remove(second)
        assertEquals(2, set.size())
        assertEquals(second, removed)
        assertTrue(first in set)
        assertFalse(second in set)
        assertTrue(third in set)
    }
    */

    @Test
    fun size_limit_exceeded_by_one() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 2,1)
        val first = "1st"
        val second = "2nd"
        val third = "3rd"
        set.put(first)
        set.put(second)
        // todo bug: second element is removed from hash table!!!
        set.put(third)
        assertEquals(2, set.size())
        assertFalse(first in set)
        assertTrue(second in set)
        assertTrue(third in set)
    }

    @Test
    fun put_100_elements_with_size_limit_of_50() {
        val set = FifoHashSet(::hashCodeFunction, ::equalsFunction, 50,1)
        for(i in 0 until 100) {
            val prior = set.put("element #$i")
            assertNull(prior)
            assertEquals(Math.min(i + 1, 50), set.size())
        }
        assertFalse(set.isEmpty())
        assertEquals(50, set.size())
        assertFalse("element #101" in set)
        for(i in 0 until 50) {
            assertFalse("element #$i" in set)
            assertNull(set.get("element #$i"))
        }
        for(i in 50 until 100) {
            assertTrue("element #$i" in set)
            assertNotNull(set.get("element #$i"))
        }
        Log.d(TAG, "capacity: ${set.getCapacity()}")
        assertEquals(127, set.getCapacity())
    }

    companion object {
        private const val TAG = "balla.FifoHashSetTest"
    }
}