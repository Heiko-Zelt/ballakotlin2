package de.heikozelt.ballakotlin2.model

import android.util.Log
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EfficientListTest {

    @Test
    fun empty_EfficientList() {
        val list = EfficientList<String>()
        assertEquals(0, list.getSize())
        val iterator = list.iterator()
        assertFalse(iterator.hasNext())
        assertEquals(0, list.numberOfChunks())
        assertEquals(0, list.getCapacity())
    }

    @Test
    fun one_element() {
        val list = EfficientList<String>()
        val first = "1st element"
        list.add(first)
        assertEquals(1, list.getSize())
        val iterator = list.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(first, iterator.next())
        assertFalse(iterator.hasNext())
        assertEquals(1, list.numberOfChunks())
        assertEquals(32, list.getCapacity())
    }

    @Test
    fun one_element_chunk_size_1() {
        val list = EfficientList<String>(1, 1)
        val first = "1st element"
        list.add(first)
        assertEquals(1, list.getSize())
        val iterator = list.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(first, iterator.next())
        assertFalse(iterator.hasNext())
        assertEquals(1, list.numberOfChunks())
        assertEquals(1, list.getCapacity())
    }

    @Test
    fun two_elements() {
        val list = EfficientList<String>()
        val first = "1st element"
        val second = "2nd element"
        list.add(first)
        list.add(second)
        assertEquals(2, list.getSize())
        val iterator = list.iterator()
        iterator.next()
        assertTrue(iterator.hasNext())
        assertEquals(second, iterator.next())
        assertFalse(iterator.hasNext())
        assertEquals(1, list.numberOfChunks())
        assertEquals(32, list.getCapacity())
    }

    @Test
    fun two_elements_chunk_size_1() {
        val list = EfficientList<String>(2, 1)
        val first = "1st element"
        val second = "2nd element"
        list.add(first)
        list.add(second)
        assertEquals(2, list.getSize())
        val iterator = list.iterator()
        iterator.next()
        assertTrue(iterator.hasNext())
        assertEquals(second, iterator.next())
        assertFalse(iterator.hasNext())
        assertEquals(2, list.numberOfChunks())
        assertEquals(2, list.getCapacity())
    }

    @Test
    fun for_each() {
        val list = EfficientList<String>(12, 4)
        for(i in 0 until 10) {
            list.add("element #$i")
        }
        var i = 0
        for(elem in list) {
            Log.d(TAG, "#$i: $elem")
            assertEquals("element #$i", elem)
            i++
        }
        assertEquals(3, list.numberOfChunks())
        assertEquals(12, list.getCapacity())
    }

    @Test
    fun capacity_limit_0_exceeded() {
        val list = EfficientList<String>(0, 3)
        assertThrows(EfficientList.CapacityLimitExceededException::class.java) {
            list.add("1st element")
        }
        assertEquals(0, list.getSize())
        assertEquals(0, list.numberOfChunks())
        assertEquals(0, list.getCapacity())
    }

    @Test
    fun capacity_limit_1_exceeded() {
        val list = EfficientList<String>(1, 1)
        list.add("1st element")
        assertThrows(EfficientList.CapacityLimitExceededException::class.java) {
            list.add("2nd element")
        }
        assertEquals(1, list.getSize())
        assertEquals(1, list.numberOfChunks())
        assertEquals(1, list.getCapacity())
    }

    @Test
    fun capacity_limit_3_exceeded() {
        val list = EfficientList<String>(3, 2)
        list.add("1st element")
        list.add("2nd element")
        assertThrows(EfficientList.CapacityLimitExceededException::class.java) {
            list.add("3rd element")
        }
        assertEquals(2, list.getSize())
        assertEquals(1, list.numberOfChunks())
        assertEquals(2, list.getCapacity())
    }

    @Test
    fun capacity_limit_4_exceeded() {
        val list = EfficientList<String>(4, 2)
        list.add("1st element")
        list.add("2nd element")
        list.add("3rd element")
        list.add("4th element")
        assertThrows(EfficientList.CapacityLimitExceededException::class.java) {
            list.add("5th element")
        }
        assertEquals(4, list.getSize())
        assertEquals(2, list.numberOfChunks())
        assertEquals(4, list.getCapacity())
    }

    companion object {
        private const val TAG = "balla.EfficientListTest"
    }
}