package de.heikozelt.ballakotlin2.model.codec

import de.heikozelt.ballakotlin2.model.GameState
import kotlin.experimental.and

/**
 * After the ball color codes of each tube there is a 0 as terminator
 */
class TerminatorCodec {
    companion object : GameStateCodec {

        override fun encodeNormalized(gameState: GameState): Array<Byte> {
            val sortedTubes = gameState.tubes.copyOf()
            sortedTubes.sort()

            val bytes = Array<Byte>(encodedSizeInNibbles(gameState)) { 0 }
            var i = 0
            for(tube in sortedTubes) {
                for(position in 0 until tube.fillLevel) {
                    bytes[i] = tube.cells[position]
                    i++
                }
                // terminator
                bytes[i] = 0
                i++
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
                // seltsame Berechung, aber Bytes sind in Java eigentlich vorzeichenbehaftet
                val higherNibble = ((byte.toInt() shr 4) and 0b00001111).toByte()
                bytes2[nibbleIndex] = higherNibble
                nibbleIndex++
            }

            var i = 0
            for(tube in gameState.tubes) {
                tube.fillLevel = 0
                while(true) {
                    val color = bytes2[i]
                    i++
                    if(color == 0.toByte()) break
                    tube.cells[tube.fillLevel] = color
                    tube.fillLevel++
                }
                for(emptyPosition in tube.fillLevel until gameState.tubeHeight) {
                    tube.cells[emptyPosition] = 0
                }
            }
        }

        /**
         * @return constant size depending on dimensions of board and number of balls
         */
        override fun encodedSizeInNibbles(gameState: GameState): Int {
            val numberOfBalls = gameState.numberOfColors * gameState.tubeHeight
            return numberOfBalls + gameState.numberOfTubes
        }

        override fun encodedSizeInBytes(gameState: GameState): Int {
            val x = encodedSizeInNibbles(gameState) + 1
            return x shr 1
        }

    }
}