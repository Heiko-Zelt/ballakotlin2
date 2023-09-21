package de.heikozelt.ballakotlin2.model.codec

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
class FixedSizeCodecTest {

    @Test
    fun encodedSizeInNibbles_3x3() {
        val boardAscii = """
            _ 2 _
            1 2 _
            1 2 1""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 3 Röhren * 3 Bälle/Röhre
        assertEquals(3 * 3, FixedSizeCodec.encodedSizeInNibbles(gs))
    }

    @Test
    fun encodedSizeInNibbles_4x2() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 4 Röhren * 2 Bälle/Röhre
        assertEquals(4 * 2, FixedSizeCodec.encodedSizeInNibbles(gs))
    }

    @Test
    fun encodedSizeInBytes_3x3() {
        val boardAscii = """
            _ 2 _
            1 2 _
            1 2 1""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 5 bytes to store 9 nibbles ((3 * 3) + 1) / 2
        assertEquals(5, FixedSizeCodec.encodedSizeInBytes(gs))
    }

    @Test
    fun encodedSizeInBytes_4x2() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 4 bytes to store 8 nibbles
        assertEquals(4 * 2 / 2, FixedSizeCodec.encodedSizeInBytes(gs))
    }

    @Test
    fun encodeNormalized1() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        // original: 1 1 | 2 0 | 0 0 | 2 0
        // sorted: 0 0 | 1 1 | 2 0 | 2 0
        // nibbles: 00 11 20 20 -> 00 11 02 02
        val expected = arrayOf(0.toByte(), (1 * 16 + 1).toByte(), 2.toByte(), 2.toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = FixedSizeCodec.encodeNormalized(gs)
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
        // nibbles: 10 01 10 22 20 -> 01 10 01 22 02
        val expected = arrayOf(0x01.toByte(), 0x10.toByte(), 0x01.toByte(), 0x22.toByte(), 0x02.toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = FixedSizeCodec.encodeNormalized(gs)
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun encodeNormalized3() {
        val boardAscii = """
            1 _ _
            1 2 _
            2 1 2""".trimMargin()
        // original: 2 1 1 | 1 2 0 | 2 0 0
        // sorted: 1 2 0 | 2 0 0 | 2 1 1
        // nibbles: 12 02 00 21 10 -> 21 20 00 12 01
        val expected = arrayOf(0x21.toByte(), 0x20.toByte(), 0x00.toByte(), 0x12.toByte(), 0x01.toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = FixedSizeCodec.encodeNormalized(gs)
        assertArrayEquals(expected, bytes)
    }

    /*
    @Test
    fun encodeNormalized3() {
        val boardAscii = """
            f 8 c _ 4 3 _ e _ 8 _ 4 _ 2 8 _ a d
            6 5 4 _ 2 6 _ 9 _ 2 _ 8 _ 6 c _ b 9
            1 c d _ d a _ f _ 2 7 f _ 2 6 _ 5 e
            d 1 d _ 1 9 _ 1 _ c 7 a _ 5 8 4 1 6
            b b 7 4 5 9 7 e _ 2 c 9 _ f 7 c e b
            a b 6 c 5 9 4 b 7 d 7 2 _ f 1 3 e 8
            a b 6 8 5 9 7 a 4 d 3 3 3 f 1 3 e 5
            a b 6 8 5 9 3 f 3 d c e 4 f 1 2 e a""".trimMargin()
        /* sorted:
            8 _ _ _ _ 4 c _ 3 d f 8 _ 8 4 a e 2
            c _ _ _ _ 2 4 _ 6 9 6 5 _ 2 8 b 9 6
            6 _ _ _ _ d d _ a e 1 c 7 2 f 5 f 2
            8 4 _ _ _ 1 d _ 9 6 d 1 7 c a 1 1 5
            7 c _ 7 _ 5 7 4 9 b b b c 2 9 e e f
            1 3 7 4 _ 5 6 c 9 8 a b 7 d 2 e b f
            1 3 4 7 3 5 6 8 9 5 a b 3 d 3 e a f
            1 2 3 3 4 5 6 8 9 a a b c d e e f f

        bytes:


        encoded:
        */
            val expectedBytes = arrayOf(0x11.toByte(), 0x71.toByte(), ....)

            // original: 1 1 0 | 2 2 2 | 1 0 0
            // sorted: 1 0 0 | 1 1 0 | 2 2 2
            // bytes: 1 0 | 1 1 0 | 2 2 2
            // nibbles: 1 0 | 1 1 | 0 2 | 2 2
            // lower nibble right: 0 1 | 1 1 | 2 0 | 2 2

        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = FixedSizeCodec.encodeNormalized(gs)
        assertArrayEquals(expectedBytes, bytes)
    }
*/


    @Test
    fun decode1() {
        val bytes = arrayOf(0x11.toByte(), 0x02.toByte(), 0x00.toByte(), 0x02.toByte())
        val expectedBoardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(2, 2, 2)
        FixedSizeCodec.decode(gs, bytes)
        assertEquals(expected.toAscii(), gs.toAscii())
    }

    @Test
    fun decode2() {
        // 10 01 10 22 20 -> 01 10 01 22 02
        val bytes = arrayOf(0x01.toByte(), 0x10.toByte(), 0x01.toByte(), 0x22.toByte(), 0x02.toByte())
        val expectedBoardAscii = """
            _ _ 2
            _ 1 2
            1 1 2""".trimMargin()
        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(2, 1, 3)
        FixedSizeCodec.decode(gs, bytes)
        assertEquals(expected.toAscii(), gs.toAscii())
    }

    @Test
    fun decode3() {
        // 12 02 00 21 10 -> 21 20 00 12 01
        val bytes = arrayOf(0x21.toByte(), 0x20.toByte(), 0x00.toByte(), 0x12.toByte(), 0x01.toByte())
        val expectedBoardAscii = """
            _ _ 1
            2 _ 1
            1 2 2""".trimMargin()
        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(2, 1, 3)
        FixedSizeCodec.decode(gs, bytes)
        assertEquals(expected.toAscii(), gs.toAscii())
    }

    /*
    @Test
    fun decode3() {
        val bytes = arrayOf(0x11.toByte(), 0x71.toByte(), 0x68.toByte(), 0x8c.toByte(),
            0x32.toByte(), 0xc3.toByte(), 0x04.toByte(), 0x43.toByte(), 0x07.toByte(),
            0x73.toByte(), 0x74.toByte(), 0x40.toByte(), 0x03.toByte(), 0x55.toByte(),
            0x55.toByte(), 0xd1.toByte(), 0x42.toByte(), 0x66.toByte(), 0x76.toByte(),
            0xdd.toByte(), 0xc4.toByte(), 0x88.toByte(), 0x4c.toByte(),
            0x90.toByte(), 0x99.toByte(), 0x99.toByte(), 0x6a.toByte(), 0xa3.toByte(),
            0x85.toByte(), 0x6b.toByte(), 0x9e.toByte(), 0xad.toByte(), 0xaa.toByte(),
            0xdb.toByte(), 0x61.toByte(), 0xbf.toByte(), 0xbb.toByte(),
            0x1b.toByte(), 0x5c.toByte(), 0xc8.toByte(), 0x73.toByte(), 0x7c.toByte(),
            0x07.toByte(), 0xdd.toByte(), 0x2d.toByte(), 0x2c.toByte(), 0x82.toByte(),
            0x3e.toByte(), 0x92.toByte(), 0xfa.toByte(), 0x48.toByte(), 0xee.toByte(),
            0xee.toByte(), 0x51.toByte(), 0xab.toByte(), 0xaf.toByte(), 0xeb.toByte(),
            0xf1.toByte(), 0xe9.toByte(), 0xff.toByte(), 0xff.toByte(), 0x25.toByte(),
            0x26.toByte())
        val expectedBoardAscii = """
            8 _ _ _ _ 4 c _ 3 d f 8 _ 8 4 a e 2
            c _ _ _ _ 2 4 _ 6 9 6 5 _ 2 8 b 9 6
            6 _ _ _ _ d d _ a e 1 c 7 2 f 5 f 2
            8 4 _ _ _ 1 d _ 9 6 d 1 7 c a 1 1 5
            7 c _ 7 _ 5 7 4 9 b b b c 2 9 e e f
            1 3 7 4 _ 5 6 c 9 8 a b 7 d 2 e b f
            1 3 4 7 3 5 6 8 9 5 a b 3 d 3 e a f
            1 2 3 3 4 5 6 8 9 a a b c d e e f f""".trimIndent()
        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(15, 3, 8)
        FixedSizeCodec.decode(gs, bytes)
        val resultAsAscii = gs.toAscii()
        assertEquals(expectedBoardAscii, resultAsAscii)
    }
    */

    companion object {
        private const val TAG = "balla.FixedSizeCodec"
    }

}