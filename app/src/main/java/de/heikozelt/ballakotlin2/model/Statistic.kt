package de.heikozelt.ballakotlin2.model

import android.util.Log


/**
 * Implementiert eine einfache Abbildung für Statistik-Zwecke.
 * Eine Map verwendet Name eines Zählers als Schlüssel und Zählerstand als Wert.
 * Verwendung: Qualität einer Hash-Funktion ermitteln
 */
open class Statistic<K> {

    /**
     * HashMap performt besser als TreeMap bei Identitäts-Suche
     */
    protected val map = HashMap<K, Int>()

    /**
     * Die Funktion inkrementiert einen Zähler oder initialisiert einen Zähler mit 1,
     * falls er noch nicht in der Map existiert.
     *
     * @param key
     */
    fun increment(key: K) {
        map.merge(key, 1) { oldValue: Int, _: Int? -> oldValue + 1 }
    }

    /**
     * Liefert den Zählerstand zu einem Schlüssel oder 0 wenn der Zähler nicht existiert.
     *
     * @param key
     * @return aktueller Zählerstand.
     */
    operator fun get(key: K): Int {
        return map.getOrDefault(key, 0)
    }

    /**
     * @param key
     * @param value
     */
    operator fun set(key: K, value: Int) {
        if (value != 0) { // keine Nullen speichern, denn das ist der Default-Wert
            map[key] = value
        }
    }

    /**
     * @param key
     * @return true, wenn Zähler auf 0 steht oder gar nicht in der Map enthalten ist.
     */
    fun isZero(key: K): Boolean {
        return get(key) == 0
    }

    fun size(): Int {
        return map.size
    }

    fun forEach(action: (K, Int) -> Unit) {
        map.forEach(action)
    }

    fun dump() {
        map.forEach { (key: K, value: Int) ->
            Log.d(TAG, "$key -> $value")
        }
    }



    companion object {
        private const val TAG = "balla.Statistics"
    }
}