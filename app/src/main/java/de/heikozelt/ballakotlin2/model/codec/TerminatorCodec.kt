package de.heikozelt.ballakotlin2.model.codec

import de.heikozelt.ballakotlin2.model.GameState
import java.util.Arrays
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

            val bytes2 = deflate(bytes, i)

            return bytes2
        }


        override fun decode(gameState: GameState, bytes: Array<Byte>) {
            val bytes2 = Array<Byte>(bytes.size * 2) { 0 }
            inflate(bytes, bytes2)

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
                /*
                if(tube.fillLevel < gameState.tubeHeight) {
                    Arrays.fill(tube.cells, tube.fillLevel, gameState.tubeHeight, 0.toByte())
                }
                */
                for(emptyPosition in tube.fillLevel until gameState.tubeHeight) {
                    tube.cells[emptyPosition] = 0.toByte()
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