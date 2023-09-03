package de.heikozelt.ballakotlin2.model

import java.util.LinkedList

/**
 * remembers added elements.
 * if the maximum capacity is reached, older elements will be forgotten.
 */
class LimitedSet<T>(private val capacity: Int) {

    private val hashSet = hashSetOf<T>()
    private val queue = LinkedList<T>()

    operator fun contains(element: T): Boolean {
        return hashSet.contains(element)
    }

    fun add(element: T) {
        if(queue.size >= capacity) {
            val oldElement = queue.remove()
            hashSet.remove(oldElement)
        }
        queue.add(element)
        hashSet.add(element)
    }

    fun size(): Int {
        return hashSet.size
    }
}