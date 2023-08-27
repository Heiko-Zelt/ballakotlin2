package de.heikozelt.ballakotlin2.model

import android.util.Log
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SpecialArrayTest {

    @Test
    fun test_equals() {
        val a1 = SpecialArray(arrayOf(1, 2, 3))
        val a2 = SpecialArray(arrayOf(1, 2, 3))
        assertEquals(a1, a2)
    }

    @Test
    fun test_hashCode() {
        val a1 = SpecialArray(arrayOf(1, 2, 3))
        val a2 = SpecialArray(arrayOf(1, 2, 3))
        Log.d(TAG, "a1.hashCode: ${a1.hashCode()}")
        Log.d(TAG, "a2.hashCode: ${a2.hashCode()}")
        assertEquals(a1.hashCode(), a2.hashCode())
    }

    @Test
    fun test_in_set() {
        val a1 = SpecialArray(arrayOf(1, 2, 3))
        val a2 = SpecialArray(arrayOf(1, 2, 3))
        val set = hashSetOf(a1)
        assertTrue(a2 in set)
    }

    @Test
    fun test_not_in_set() {
        val a1 = SpecialArray(arrayOf(1, 2, 3))
        val a2 = SpecialArray(arrayOf(2, 1, 3))
        val set = hashSetOf(a1)
        assertFalse(a2 in set)
    }

    companion object {
        private const val TAG = "balla.SpecialArrayTest"
    }
}