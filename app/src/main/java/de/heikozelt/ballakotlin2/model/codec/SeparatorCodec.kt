package de.heikozelt.ballakotlin2.model.codec

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import java.util.Arrays
import kotlin.experimental.and

/**
 * Between tubes, there is a 0 as separator.
 * No separator is needed after last tube.
 * Separator is omitted, if a tube is full.
 * (any trailing zeros could be omitted)
 */
class SeparatorCodec(private val gameState: GameState) : GameStateCodec {
    val bytes = Array<Byte>(encodedSizeInNibbles()) { 0 }

    override fun encodeNormalized(): Array<Byte> {
        Log.d(TAG, "encode: ${gameState.toAscii()}")
        val sortedTubes = gameState.tubes.copyOf()
        sortedTubes.sort()

        //Arrays.fill(bytes, 0.toByte())

        var i = 0
        val maxTubeIndex = sortedTubes.size - 1
        for (tubeIndex in sortedTubes.indices) {
            val tube = sortedTubes[tubeIndex]
            for (position in 0 until tube.fillLevel) {
                bytes[i] = tube.cells[position]
                i++
            }
            // separator?
            // nur wenn nicht voll und nicht bei letzter Röhre
            if ((tube.fillLevel < gameState.tubeHeight) && (tubeIndex != maxTubeIndex)) {
                bytes[i] = 0
                i++
            }
        }

        val bytes2 = deflate(bytes, i)
        return bytes2
    }


    override fun decode(bytes: Array<Byte>) {
        val hex = bytes.joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }
        Log.d(TAG, "decode: $hex")
        // copy array with nibbles to array of bytes
        val bytes2 = Array<Byte>(bytes.size * 2) { 0 }
        //Arrays.fill(bytes2, 0.toByte())
        inflate(bytes, bytes2)

        /*
        var byteIndex = 0
        for(tubeIndex in gameState.tubes.indices) {
            val tube = gameState.tubes[tubeIndex]
            for(position in 0 until gameState.tubeHeight) {
                val color = bytes2[byteIndex]
                // innerhalb der Röhre farblos oder Seperator erreicht oder letzter Ball erreicht?
                if((color != 0.toByte()) && (byteIndex < bytes2.size)) {
                    byteIndex++
                }
                tube.cells[position] = color
            }
            tube.repairFillLevel()
            // separator überspringen?
            if(tube.fillLevel < gameState.tubeHeight) {
                byteIndex++
            }
        }
        */
        var tubeIndex = 0
        var position = 0
        var ballIndex = 0
        val numberOfBalls = gameState.numberOfTubes * gameState.tubeHeight
        gameState.clear()
        for (color in bytes2) {
            if (position == gameState.tubeHeight) {
                tubeIndex++
                position = 0
            }
            if (color == 0.toByte()) {
                tubeIndex++
                position = 0
            } else {
                if (tubeIndex == gameState.numberOfTubes) break
                gameState.tubes[tubeIndex].addBall(color)
                position++
                ballIndex++
            }
            if (ballIndex == numberOfBalls) break
        }
    }

    /**
     * The actual size usually is a little bit smaller,
     * if some tubes are fully filled and separators are emitted.
     * @return the maximum size
     */
    override fun encodedSizeInNibbles(): Int {
        val numberOfBalls = gameState.numberOfColors * gameState.tubeHeight
        val numberOfSeparators = gameState.numberOfTubes - 1
        return numberOfBalls + numberOfSeparators
    }

    override fun encodedSizeInBytes(): Int {
        val x = encodedSizeInNibbles() + 1
        return x shr 1
    }

    companion object {
        private const val TAG = "balla.SeparatorCodec"
    }
}