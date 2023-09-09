package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlin.math.abs


/**
 * A slimmer, more flexible and efficient LinkedHashSet.
 * The size is limited. If the size limit is reached, older elements are evicted.
 * This is implemented by a single linked list, instead of a double linked list like in LinkedHashSet.
 * The hashCode- and equals-function are functional parameters independent of the
 * hashCode- and equals-methods of the elements. This makes this HashSet more flexible.
 * For example:
 * Elements are Arrays. We don't care about the object identities but about the array contents.
 * Instead of Array.equals-method, Array.contentEquals-method is used and
 * instead of Array.hashCode-method, Array.contentHashCode-method is used.
 * Otherwise the elements would have to be wrapped, which is awkward.
 * The HashSet is not synchronized.
 * There is no concurrent access detection (modification counter).
 * No iterator, putAll, remove needed/implemented yet.
 */
class FifoHashSet<T>(
    private var hashCodeFunction: (T) -> Int,
    private var equalsFunction: (T, T) -> Boolean,
    private var sizeLimit: Int,
    initialCapacity: Int = 11,
    private val loadFactor: Float = 0.75f
) {
    private var threshold: Int
    private var buckets: Array<Entry<T>?>
    private var queueHead: Entry<T>? = null
    private var queueTail: Entry<T>? = null

    /**
     * number of elements in this set
     */
    private var size: Int = 0

    init {
        if (initialCapacity < 0)
            throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        if (loadFactor <= 0) // Todo: check for NaN too
            throw IllegalArgumentException("Illegal Load: $loadFactor")
        buckets = Array(initialCapacity) { null }
        threshold = (initialCapacity * loadFactor).toInt()
    }

    /**
     * only for testing
     */
    fun getCapacity(): Int {
        return buckets.size
    }

    /**
     * value may be replaced, if an element is added, which equals an existing element.
     */
    class Entry<T>(
        var value: T,
        var nextInBucket: Entry<T>? = null,
        var nextInQueue: Entry<T>? = null
    )

    fun size(): Int {
        return size
    }

    fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * If the set contains an element which equals the given element,
     * a reference to the element in the set is returned, otherwise null.
     * contains() should be more useful in most use cases.
     */
    fun get(element: T): T? {
        val idx = hash(element)
        var entry = buckets[idx]
        while (entry != null) {
            if (equalsFunction(element, entry.value))
                return entry.value;
            entry = entry.nextInBucket;
        }
        return null
    }

    operator fun contains(element: T): Boolean {
        val idx = hash(element)
        var entry = buckets[idx]
        while (entry != null) {
            if (equalsFunction(element, entry.value))
                return true
            entry = entry.nextInBucket;
        }
        return false
    }

    /**
     * @return the prior element, or null if there was none
     */
    fun put(element: T): T? {
        Log.d(TAG, "put($element)")
        var idx = hash(element)
        var entry = buckets[idx]
        // replace existing element?
        while (entry != null) {
            if (equalsFunction(element, entry.value)) {
                val priorElement = entry.value
                entry.value = element
                return priorElement
            }
            // follow bucket chain
            entry = entry.nextInBucket
        }
        size++
        if (size > threshold) {
            rehash()
            // Need a new hash value to suit the bigger table.
            idx = hash(element)
        }
        addEntry(element, idx)
        return null
    }

    /**
     * Warning: The element is removed from the hash table, but not from the queue.
     * If removal is not needed, this is not a problem. so make it private.
     */
    private fun remove(element: T): T? {
        val idx = hash(element);
        return removeEntry(element, idx)
    }

    /**
     * remove an entry from the hash table
     */
    private fun removeEntry(element: T, idx: Int): T? {
        var entry = buckets[idx]
        var previous: Entry<T>? = null
        while (entry != null) {
            if (equalsFunction(element, entry.value)) {
                // cut entry out of chain
                if (previous == null) { // is first in chain?
                    buckets[idx] = entry.nextInBucket
                } else {
                    previous.nextInBucket = entry.nextInBucket
                }
                size--
                return entry.value
            }
            previous = entry
            entry = entry.nextInBucket
        }
        return null
    }

    private fun rehash() {
        Log.d(TAG, "rehash()")
        val oldBuckets = buckets
        val newCapacity = buckets.size * 2 + 1
        Log.d(TAG, "old capacity: ${oldBuckets.size}, new capacity: $newCapacity")
        threshold = (newCapacity * loadFactor).toInt()
        buckets = Array(newCapacity) { null }
        for (i in oldBuckets.size - 1 downTo 0) {
            // Der Entry aus oldBuckets wird wiederverwendet.
            // Das reduziert den Garabage Collection-Aufwand.
            var entry = oldBuckets[i]
            while (entry != null) {
                // neue Position berechnen
                val idx = hash(entry.value)
                // next temporär sichern
                val next = entry.nextInBucket
                // einfügen
                entry.nextInBucket = buckets[idx]
                buckets[idx] = entry
                // Kette in Schleife weitergehen
                entry = next
            }
        }
    }


    /**
     * adds a new entry at the head of the bucket chain
     * move queue head and
     * if size limit is reached, remove oldest entry from queue and hash table
     */
    private fun addEntry(element: T, idx: Int) {
        val newEntry = Entry(element)
        newEntry.nextInBucket = buckets[idx]
        buckets[idx] = newEntry
        queueHead?.nextInQueue = newEntry
        queueHead = newEntry
        if (size > sizeLimit) {
            queueTail?.let { tail ->
                remove(tail.value)
            }
            queueTail = queueTail?.nextInQueue
        } else if(queueTail == null) {
            queueTail = newEntry
        }
    }

    /**
     * returns the bucket number for an element
     */
    private fun hash(element: T): Int {
        return abs(hashCodeFunction(element) % buckets.size)
    }

    companion object {
        private const val TAG = "balla.EfficientHashSet"
    }

}