package de.heikozelt.ballakotlin2.view

import kotlin.math.sqrt

/**
 * val fractions = arrayOf(
 *   time[0] / wholeTime,  //down
 *   (time[0] + time[1]) / wholeTime, // bounce up
 *   (time[0] + time[1] + time[2]) / wholeTime, // bounce down
 *   (time[0] + time[1] + time[2] + time[3]) / wholeTime, // bounce up again
 * )
 */
fun durationsToFractions(durations: Array<Float>, wholeDuration: Float): Array<Float> {
    // erste ist 0f, letzte ist 1f, nur dazwischen muss berechnet werden
    val fractions = Array(durations.size - 1) { 0f }
    for (i in fractions.indices) {
        for (j in 0..i) {
            fractions[i] += durations[j]
        }
        fractions[i] = fractions[i] / wholeDuration
    }
    return fractions
}

/**
 * Integer precision is sufficient
 */
fun diagonalDistance(x1: Int, x2: Int, y1: Int, y2: Int): Int {
    return diagonalDistance(x2 - x1, y2 - y1)
}

/**
 * Pythagorean theorem
 * c^2 = a^2 + b^2
 */
fun diagonalDistance(a: Int, b: Int): Int {
    return sqrt((a * a + b * b).toDouble()).toInt()
}