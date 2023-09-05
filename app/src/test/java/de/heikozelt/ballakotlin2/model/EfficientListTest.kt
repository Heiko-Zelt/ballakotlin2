package de.heikozelt.ballakotlin2.model

import android.util.Log
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EfficientListTest {

    @Test
    fun empty_EfficientList() {
        val list = EfficientList<String>()
        assertEquals(0, list.size)
        val iter = list.iterator()
        assertFalse(iter.hasNext())
    }

    @Test
    fun one_element() {
        val list = EfficientList<String>()
        val first = "1st element"
        list.add(first)
        assertEquals(1, list.size)
        val iter = list.iterator()
        assertTrue(iter.hasNext())
        assertEquals(first, iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun one_element_chunk_size_1() {
        val list = EfficientList<String>(1)
        val first = "1st element"
        list.add(first)
        assertEquals(1, list.size)
        val iter = list.iterator()
        assertTrue(iter.hasNext())
        assertEquals(first, iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun two_elements() {
        val list = EfficientList<String>()
        val first = "1st element"
        val second = "2nd element"
        list.add(first)
        list.add(second)
        assertEquals(2, list.size)
        val iter = list.iterator()
        iter.next()
        assertTrue(iter.hasNext())
        assertEquals(second, iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun two_elements_chunk_size_1() {
        val list = EfficientList<String>(1)
        val first = "1st element"
        val second = "2nd element"
        list.add(first)
        list.add(second)
        assertEquals(2, list.size)
        val iter = list.iterator()
        iter.next()
        assertTrue(iter.hasNext())
        assertEquals(second, iter.next())
        assertFalse(iter.hasNext())
    }

    @Test
    fun for_each() {
        val list = EfficientList<String>(4)
        for(i in 0 until 10) {
            list.add("element #$i")
        }
        var i = 0
        for(elem in list) {
            Log.d(TAG, "#$i: $elem")
            assertEquals("element #$i", elem)
            i++
        }
    }

    companion object {
        private const val TAG = "balla.EfficientListTest"
    }
}