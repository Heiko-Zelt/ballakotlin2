package de.heikozelt.ballakotlin2.model.codec

import android.util.Log
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NibblesTest {

    @Test
    fun deflate0_1_2_3__15() {
        val input = Array(16) { it.toByte() }
        val expected = arrayOf(0x10.toByte(), 0x32.toByte(), 0x54.toByte(), 0x76.toByte(),
            0x98.toByte(), 0xba.toByte(), 0xdc.toByte(), 0xfe.toByte())
        val output = deflate(input)
        Log.d(TAG, "output: ${output.joinToString()}")
        assertArrayEquals(expected, output)
    }

    @Test
    fun deflate0_1_2() {
        val input = Array(3) { it.toByte() }
        val expected = arrayOf(0x10.toByte(), 0x02.toByte())
        val output = deflate(input)
        Log.d(TAG, "output: ${output.joinToString()}")
        assertArrayEquals(expected, output)
    }

    @Test
    fun inflate0_1_2_3__15() {
        val input = arrayOf(0x10.toByte(), 0x32.toByte(), 0x54.toByte(), 0x76.toByte(),
            0x98.toByte(), 0xba.toByte(), 0xdc.toByte(), 0xfe.toByte())
        val output = Array(16) { (99 - it).toByte() }
        val expected = Array(16) { it.toByte() }
        inflate(input, output)
        Log.d(TAG, "output: ${output.joinToString()}")
        assertArrayEquals(expected, output)
    }

    @Test
    fun inflate0_1_2() {
        val input = arrayOf(0x10.toByte(), 0xf2.toByte()) // last nibble is ignored
        val output = Array(3) { (99 - it).toByte() }
        val expected = Array(3) { it.toByte() }
        inflate(input, output)
        Log.d(TAG, "output: ${output.joinToString()}")
        assertArrayEquals(expected, output)
    }

    companion object {
        private const val TAG = "balla.NibblesTest"
    }
}