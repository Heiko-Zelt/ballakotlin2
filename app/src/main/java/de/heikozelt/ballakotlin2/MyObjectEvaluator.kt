package de.heikozelt.ballakotlin2

import android.animation.TypeEvaluator

class MyObjectEvaluator : TypeEvaluator<Any> {

    override fun evaluate(fraction: Float, startValue: Any?, endValue: Any?): Any {
        val start = startValue as MyObject
        val end =endValue as MyObject
        val newX = start.x + fraction * (end.x - start.x)
        val newY = start.y + fraction * (end.y - start.y)
        return MyObject(newX, newY);
    }

    //override fun evaluate(fraction: Float, startValue: MyObject, endValue: MyObject): MyObject {
    //}
}
