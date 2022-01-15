package de.heikozelt.ballakotlin2.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilsTest {

    @Test
    fun durationsToFractions_1() {
        val durations = arrayOf(100f, 200f, 300f, 400f)
        val wholeDuration = durations.sum()
        assertEquals(1000f, wholeDuration)

        val fractions = durationsToFractions(durations, wholeDuration)
        assertEquals(3, fractions.size)
        assertEquals(0.1f,fractions[0]) // 0.1f
        assertEquals(0.3f,fractions[1]) // 0.1f + 0.2f
        assertEquals(0.6f,fractions[2]) // 0.1f + 0.2f + 0.3f
    }

    @Test
    fun diagonalDistance_a_b() {
        // 5^2 = 3^2 + 4^2
        // 25 = 9 + 16
        assertEquals(5, diagonalDistance(3, 4))
    }

    @Test
    fun diagonalDistance_x1_x2_y1_y2() {
        // 5^2 = 3^2 + 4^2
        // 25 = 9 + 16
        assertEquals(5, diagonalDistance(0,3, 0,4))
    }
}