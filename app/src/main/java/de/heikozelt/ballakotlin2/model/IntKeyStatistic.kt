package de.heikozelt.ballakotlin2.model


/**
 * Implementiert eine einfache Abbildung für Statistik-Zwecke.
 * Eine Map verwendet Name eines Zählers als Schlüssel und Zählerstand als Wert.
 * Verwendung: Qualität einer Hash-Funktion ermitteln
 */
class IntKeyStatistic: Statistic<Int>() {

    /**
     * Mittelwert
     */
    fun average(): Float {
        var sum = 0
        for(e in map.entries) {
            sum += e.key * e.value
        }
        return sum / map.size.toFloat()
    }

    /**
     * todo: Standardabweichung & Varianz
     */
    fun standardDeviation(): Float {
        /*
        for(e in map.entries) {
        }
         */
        return 1.23456f
    }

    companion object {
        private const val TAG = "balla.Statistics"
    }
}