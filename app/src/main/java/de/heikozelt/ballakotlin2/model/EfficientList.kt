package de.heikozelt.ballakotlin2.model

/**
 * a simple list which is memory and CPU efficient.
 * chunks are stored in arrays, arrays are single linked.
 * not synchronized.
 * @param chunkSize must be at least 1
 */

class EfficientList<T>(val chunkSize: Int = 32) {

    var firstChunk: Chunk<T>? = null
    private var lastChunk: Chunk<T>? = null
    var size: Int = 0
    fun add(element: T) {
        if (size == 0) {
            firstChunk = Chunk(chunkSize)
            firstChunk?.content?.set(0, element)
            lastChunk = firstChunk
        } else {
            val index = size % chunkSize
            if (index == 0) {
                val newChunk = Chunk<T>(chunkSize)
                newChunk.content[0] = element
                lastChunk?.nextChunk = newChunk
                lastChunk = newChunk
            } else {
                lastChunk?.content?.set(index, element)
            }
        }
        size++
    }

    operator fun iterator(): Iterator<T> {
        return EfficientListIterator(this)
    }
}