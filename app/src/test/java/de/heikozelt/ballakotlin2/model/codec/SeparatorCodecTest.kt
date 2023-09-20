package de.heikozelt.ballakotlin2.model.codec

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
class SeparatorCodecTest {

    @Test
    fun encodedSizeInNibbles_even() {
        val boardAscii = """
            _ 2 _
            1 2 _
            1 2 1""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 2 Farben * 3 Bälle + 2 Trenner
        assertEquals(2 * 3 + (3 - 1), SeparatorCodec.encodedSizeInNibbles(gs))
    }

    @Test
    fun encodedSizeInNibbles_odd() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 2 Farben * 2 Bälle + 3 Trenner
        assertEquals(2 * 2 + (4 - 1), SeparatorCodec.encodedSizeInNibbles(gs))
    }

    @Test
    fun encodedSizeInBytes_even() {
        val boardAscii = """
            _ 2 _
            1 2 _
            1 2 1""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        assertEquals(8 / 2, SeparatorCodec.encodedSizeInBytes(gs))
    }

    @Test
    fun encodedSizeInBytes_odd() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 4 bytes to store 7 nibbles
        assertEquals(4, SeparatorCodec.encodedSizeInBytes(gs))
    }

    @Test
    fun encodeNormalized1() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        // original: 1 1 | 2 0 | 0 0 | 2 0
        // sorted: 0 0 | 1 1 | 2 0 | 2 0
        // bytes: 0 | 1 1 | 2 0 | 2 0
        // nibbles: 0 1 | 1 2 | 0 2 | 0
        // lower nibble right: 1 0 | 2 1 | 2 0 | 0 0
        val expected = arrayOf((1 * 16).toByte(), (2 * 16 + 1).toByte(), (2 * 16).toByte(), 0.toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = SeparatorCodec.encodeNormalized(gs)
        bytes.forEach {
            Log.d(TAG, "result: $it")
        }
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun encodeNormalized2() {
        val boardAscii = """
            _ 2 _
            1 2 _
            1 2 1""".trimMargin()
        // original: 1 1 0 | 2 2 2 | 1 0 0
        // sorted: 1 0 0 | 1 1 0 | 2 2 2
        // bytes: 1 0 | 1 1 0 | 2 2 2
        // nibbles: 1 0 | 1 1 | 0 2 | 2 2
        // lower nibble right: 0 1 | 1 1 | 2 0 | 2 2
        val expected = arrayOf(1.toByte(), (1 * 16 + 1).toByte(), (2 * 16).toByte(), (2 * 16 + 2).toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = SeparatorCodec.encodeNormalized(gs)
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun decode1() {
        val bytes = arrayOf((1 * 16 + 1).toByte(), 2.toByte(), (2 * 16).toByte(), 0.toByte())
        val expectedBoardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        // lower nibble right: 1 1 | 0 2 | 2 0 | 0 0
        // nibbles: 1 1 | 2 0 | 0 2 | 0 0
        // bytes: 1 1 | 2 0 | 0 | 2 0
        // tubes: 1 1 | 2 0 | 0 0 | 2 0

        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(2, 2, 2)
        SeparatorCodec.decode(gs, bytes)
        assertEquals(expected.toAscii(), gs.toAscii())
    }

    @Test
    fun decode2() {
        val bytes = arrayOf(1.toByte(), (1 * 16 + 1).toByte(), (2 * 16).toByte(), (2 * 16 + 2).toByte())
        val expectedBoardAscii = """
            _ _ 2
            _ 1 2
            1 1 2""".trimMargin()
        // lower nibble right: 0 1 | 1 1 | 2 0 | 2 2
        // nibbles: 1 0 | 1 1 | 0 2 | 2 2
        // bytes: 1 0 | 1 1 0 | 2 2 2
        // tubes: 1 0 0 | 1 1 0 | 2 2 2

        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(2, 1, 3)
        SeparatorCodec.decode(gs, bytes)
        assertEquals(expected.toAscii(), gs.toAscii())
    }

    companion object {
        private const val TAG = "balla.SeparatorCodec"
    }

}