package de.heikozelt.ballakotlin2.model.codec

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
class TerminatorCodecTest {

    @Test
    fun encodedSizeInNibbles_even() {
        val boardAscii = """
            _ 2 _
            1 2 _
            1 2 1""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 2 Farben * 3 Bälle + 3 Terminatoren
        assertEquals(2 * 3 + 3, TerminatorCodec.encodedSizeInNibbles(gs))
    }

    @Test
    fun encodedSizeInNibbles_odd() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 2 Farben * 2 Bälle + 4 Terminatoren
        assertEquals(2 * 2 + 4, TerminatorCodec.encodedSizeInNibbles(gs))
    }

    @Test
    fun encodedSizeInBytes_even() {
        val boardAscii = """
            _ 2 _
            1 2 _
            1 2 1""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        assertEquals(5, TerminatorCodec.encodedSizeInBytes(gs))
    }

    @Test
    fun encodedSizeInBytes_odd() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        val gs = GameState()
        gs.fromAscii(boardAscii)
        // 4 bytes to store 7 nibbles
        assertEquals(4, TerminatorCodec.encodedSizeInBytes(gs))
    }

    @Test
    fun encodeNormalized1() {
        val boardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        // original: 1 1 | 2 0 | 0 0 | 2 0
        // sorted: 0 0 | 1 1 | 2 0 | 2 0
        // bytes: 0 | 1 1 0 | 2 0 | 2 0
        // nibbles: 01 10 20 20
        // lower nibble right: 10 01 02 02
        val expected = arrayOf(0x10.toByte(), 0x01.toByte(), 0x02.toByte(), 0x02.toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = TerminatorCodec.encodeNormalized(gs)
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
        // bytes: 1 0 | 1 1 0 | 2 2 2 0
        // nibbles: 10 11 02 22 00
        // lower nibble right: 01 11 20 22 00
        val expected = arrayOf(0x01.toByte(), 0x11.toByte(), 0x20.toByte(), 0x22.toByte(), 0x00.toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = TerminatorCodec.encodeNormalized(gs)
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun encodeNormalized3() {
        // Höhe 8, 15 Farben, 3 zusätzliche Röhren
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
            111786c80 | 233c40 | 3470 | 37470 | 430 | 55551d240 | 6667dd4c0 | 88c40 |
            99999a630 | a58b6e9d0 | aaabd16f0 | bbbb1c580 | c37c770 | ddd2c2280 |
            e329af840 | eeee15ba0 | fabe1f9e0 | ffff52620

        nibbles:
            11 17 86 c8 02 33 c4 03 47 03 74 70 43 05 55 51 d2 40 66 67 dd 4c 08 8c 40
            99 99 9a 63 0a 58 b6 e9 d0 aa ab d1 6f 0b bb b1 c5 80 c3 7c 77 0d dd 2c 22 80
            e3 29 af 84 0e ee e1 5b a0 fa be 1f 9e 0f ff f5 26 20

        lower nibbles right:
            11 71 68 8c 20 33 4c 30 74 30 47 07 34 50 55 15 2d 04 66 76 dd c4 80 c8 04
            99 99 a9 36 a0 85 6b 9e 0d aa ba 1d f6 b0 bb 1b 5c 08 3c c7 77 d0 dd c2 22 08
            3e 92 fa 48 e0 ee 1e b5 0a af eb f1 e9 f0 ff 5f 62 02

        encoded:
        */
            val expectedBytes = arrayOf(0x11.toByte(), 0x71.toByte(), 0x68.toByte(), 0x8c.toByte(),
                0x20.toByte(), 0x33.toByte(), 0x4c.toByte(), 0x30.toByte(), 0x74.toByte(),
                0x30.toByte(), 0x47.toByte(), 0x07.toByte(), 0x34.toByte(), 0x50.toByte(),
                0x55.toByte(), 0x15.toByte(), 0x2d.toByte(), 0x04.toByte(), 0x66.toByte(),
                0x76.toByte(), 0xdd.toByte(), 0xc4.toByte(), 0x80.toByte(), 0xc8.toByte(),
                0x04.toByte(),
                0x99.toByte(), 0x99.toByte(), 0xa9.toByte(), 0x36.toByte(), 0xa0.toByte(),
                0x85.toByte(), 0x6b.toByte(), 0x9e.toByte(), 0x0d.toByte(), 0xaa.toByte(),
                0xba.toByte(), 0x1d.toByte(), 0xf6.toByte(), 0xb0.toByte(), 0xbb.toByte(),
                0x1b.toByte(), 0x5c.toByte(), 0x08.toByte(), 0x3c.toByte(), 0xc7.toByte(),
                0x77.toByte(), 0xd0.toByte(), 0xdd.toByte(), 0xc2.toByte(), 0x22.toByte(),
                0x08.toByte(),
                0x3e.toByte(), 0x92.toByte(), 0xfa.toByte(), 0x48.toByte(), 0xe0.toByte(),
                0xee.toByte(), 0x1e.toByte(), 0xb5.toByte(), 0x0a.toByte(), 0xaf.toByte(),
                0xeb.toByte(), 0xf1.toByte(), 0xe9.toByte(), 0xf0.toByte(), 0xff.toByte(),
                0x5f.toByte(), 0x62.toByte(), 0x02.toByte())
        val gs = GameState()
        gs.fromAscii(boardAscii)
        val bytes = TerminatorCodec.encodeNormalized(gs)
        assertArrayEquals(expectedBytes, bytes)
    }



    @Test
    fun decode1() {
        val bytes = arrayOf(0x11.toByte(), 0x20.toByte(), 0x00.toByte(), 0x02.toByte())
        val expectedBoardAscii = """
            1 _ _ _
            1 2 _ 2""".trimMargin()
        // but was
        // 1 _ _ _
        // 1 _ 2 _

        // nibbles: 11 20 00 02
        // bytes: 11 02 00 20
        // tubes: 1 1 0 | 2 0 | 0 | 2 0

        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(2, 2, 2)
        TerminatorCodec.decode(gs, bytes)
        assertEquals(expected.toAscii(), gs.toAscii())
    }

    @Test
    fun decode2() {
        val bytes = arrayOf(0x01.toByte(), 0x11.toByte(), 0x20.toByte(), 0x22.toByte(), 0x00.toByte())
        val expectedBoardAscii = """
            _ _ 2
            _ 1 2
            1 1 2""".trimMargin()
        // nibbles: 01 11 20 22 00
        // bytes: 10 11 02 22 00
        // tubes: 1 0 | 1 1 0 | 2 2 2 0

        val expected = GameState()
        expected.fromAscii(expectedBoardAscii)
        val gs = GameState()
        gs.resize(2, 1, 3)
        TerminatorCodec.decode(gs, bytes)
        assertEquals(expected.toAscii(), gs.toAscii())
    }

    @Test
    fun decode3() {
        /* to do */
        val bytes = arrayOf(0x11.toByte(), 0x71.toByte(), 0x68.toByte(), 0x8c.toByte(),
            0x20.toByte(), 0x33.toByte(), 0x4c.toByte(), 0x30.toByte(), 0x74.toByte(),
            0x30.toByte(), 0x47.toByte(), 0x07.toByte(), 0x34.toByte(), 0x50.toByte(),
            0x55.toByte(), 0x15.toByte(), 0x2d.toByte(), 0x04.toByte(), 0x66.toByte(),
            0x76.toByte(), 0xdd.toByte(), 0xc4.toByte(), 0x80.toByte(), 0xc8.toByte(),
            0x04.toByte(),
            0x99.toByte(), 0x99.toByte(), 0xa9.toByte(), 0x36.toByte(), 0xa0.toByte(),
            0x85.toByte(), 0x6b.toByte(), 0x9e.toByte(), 0x0d.toByte(), 0xaa.toByte(),
            0xba.toByte(), 0x1d.toByte(), 0xf6.toByte(), 0xb0.toByte(), 0xbb.toByte(),
            0x1b.toByte(), 0x5c.toByte(), 0x08.toByte(), 0x3c.toByte(), 0xc7.toByte(),
            0x77.toByte(), 0xd0.toByte(), 0xdd.toByte(), 0xc2.toByte(), 0x22.toByte(),
            0x08.toByte(),
            0x3e.toByte(), 0x92.toByte(), 0xfa.toByte(), 0x48.toByte(), 0xe0.toByte(),
            0xee.toByte(), 0x1e.toByte(), 0xb5.toByte(), 0x0a.toByte(), 0xaf.toByte(),
            0xeb.toByte(), 0xf1.toByte(), 0xe9.toByte(), 0xf0.toByte(), 0xff.toByte(),
            0x5f.toByte(), 0x62.toByte(), 0x02.toByte())
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
        TerminatorCodec.decode(gs, bytes)
        val resultAsAscii = gs.toAscii()
        assertEquals(expectedBoardAscii, resultAsAscii)
    }

    companion object {
        private const val TAG = "balla.TerminatorCodec"
    }

}