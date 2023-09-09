package de.heikozelt.ballakotlin2.model

import android.util.Log
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FifoHashSetTest {

    private fun equalsFunction(a: String, b: String): Boolean {
        return a.lowercase() == b.lowercase()
    }

    private fun hashCodeFunction(s: String): Int {
        return s.lowercase().hashCode()
    }

    private fun count(iterator: Iterator<Any>): Int {
        var i = 0
        for(element in iterator) i++
        return i
    }

    @Test
    fun test_count() {
        val list = listOf("a", "b")
        assertEquals(2, count(list.iterator()))
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
        val hashSet = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10)
        assertEquals(0, hashSet.size())
        assertTrue(hashSet.isEmpty())
        assertFalse("Hallo" in hashSet)
        assertNull(hashSet.get("Hallo"))
        assertFalse(hashSet.iterator().hasNext())
        assertFalse(hashSet.hashedIterator().hasNext())
        assertEquals(0, count(hashSet.iterator()))
        assertEquals(0, count(hashSet.hashedIterator()))
    }

    @Test
    fun test_one_element() {
        val hashSet = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10)
        val firstElement = "Hallo"
        val prior = hashSet.put(firstElement)
        assertNull(prior)
        assertEquals(1, hashSet.size())
        assertFalse(hashSet.isEmpty())
        assertTrue("Hallo" in hashSet)
        assertTrue(firstElement === hashSet.get("Hallo"))
        assertEquals(1, count(hashSet.iterator()))
        assertEquals(1, count(hashSet.hashedIterator()))
    }

    @Test
    fun test_two_equal_elements() {
        val hashSet = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10)
        val firstElement = "Hallo"
        val secondElement = "hallo"
        hashSet.put(firstElement)
        val prior = hashSet.put(secondElement)
        assertTrue(prior === firstElement)
        assertEquals(1, hashSet.size())
        assertFalse(hashSet.isEmpty())
        assertTrue("hallo" in hashSet)
        assertTrue(secondElement === hashSet.get("Hallo"))
        assertEquals(1, count(hashSet.iterator()))
        assertEquals(1, count(hashSet.hashedIterator()))
    }

    @Test
    fun test_minimal_capacity() {
        val hashSet = FifoHashSet(::hashCodeFunction, ::equalsFunction, 10,1)
        val firstElement = "1st"
        val secondElement = "2nd"
        val prior1 = hashSet.put(firstElement)
        assertNull(prior1)
        val prior2 = hashSet.put(secondElement)
        assertNull(prior2)
        assertEquals(2, hashSet.size())
        assertFalse(hashSet.isEmpty())
        assertTrue("1st" in hashSet)
        assertTrue("2nd" in hashSet)
        assertTrue(firstElement === hashSet.get("1st"))
        assertTrue(secondElement === hashSet.get("2nd"))
        Log.d(TAG, "capacity: ${hashSet.getCapacity()}")
        assertEquals(3, hashSet.getCapacity())
        assertEquals(2, count(hashSet.iterator()))
        assertEquals(2, count(hashSet.hashedIterator()))
    }

    @Test
    fun test_100_elements() {
        val hashSet = FifoHashSet(::hashCodeFunction, ::equalsFunction, 100,1)
        for(i in 0 until 100) {
            val prior = hashSet.put("element #$i")
            assertNull(prior)
            assertEquals(i + 1, hashSet.size())
        }
        assertFalse(hashSet.isEmpty())
        assertEquals(100, hashSet.size())
        assertFalse("element #101" in hashSet)
        for(i in 0 until 100) {
            assertTrue("element #$i" in hashSet)
            assertNotNull(hashSet.get("element #$i"))
        }
        Log.d(TAG, "capacity: ${hashSet.getCapacity()}")
        assertEquals(255, hashSet.getCapacity())
        assertEquals(100, count(hashSet.iterator()))
        assertEquals(100, count(hashSet.hashedIterator()))
        for(element in hashSet) {
            assertTrue(element in hashSet)
        }
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
        val hashSet = FifoHashSet(::hashCodeFunction, ::equalsFunction, 2,1)
        val first = "1st"
        val second = "2nd"
        val third = "3rd"
        hashSet.put(first)
        hashSet.put(second)
        hashSet.put(third)
        assertEquals(2, hashSet.size())
        assertFalse(first in hashSet)
        assertTrue(second in hashSet)
        assertTrue(third in hashSet)
        assertEquals(2, count(hashSet.iterator()))
        assertEquals(2, count(hashSet.hashedIterator()))
    }

    @Test
    fun put_100_elements_with_size_limit_of_50() {
        val hashSet = FifoHashSet(::hashCodeFunction, ::equalsFunction, 50,1)
        for(i in 0 until 100) {
            val prior = hashSet.put("element #$i")
            assertNull(prior)
            assertEquals(Math.min(i + 1, 50), hashSet.size())
        }
        assertFalse(hashSet.isEmpty())
        assertEquals(50, hashSet.size())
        assertFalse("element #101" in hashSet)
        for(i in 0 until 50) {
            assertFalse("element #$i" in hashSet)
            assertNull(hashSet.get("element #$i"))
        }
        for(i in 50 until 100) {
            assertTrue("element #$i" in hashSet)
            assertNotNull(hashSet.get("element #$i"))
        }
        Log.d(TAG, "capacity: ${hashSet.getCapacity()}")
        assertEquals(127, hashSet.getCapacity())
        assertEquals(50, count(hashSet.iterator()))
        assertEquals(50, count(hashSet.hashedIterator()))
        for(element in hashSet) {
            assertTrue(element in hashSet)
        }
    }

    companion object {
        private const val TAG = "balla.FifoHashSetTest"
    }
}