package de.heikozelt.ballakotlin2.model

import java.lang.ClassCastException

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