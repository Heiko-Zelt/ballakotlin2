package de.heikozelt.ballakotlin2.model.codec

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import kotlin.experimental.and

/**
 * Every tube is encoded with the same number ob nibbles.
 * Empty fields are encoded as color with number 0.
 */
class FixedSizeCodec(private val gameState: GameState) : GameStateCodec {

    override fun encodeNormalized(): Array<Byte> {
        // (3 * 3 + 1) / 2 = (9 + 1) / 2 = 10 / 2 = 5; 5 Bytes um 9 Nibbles zu speichern
        // (4 * 3 + 1) / 2 = (12 + 1) / 2 = 13 / 2 = 6; 6 Bytes um 12 Nibbles zu speichern
        // (5 * 3 + 1) / 2 = (15 + 1) / 2 = 16 / 2 = 8; 8 Bytes um 15 Nibbles zu speichern
        val sizeEncoded = encodedSizeInBytes()
        //Log.d(TAG, "sizeEncoded: $sizeEncoded")
        val bytes = Array<Byte>(sizeEncoded) { 0 }
        val sortedTubes = gameState.tubes.copyOf()
        sortedTubes.sort()

        for (i in bytes.indices) {
            //Log.d(TAG, "i: $i")
            val lowerTubeIndex = i * 2 / gameState.tubeHeight
            val lowerBallIndex = i * 2 % gameState.tubeHeight
            val lowerNibble = sortedTubes[lowerTubeIndex].cells[lowerBallIndex]
            //Log.d(TAG, "lower [$lowerTubeIndex, $lowerBallIndex] = $lowerNibble")
            val upperTubeIndex = (i * 2 + 1) / gameState.tubeHeight
            // bei ungerader Anzahl Zellen bleibt das letzte Nibble leer
            val upperNibble = if (upperTubeIndex < gameState.numberOfTubes) {
                val upperBallIndex = (i * 2 + 1) % gameState.tubeHeight
                //Log.d(TAG, "upper [$upperTubeIndex, $upperBallIndex]")
                sortedTubes[upperTubeIndex].cells[upperBallIndex]
            } else {
                0
            }
            bytes[i] = (lowerNibble + (upperNibble * 16.toByte())).toByte()
            //Log.d(TAG, "byte: ${bytes[i]}")
        }
        return bytes
    }


    override fun decode(bytes: Array<Byte>) {
        for (i in bytes.indices) {
            val lowerTubeIndex = i * 2 / gameState.tubeHeight
            val lowerBallIndex = i * 2 % gameState.tubeHeight
            val lowerNibble = bytes[i] and 0b00001111.toByte()
            //Log.d(TAG, "lower [$lowerTubeIndex, $lowerBallIndex] = $lowerNibble")
            gameState.tubes[lowerTubeIndex].cells[lowerBallIndex] = lowerNibble
            val higherTubeIndex = (i * 2 + 1) / gameState.tubeHeight
            if (higherTubeIndex < gameState.numberOfTubes) {
                val higherBallIndex = (i * 2 + 1) % gameState.tubeHeight
                val higherNibble = (bytes[i].toInt() shr 4).toByte() and 0b00001111.toByte()
                //Log.d(TAG, "higher [$higherTubeIndex, $higherBallIndex] = $higherNibble")
                gameState.tubes[higherTubeIndex].cells[higherBallIndex] = higherNibble
            }
        }
        gameState.tubes.forEach { it.repairFillLevel() }
    }

    /**
     * returns number of nibbles
     */
    override fun encodedSizeInNibbles(): Int {
        return gameState.tubeHeight * gameState.numberOfTubes
    }

    override fun encodedSizeInBytes(): Int {
        return (encodedSizeInNibbles() + 1) / 2
    }

    companion object {
        private const val TAG = "balla.FixedSizeCodec"
    }
}