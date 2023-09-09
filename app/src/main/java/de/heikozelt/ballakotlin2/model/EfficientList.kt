package de.heikozelt.ballakotlin2.model

/**
 * A simple list which is memory, CPU and garbage collector efficient.
 * Chunks are stored in arrays, arrays are single linked.
 * not synchronized.
 * When capacity is reached references to elements are not copied from old to new array as in Vector or ArrayList.
 * There isn't a data structure for every element as in LinkedList.
 * The list is easily iterable from top to bottom.
 * @param chunkSize must be at least 1
 */

class EfficientList<T>(val chunkSize: Int = 32) {

    class EfficientListIterator<T>(private val efficientList: EfficientList<T>): Iterator<T> {
        private var currentChunk = efficientList.firstChunk
        private var index = 0
        override fun hasNext(): Boolean {
            return index < efficientList.size
        }

        override fun next(): T {
            val localIndex = index % efficientList.chunkSize
            index++
            val element = currentChunk?.content?.get(localIndex)
            if(localIndex == (efficientList.chunkSize - 1)) {
                currentChunk = currentChunk?.nextChunk
            }
            return element as T
        }
    }

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