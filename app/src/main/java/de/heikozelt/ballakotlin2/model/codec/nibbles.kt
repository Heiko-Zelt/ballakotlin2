package de.heikozelt.ballakotlin2.model.codec

import kotlin.experimental.and

/**
 * converts the lower 4 bits of bytes in bytes with 4+4 bits.
 * shrinks the size of the array by half (if even).
 * if odd, the last nibble is padded with 0.
 * @param input buffer, may be bigger as actual data
 * @param size number of bytes to read from buffer / nibbles to return
 */
fun deflate(input: Array<Byte>, size: Int): Array<Byte> {
    val output = Array<Byte>((size + 1) / 2) { 0 }
    var nibbleIndex = 0
    for (byteIndex in output.indices) {
        val lowerNibble = input[nibbleIndex]
        nibbleIndex++
        var higherNibble = 0.toByte()
        if (nibbleIndex < size) {
            higherNibble = input[nibbleIndex]
            nibbleIndex++
        }
        output[byteIndex] = (lowerNibble + (higherNibble * 16)).toByte()
    }
    return output
}

/**
 * converts the lower and higher 4 bits of bytes to an array of bytes.
 * output could be a return value, but reusing an existing array,
 * increases memory cache hits and reduces garbage collection.
 * The conversion stops when the end of one of the arrays is reached.
 * @param input Array of bytes containing lower and higher nibbles (like hex number)
 * @param output Array of bytes containing number between 0 and 15 (higher nibble contains zeros)
 */
fun inflate(input: Array<Byte>, output: Array<Byte>) {
    var nibbleIndex = 0
    for(byteIndex in input.indices) {
        if(nibbleIndex == output.size) break
        val byte = input[byteIndex]
        val lowerNibble = byte and 0b00001111.toByte()
        output[nibbleIndex] = lowerNibble
        nibbleIndex++

        if(nibbleIndex == output.size) break
        // seltsame Konvertierung, aber Bytes sind in Java eigentlich vorzeichenbehaftet.
        val higherNibble = ((byte.toInt() shr 4) and 0b00001111).toByte()
        output[nibbleIndex] = higherNibble
        nibbleIndex++
    }
}