package de.heikozelt.ballakotlin2.model.codec

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class CodecPerformanceTest {

    @Test
    fun performance() {
        val gameStates = mutableListOf<GameState>()
        for (board in boards) {
            val gs = GameState()
            gs.fromAscii(board)
            gameStates.add(gs)
        }
        for (codec in codecs) {
            val duration = measureTimeMillis {
                for (i in 0 until 500) {
                    for (gs in gameStates) {
                        val bytes = codec.encodeNormalized(gs)
                        codec.decode(gs, bytes)
                    }
                }
            }
            Log.d(TAG, "codec: $codec, duration: $duration")
        }
    }

    companion object {
        private const val TAG = "balla.CodecPerformanceTest"

        val codecs = arrayOf(
            FixedSizeCodec, TerminatorCodec, SeparatorCodec,
            FixedSizeCodec, TerminatorCodec, SeparatorCodec,
            FixedSizeCodec, TerminatorCodec, SeparatorCodec)

        val boards = arrayOf(
            """
                    f 8 c _ 4 3 _ e _ 8 _ 4 _ 2 8 _ a d
                    6 5 4 _ 2 6 _ 9 _ 2 _ 8 _ 6 c _ b 9
                    1 c d _ d a _ f _ 2 7 f _ 2 6 _ 5 e
                    d 1 d _ 1 9 _ 1 _ c 7 a _ 5 8 4 1 6
                    b b 7 4 5 9 7 e _ 2 c 9 _ f 7 c e b
                    a b 6 c 5 9 4 b 7 d 7 2 _ f 1 3 e 8
                    a b 6 8 5 9 7 a 4 d 3 3 3 f 1 3 e 5
                    a b 6 8 5 9 3 f 3 d c e 4 f 1 2 e a
                    """.trimIndent(),
            """
                    8 _ _ _ _ 4 c _ 3 d f 8 _ 8 4 a e 2
                    c _ _ _ _ 2 4 _ 6 9 6 5 _ 2 8 b 9 6
                    6 _ _ _ _ d d _ a e 1 c 7 2 f 5 f 2
                    8 4 _ _ _ 1 d _ 9 6 d 1 7 c a 1 1 5
                    7 c _ 7 _ 5 7 4 9 b b b c 2 9 e e f
                    1 3 7 4 _ 5 6 c 9 8 a b 7 d 2 e b f
                    1 3 4 7 3 5 6 8 9 5 a b 3 d 3 e a f
                    1 2 3 3 4 5 6 8 9 a a b c d e e f f
                    """.trimIndent()
        )
    }
}