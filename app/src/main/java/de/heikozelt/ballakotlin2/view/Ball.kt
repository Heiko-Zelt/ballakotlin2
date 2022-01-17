package de.heikozelt.ballakotlin2.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

// import android.util.Log

/**
 * Represents a Ball
 * x and y coordinates may be fix or animated
 * (if ball is fixed, the object may be reused to draw different Balls, less garbage collection)
 *
 * color is set by constructor, but can be changed later
 * if color ist set, paint is set too (in constructor and setter)
 */

class Ball(var coordinates: Coordinates) {
    private var color: Int = 0

    private var paint: Paint? = null

    // Ball, der sich diagonal über Bälle in Röhren bewegt, muss im Vordergrund sein
    var foreground = false

    fun setColor(_color: Int, paints: Array<Paint?>) {
        color = _color
        paint = paints[_color]
    }

    fun draw(canvas: Canvas) {
        //Log.i(TAG, "Ball.draw()")
        paint?.let { p ->
            canvas.drawCircle(coordinates.x, coordinates.y, MyDrawView.BALL_RADIUS_INSIDE, p)
        }
        //Log.i(TAG,"drawCircle(${coordinates.x}, ${coordinates.y}, ${MyDrawView.BALL_RADIUS_INSIDE}, ${paint})")
    }

    companion object {
        private const val TAG = "balla.Ball"
    }
}