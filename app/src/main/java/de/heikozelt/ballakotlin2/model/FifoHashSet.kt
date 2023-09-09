package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlin.math.abs


/**
 * A hash set much like LinkedHashSet, but with special features.
 *
 * The size is limited. If the size limit is reached, older elements are evicted.
 * That's useful if the set is huge, wouldn't fit into memory and older elements are unimportant.
 * This is implemented by a single linked list, instead of a double linked list like in LinkedHashSet.
 *
 * The hashCode- and equals-function are functional parameters independent of the
 * hashCode- and equals-methods of the elements. This makes this HashSet more flexible.
 * For example:
 * Elements are Arrays. We don't care about the object identities but about the array contents.
 * Instead of the Array.equals-method, the Array.contentEquals-method can be used and
 * instead of the Array.hashCode-method, the Array.contentHashCode-method can be used.
 * Otherwise the elements would have to be wrapped, which is awkward.
 *
 * The HashSet is not synchronized. There is no concurrent access detection (modification counter).
 * No putAll, remove needed/implemented yet.
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

    class Entry<T>(
        var value: T,
        var nextInBucket: Entry<T>? = null,
        var nextInQueue: Entry<T>? = null
    )

    class ChronologicalIterator<T>(private val hashSet: FifoHashSet<T>) : Iterator<T> {
        private var started = false
        private var currentEntry: Entry<T>? = null
        override fun hasNext(): Boolean {
            return if(started) {
                currentEntry?.nextInQueue != null
            } else {
                hashSet.queueTail != null
            }
        }

        override fun next(): T {
            var entry = currentEntry
            return if(started) {
                entry = entry?.nextInQueue
                if(entry == null) {
                    throw NoSuchElementException()
                } else {
                    currentEntry = entry
                    entry.value
                }
            } else {
                started = true
                val tail = hashSet.queueTail
                if(tail == null) {
                    throw NoSuchElementException()
                } else {
                    currentEntry = tail
                    tail.value
                }
            }
        }
    }

    class HashedIterator<T>(private val hashSet: FifoHashSet<T>) : Iterator<T> {
        /**
         * -1 for not started yet
         * -2 for end reached
         */
        private var currentBucket = -1
        private var currentEntry: Entry<T>? = null

        override fun hasNext(): Boolean {
            // 3 Fälle:
            // noch nicht angefangen
            // im aktuellen Bucket befindet sich mindestens ein weiterer Eintrag
            // im aktuellen Bucket befindet sich kein Eintrag mehr
            return if (currentBucket == -1) {
                findNextBucket() != -2
            } else {
                if (currentEntry?.nextInBucket == null) {
                    findNextBucket() != -2
                } else {
                    true
                }
            }
        }

        override fun next(): T {
            // 3 Fälle:
            // noch nicht angefangen
            // im aktuellen Bucket befindet sich mindestens ein weiterer Eintrag
            // im aktuellen Bucket befindet sich kein Eintrag mehr
            return if (currentBucket == -1) {
                val nextBucket = findNextBucket()
                if (nextBucket == -2) {
                    throw NoSuchElementException()
                } else {
                    currentBucket = nextBucket
                    currentEntry = hashSet.buckets[currentBucket]
                    currentEntry as T
                }
            } else {
                if (currentEntry?.nextInBucket == null) {
                    val nextBucket = findNextBucket()
                    if (nextBucket == -2) {
                        throw NoSuchElementException()
                    } else {
                        currentBucket = nextBucket
                        currentEntry = hashSet.buckets[currentBucket]
                        currentEntry as T
                    }
                } else {
                    currentEntry = currentEntry?.nextInBucket
                    currentEntry as T
                }
            }
        }

        /**
         * @return nächster Bucket-Index, welcher mindestens einen Eintrag enthält oder -2 für Ende erreicht.
         */
        private fun findNextBucket(): Int {
            var i = currentBucket
            do {
                i++
                if (i == hashSet.buckets.size) {
                    break
                }
                if (hashSet.buckets[i] != null) {
                    return i
                }
            } while (true)
            return -2
        }

    }

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
        } else if (queueTail == null) {
            queueTail = newEntry
        }
    }

    /**
     * returns the bucket number for an element
     */
    private fun hash(element: T): Int {
        return abs(hashCodeFunction(element) % buckets.size)
    }

    operator fun iterator(): Iterator<T> {
        return ChronologicalIterator(this)
    }

    fun hashedIterator(): Iterator<T> {
        return HashedIterator(this)
    }

    companion object {
        private const val TAG = "balla.EfficientHashSet"
    }

}