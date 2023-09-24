package de.heikozelt.ballakotlin2.model.codec

import de.heikozelt.ballakotlin2.model.GameState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class GenericCodecTest {

    /**
     * Codecs encode differently,
     * but after encoding and decoding the results must be the same.
     */
    @ParameterizedTest
    @MethodSource("provideData")
    fun encode_decode(expectedBoard: String, gs: GameState, codec: GameStateCodec) {
        val bytes = codec.encodeNormalized()
        gs.rainbow()
        codec.decode(bytes)
        Assertions.assertEquals(expectedBoard, gs.toAscii())
    }

    companion object {
        /**
         * @return a stream of Arguments. Arguments are a pair of boards and a codec.
         * 0: Pair(originalBoard0, expectedBoard0), FixedSizeCodec
         * 1: Pair(originalBoard0, expectedBoard0), SeparatorCodec
         * 2: Pair(originalBoard1, expectedBoard1), FixedSizeCodec
         * 3: Pair(originalBoard1, expectedBoard1), SeparatorCodec
         * ...
         */
        @JvmStatic
        fun provideData(): Stream<Arguments> {
            val datas = arrayOf(
                Pair(
                    """
                    _ 1 _
                    2 1 2
                    """.trimIndent(),
                    """
                    1 _ _
                    1 2 2
                    """.trimIndent()
                ),
                Pair(
                    """
                    _ 1 _
                    _ 1 2
                    2 1 2
                    """.trimIndent(),
                    """
                    1 _ _
                    1 _ 2
                    1 2 2
                    """.trimIndent()
                ),
                Pair(
                    """
                    _ _ _
                    1 1 2
                    2 1 2
                    """.trimIndent(),
                    """
                    _ _ _
                    1 1 2
                    1 2 2
                    """.trimIndent()
                ),
                Pair(
                    """
                    1 _ _
                    1 2 _
                    2 1 2
                    """.trimIndent(),
                    """
                    _ _ 1
                    2 _ 1
                    1 2 2
                    """.trimIndent()
                ),
                Pair(
                    """
                    1 _ 2
                    1 _ 2
                    1 _ 2
                    """.trimIndent(),
                    """
                    _ 1 2
                    _ 1 2
                    _ 1 2
                    """.trimIndent()
                ),
                Pair(
                    """
                    c a 5 c 8 9 d 6 2 _ 1 4 _ _ b f
                    3 a 5 c 1 9 d 6 2 e 7 4 3 8 b f
                    5 3 e a 1 9 d 7 2 e 7 4 f 8 b 6
                    """.trimIndent(),
                    """
                    8 2 a 4 c f 6 1 _ 9 c b d 5 _ _
                    1 2 a 4 3 f 6 7 8 9 c b d 5 e 3
                    1 2 3 4 5 6 7 7 8 9 a b d e e f
                    """.trimIndent()
                ),
                Pair(
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
            )
            var stream = Stream.of<Arguments>()
            datas.forEach { boards ->
                val gs = GameState()
                gs.fromAscii(boards.first)
                val codecs = mutableListOf<GameStateCodec>()
                codecs.add(FixedSizeCodec(gs))
                codecs.add(TerminatorCodec(gs))
                codecs.add(SeparatorCodec(gs))
                codecs.forEach { codec ->
                    stream = Stream.concat(stream, Stream.of(arguments(boards.second, gs, codec)))
                }
            }
            return stream
        }
    }
}