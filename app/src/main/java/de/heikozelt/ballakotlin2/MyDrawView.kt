package de.heikozelt.ballakotlin2

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import kotlin.math.floor
import kotlin.math.min

class MyDrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val _TAG = "MyDrawView"
    val app = context.applicationContext as BallaApplication?

    /**
     * positions of original game board / puzzle without scaling
     */
    private val _BALL_RADIUS = 40
    private val _BALL_RADIUS_INSIDE = _BALL_RADIUS.toFloat().minus(0.5f)
    private val _BALL_DIAMETER = _BALL_RADIUS * 2
    private val _BALL_PADDING = 4
    private val _TUBE_WIDTH = _BALL_DIAMETER + _BALL_PADDING * 2
    private val _TUBE_LOWER_CORNER_RADIUS = 26
    private val _TUBE_PADDING = 8
    private val _BOUNCE = 20

    private val _PAINTS = arrayOf(
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x40, 0x40, 0xff) // some light blue
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0xff, 0x8c, 0x00) // orange
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x8a, 0x2b, 0xe2) // violet
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x00, 0xff, 0x00) // lime
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x80, 0x00, 0x00) // maroon
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x00, 0x00, 0x80) // navy
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x00, 0xff, 0xff) // cyan
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        },
        Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0x55, 0x6b, 0x2f) // olive
            style = Paint.Style.FILL
        }
    )

    private val circlePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        strokeWidth = 10f
    }

    private var boardWidth = 600
    private var boardHeight = 300

    private var circleX = 0f
    private var circleY = 0f
    private var radius = 0f

    // private var animator = ObjectAnimator.ofFloat(this, View.ROTATION, -360f, 0f)
    private var object1 = MyObject(150f, 200f)
    private var object2 = MyObject(100f, 150f)
    private var object3 = MyObject(500f, 100f)

    private var animator = ValueAnimator.ofObject(MyObjectEvaluator(), object1, object2, object3)
    //private var animator = ValueAnimator.ofFloat(20f, 300f, 100f, 200f)

    private var scaleFactor = 0f
    private var transY = 0f
    private var transX = 0f

    private fun _getActivity(): Activity? {
        var c: Context? = context
        while (c is ContextWrapper) {
            if (c is Activity) {
                return c
            }
            c = c.baseContext as ContextWrapper
        }
        return null
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        /*
        if(app == null) {
            Log.e(_TAG, "No reference to BallaApplication in MyDrawView.onTouchEvent() :-(")
            return true
        }
        */

        if(event == null) {
            return true
        }
        if(event.action != MotionEvent.ACTION_DOWN) {
            return true
        }
        //Log.i(_TAG, "touched ${event}")
        Log.i(_TAG, "touched x=${event.x}, y=${event.y}")
        val virtualX = (event.x / scaleFactor - transX)
        Log.i(_TAG, "virtualX=${virtualX}")
        val col = (virtualX / (_TUBE_WIDTH + _TUBE_PADDING)).toInt()
        Log.i(_TAG, "col=${col}")
        val c = _getActivity() as MainActivity?
        c?.tubeClicked(col)
        return true
    }

    override fun onFinishInflate() {
        Log.i(_TAG, "onon onFinishInflate()")

        super.onFinishInflate()
        animator.duration = 1500
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = Animation.INFINITE
        //animator.interpolator = AccelerateDecelerateInterpolator()
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val obj = animation.animatedValue as MyObject
            circleX = obj.x
            circleY = obj.y
            //Log.i(TAG, "onon animator update")
            invalidate()
        }
        // animator.start()

        boardWidth = if(app == null) {400 } else { app.gameState.numberOfTubes * _TUBE_WIDTH + (app.gameState.numberOfTubes - 1) * _TUBE_PADDING }
        Log.i(_TAG, "boardWidth: ${boardWidth}")
        boardHeight = if(app == null) { 300 } else { (app.gameState.tubeHeight + 1) * _BALL_DIAMETER + _BALL_PADDING }
        Log.i(_TAG, "boardHeight: ${boardHeight}")
    }


    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        //Log.i(TAG, "onon onSizeChanged(w=${w}, h=${h})")
        //circleX = w / 2f
        //circleY = h / 2f
        radius = 100f

        val scaleX = w / boardWidth.toFloat()
        val scaleY = h / boardHeight.toFloat()
        scaleFactor = min(scaleX, scaleY)

        transX = w.div(scaleFactor).minus(boardWidth).div(2f)
        transY = h.div(scaleFactor).minus(boardHeight).div(2f)
    }

    private fun ballX(col: Int): Int {
        return col * (_TUBE_WIDTH + _TUBE_PADDING) + _BALL_RADIUS + _BALL_PADDING
    }

    private fun ballY(row: Int): Int {
        if(app == null) {
            return 100
        }
        return (app.gameState.tubeHeight - row) * _BALL_DIAMETER + _BALL_RADIUS
    }

    private fun drawBall(canvas: Canvas, col: Int, row: Int) {
        Log.i(_TAG, "drawBall(${col}, ${row})")
        val color = if(app == null) { 0 } else { app.gameState.tubes.get(col).cells.get(row) }
        val paint = _PAINTS[color]
        canvas.drawCircle(ballX(col).toFloat(), ballY(row).toFloat(), _BALL_RADIUS_INSIDE, paint)
        Log.i(
            _TAG,
            "drawCircle(${ballX(col).toFloat()}, ${ballY(row).toFloat()}, ${_BALL_RADIUS_INSIDE})"
        )
    }

    private fun drawBalls(canvas: Canvas) {
        //Log.i(TAG, "drawBalls()")
        if(app == null) {
            return
        }
        for (col in 0..(app.gameState.numberOfTubes - 1)) {
            val tube = app.gameState.tubes[col]
            //Log.i(TAG, "col: ${col}")
            for (row in 0..(tube.fillLevel - 1)) {
                //Log.i(TAG, "col: ${row}")
                drawBall(canvas, col, row)
            }
        }
    }

    // Called when the view should render its content.
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }
            canvas.save()

            //Log.i(TAG, "onon canvas.height=${canvas.height}, canvas.width=${canvas.width}, scaleFacor=${scaleFactor}")
            canvas.scale(scaleFactor, scaleFactor)
            //Log.i(TAG, "onon canvas.height=${canvas.height}, canvas.width=${canvas.width}")
            //canvas.translate((canvas.width - boardWidth) / 2f, (canvas.height - boardHeight) / 2f)

            canvas.translate(transX, transY)

            //Log.i(TAG, "onon onDraw()")
            //Log.i(TAG, "onon circleX=${circleX}, circleY=${circleY}, radius=${radius}")
            canvas.drawCircle(circleX, circleY, radius, circlePaint)
            canvas.drawLine(0f, 0f, boardWidth.toFloat(), boardHeight.toFloat(), linePaint)
            canvas.drawLine(0f, boardHeight.toFloat(), boardWidth.toFloat(), 0f, linePaint)

            drawBalls(canvas)
            canvas.restore()

    }
}