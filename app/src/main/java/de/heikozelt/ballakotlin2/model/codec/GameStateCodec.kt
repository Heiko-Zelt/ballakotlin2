package de.heikozelt.ballakotlin2.model.codec

import de.heikozelt.ballakotlin2.model.GameState

interface GameStateCodec {

        /**
         * Converts a GameState object into a dumb array,
         * which uses much less heap space.
         * Some information like width, height, move log is lost.
         */
        fun encodeNormalized(gameState: GameState): Array<Byte>

        /**
         * Converts a dumb array into a GameState object.
         * Width and height of the board in read and not changed.
         * The GameState object could be a return value,
         * but it's more efficient to reuse an existing object than
         * to create a new one on the heap.
         */
        fun decode(gameState: GameState, bytes: Array<Byte>)

        /**
         * The method could be private,
         * but for unit testing it's useful to have it publicly available.
         * @return actual or maximum size measured in nibbles (4 bit = half a byte)
         */
        fun encodedSizeInNibbles(gameState: GameState): Int

        /**
         * This is an important value to estimate heap usage.
         * @return actual size if size is fixed or maximum size if variable
         */
        fun encodedSizeInBytes(gameState: GameState): Int
}