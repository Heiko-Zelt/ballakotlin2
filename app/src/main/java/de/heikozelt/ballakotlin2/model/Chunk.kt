package de.heikozelt.ballakotlin2.model

/**
 * used by EfficientList
 */
class Chunk<T>(chunkSize: Int = 16) {
    val content = Array<Any?>(chunkSize) { null }
    var nextChunk: Chunk<T>? = null
}