package de.heikozelt.ballakotlin2

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

/**
 * Represents a Ball
 * x and y coordinates may be fix or animated
 * (if ball is fixed, the object may be reused to draw different Balls, less garbage collection)
 *
 * color is set by constructor, but can be changed later
 * if color ist set, paint is set too (in constructor and setter)
 */

class Ball(var coordinates: Coordinates, color: Int) {
    var color: Int = color
    set(value) {
        paint = PAINTS[value]
        field = value
    }

    var paint = PAINTS[color]

    fun draw(canvas: Canvas) {
        //Log.i(TAG, "Ball.draw()")
        canvas.drawCircle(coordinates.x, coordinates.y, MyDrawView.BALL_RADIUS_INSIDE, paint)
        //Log.i(TAG,"drawCircle(${coordinates.x}, ${coordinates.y}, ${MyDrawView.BALL_RADIUS_INSIDE}, ${paint})")
    }

    companion object {
        private const val TAG = "ballas Ball"

        private val PAINTS = arrayOf(
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.YELLOW
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.GREEN
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x40, 0x40, 0xff) // some light blue
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.GRAY
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0xff, 0x8c, 0x00) // orange
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x8a, 0x2b, 0xe2) // violet
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x00, 0xff, 0x00) // lime
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x80, 0x00, 0x00) // maroon
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x00, 0x00, 0x80) // navy
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x00, 0xff, 0xff) // cyan
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x55, 0x6b, 0x2f) // olive
                    style = Paint.Style.FILL
                })
    }
}