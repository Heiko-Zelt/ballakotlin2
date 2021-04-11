package de.heikozelt.ballakotlin2

import android.animation.TypeEvaluator

class CoordinatesEvaluator : TypeEvaluator<Any> {

    /**
     * @param fraction Zahl zwischen null und eins
     * @param startValue Anfangs-Koordinaten bestehend aus x und y
     * @param endValue End-Koordinaten bestehend aus x und y
     * @returns aktuelle Koordinaten auf der Linie zwischen Anfangs- und End-Koordinaten
     */
    override fun evaluate(fraction: Float, startValue: Any?, endValue: Any?): Any {
        val start = startValue as Coordinates
        val end = endValue as Coordinates
        val newX = start.x + fraction * (end.x - start.x)
        val newY = start.y + fraction * (end.y - start.y)
        return Coordinates(newX, newY)
    }

}
