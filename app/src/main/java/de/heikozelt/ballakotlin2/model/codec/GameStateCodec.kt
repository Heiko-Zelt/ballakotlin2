package de.heikozelt.ballakotlin2.model.codec

import de.heikozelt.ballakotlin2.model.GameState

interface GameStateCodec {
        fun encodeNormalized(gameState: GameState): Array<Byte>

        fun decode(gameState: GameState, bytes: Array<Byte>)

        fun encodedSizeInNibbles(gameState: GameState): Int

        fun encodedSizeInBytes(gameState: GameState): Int
}