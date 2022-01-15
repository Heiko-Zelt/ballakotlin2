package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.assertEquals
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
}