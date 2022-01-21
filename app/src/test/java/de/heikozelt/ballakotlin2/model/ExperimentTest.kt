package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ExperimentTest {

    @Test
    fun array_in_list_of_arrays_true() {
        val a = arrayOf<Byte>(1, 2, 3)
        val list =
            mutableListOf(arrayOf<Byte>(7, 8, 9), arrayOf<Byte>(1, 2, 3), arrayOf<Byte>(1, 2, 13))
        //is not working val i = list.indexOf(a)
        val i = list.indexOfFirst { it contentEquals a}
        assertEquals(1, i)
    }

    @Test
    fun array_in_list_of_arrays_false() {
        val a = arrayOf<Byte>(1, 2, 3)
        val list =
            mutableListOf(arrayOf<Byte>(7, 8, 9), arrayOf<Byte>(3, 2, 3), arrayOf<Byte>(1, 2, 13))
        val i = list.indexOfFirst { it contentEquals a}
        assertEquals(-1, i)
    }
}