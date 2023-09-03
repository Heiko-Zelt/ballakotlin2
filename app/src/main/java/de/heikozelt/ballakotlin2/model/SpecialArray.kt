package de.heikozelt.ballakotlin2.model

/**
 * a wrapper for Array<Byte> to be used with HashSet.
 * equals() returns true if the contents of the arrays equal.
 * hashCode() calculates hash value based on the content of the array.
 */
class SpecialArray(val bytes: Array<Byte>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpecialArray

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}