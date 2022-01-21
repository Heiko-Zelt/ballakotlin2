package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UtilsTest {

    @Test
    fun colorToChar() {
        assertEquals('_', colorToChar(0))
        assertEquals('1', colorToChar(1))
        assertEquals('9', colorToChar(9))
        assertEquals('a', colorToChar(10))
        assertEquals('f', colorToChar(15))
    }

    @Test
    fun charToColor() {
        assertEquals(0, charToColor('_'))
        assertEquals(1, charToColor('1'))
        assertEquals(9, charToColor('9'))
        assertEquals(10, charToColor('a'))
        assertEquals(15, charToColor('f'))
    }

    @Test
    fun allUnsolvable_2_false() {
        val sr1 = SearchResult().apply {
            status = SearchResult.STATUS_UNSOLVABLE
        }
        val sr2 = SearchResult().apply {
            status = SearchResult.STATUS_OPEN
        }
        val searchResults = listOf(sr1, sr2)
        assertFalse(allUnsolvable(searchResults))
    }

    @Test
    fun allUnsolvable_1_true() {
        val searchResults = listOf(
            SearchResult().apply {
                status = SearchResult.STATUS_UNSOLVABLE
            })
        assertTrue(allUnsolvable(searchResults))
    }

    @Test
    fun areEqual_false() {
        val list = mutableListOf(0, 0, 0, 42)
        assertFalse(areEqual(list))
    }

    @Test
    fun areEqual_true() {
        val list = mutableListOf(42)
        assertTrue(areEqual(list))
    }

    @Test
    fun listContainsArray_true() {
        val a = arrayOf<Byte>(1, 2, 3)
        val list =
            mutableListOf(arrayOf<Byte>(7, 8, 9), arrayOf<Byte>(1, 2, 3), arrayOf<Byte>(1, 2, 13))
        //is not working val i = list.indexOf(a)
        assertTrue(listContainsArray(list, a))
    }

    @Test
    fun listContainsArray_false() {
        val a = arrayOf<Byte>(1, 2, 3)
        val list =
            mutableListOf(arrayOf<Byte>(7, 8, 9), arrayOf<Byte>(3, 2, 3), arrayOf<Byte>(1, 2, 13))
        assertFalse(listContainsArray(list, a))
    }

    @Test
    fun listContainsArray_empty() {
        val a = arrayOf<Byte>(1, 2, 3)
        val list = mutableListOf<Array<Byte>>()
        assertFalse(listContainsArray(list, a))
    }
}