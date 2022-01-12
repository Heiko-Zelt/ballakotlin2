package de.heikozelt.ballakotlin2.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import de.heikozelt.ballakotlin2.R

// import android.util.Log

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

    private var paint = PAINTS[color]

    // Ball, der sich diagonal über Bälle in Röhren bewegt, muss im Vordergrund sein
    var foreground = false

    fun draw(canvas: Canvas) {
        //Log.i(TAG, "Ball.draw()")
        canvas.drawCircle(coordinates.x, coordinates.y, MyDrawView.BALL_RADIUS_INSIDE, paint)
        //Log.i(TAG,"drawCircle(${coordinates.x}, ${coordinates.y}, ${MyDrawView.BALL_RADIUS_INSIDE}, ${paint})")
    }

    companion object {
        private const val TAG = "balla.Ball"

        private val PAINTS = arrayOf(
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.YELLOW
                    //color = ContextCompat.getColor(context, R.color.ball1)
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x00, 0xa8, 0x18) // dunkles grün
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x20, 0x70, 0xff) // some light blue
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
                    color = Color.rgb(0x00, 0xff, 0x00) // helles, kräftiges lime grün
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x80, 0x00, 0x00) // maroon
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0x00, 0x00, 0xb0) // navy oder dunkelblau
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
                    color = Color.rgb(0x55, 0x6b, 0x2f) // (dark) olive green
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0xff, 0x99, 0xff) // light pink
                    style = Paint.Style.FILL
                },
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0xcc, 0xcc, 0x80) // beige
                    style = Paint.Style.FILL
                }

        )
    }
}