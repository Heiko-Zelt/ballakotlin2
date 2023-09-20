package de.heikozelt.ballakotlin2.model.codec

import de.heikozelt.ballakotlin2.model.GameState
import kotlin.experimental.and

/**
 * Between tubes, there is a 0 as separator.
 * No separator is needed after last tube.
 * Separator is omitted, if a tube is full.
 * (any trailing zeros could be omitted)
 */
class SeparatorCodec {
    companion object : GameStateCodec {

        override fun encodeNormalized(gameState: GameState): Array<Byte> {
            val sortedTubes = gameState.tubes.copyOf()
            sortedTubes.sort()

            val bytes = Array<Byte>(encodedSizeInNibbles(gameState)) { 0 }
            var i = 0
            for(tubeIndex in sortedTubes.indices) {
                val tube = sortedTubes[tubeIndex]
                for(position in 0 until tube.fillLevel) {
                    bytes[i] = tube.cells[position]
                    i++
                }
                // separator?
                if(tube.fillLevel < gameState.tubeHeight) {
                    // bytes[i] = 0 ueberflüssig, da der Array mit Nullen initialisiert wurde
                    i++
                }
            }
            // copy from array of bytes to array with nibbles (2 elements per byte)
            // if size is odd, last nibble is filled with 0
            val bytes2 = Array<Byte>((i + 1) / 2) { 0 }
            var nibbleIndex = 0
            for(byteIndex in bytes2.indices) {
                val lowerNibble = bytes[nibbleIndex]
                nibbleIndex++
                var higherNibble = 0.toByte()
                if(nibbleIndex < bytes.size) {
                    higherNibble = bytes[nibbleIndex]
                    nibbleIndex++
                }
                bytes2[byteIndex] = (lowerNibble + (higherNibble * 16)).toByte()
            }
            return bytes2
        }


        override fun decode(gameState: GameState, bytes: Array<Byte>) {
            // copy array with nibbles to array of bytes
            val bytes2 = Array<Byte>(bytes.size * 2) { 0 }
            var nibbleIndex = 0
            for(byteIndex in bytes.indices) {
                val byte = bytes[byteIndex]
                val lowerNibble = byte and 0b00001111.toByte()
                bytes2[nibbleIndex] = lowerNibble
                nibbleIndex++
                val higherNibble = (byte.toInt() shr 4).toByte()
                bytes2[nibbleIndex] = higherNibble
                nibbleIndex++
            }

            var byteIndex = 0
            for(tubeIndex in gameState.tubes.indices) {
                val tube = gameState.tubes[tubeIndex]
                for(position in 0 until gameState.tubeHeight) {
                    val color = bytes2[byteIndex]
                    if(color != 0.toByte()) {
                        byteIndex++
                    }
                    tube.cells[position] = color
                }
                tube.repairFillLevel()
                // separator?
                if(tube.fillLevel < gameState.tubeHeight) {
                    byteIndex++
                }
            }
        }

        /**
         * The actual size usually is a little bit smaller,
         * if some tubes are fully filled and separators are emitted.
         * @return the maximum size
         */
        override fun encodedSizeInNibbles(gameState: GameState): Int {
            val numberOfBalls = gameState.numberOfColors * gameState.tubeHeight
            val numberOfSeparators = gameState.numberOfTubes - 1
            return numberOfBalls + numberOfSeparators
        }

        override fun encodedSizeInBytes(gameState: GameState): Int {
            val x = encodedSizeInNibbles(gameState) + 1
            return x shr 1
        }

    }
}