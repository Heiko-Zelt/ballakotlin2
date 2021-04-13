package de.heikozelt.ballakotlin2

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import android.view.SoundEffectConstants
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import de.heikozelt.ballakotlin2.model.Move
import kotlin.math.abs
import kotlin.math.min

class MyDrawView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val app = context.applicationContext as BallaApplication?

    /**
     * Board Dimensions depend on number of tubes and tube height
     */
    private var boardWidth = 0
    private var boardHeight = 0

    // ball is lifting
    private val upwardsBall = Ball(Coordinates(0f, 0f), 0)
    // Spalte, in der gerade ein Ball angehoben wird
    private var upwardsCol = -1

    // ball is
    // - dropping or
    // - moving sidewards and dropping or
    // - moving upward, sidewards and eventually dropping
    private val downwardsBall = Ball(Coordinates(0f, 0f), 0)
    // Spalte, in der gerade ein Ball gesenkt wird
    private var downwardsCol = -1

    // ball ist fixed / object is reused for different balls
    private var fixedBall = Ball(Coordinates(0f, 0f), 1)

    // private var animator = ObjectAnimator.ofFloat(this, View.ROTATION, -360f, 0f)
    private var object1 = Coordinates(150f, 20f)
    private var object2 = Coordinates(100f, 150f)
    private var object3 = Coordinates(50f, 100f)
    private var upwardsAnimator = ValueAnimator.ofObject(CoordinatesEvaluator(), object1, object2, object3)

    private var scaleFactor = 0f
    private var transY = 0f
    private var transX = 0f

    init {
        // Muss jedes Mal ein neuer Animator-Objekt erstellt werden?
        upwardsAnimator.duration = 1000
        upwardsAnimator.repeatMode = ValueAnimator.RESTART //is default
        upwardsAnimator.repeatCount = 0
        //animator.interpolator = AccelerateDecelerateInterpolator()
        upwardsAnimator.interpolator = LinearInterpolator()

        upwardsAnimator.addUpdateListener { animation ->
            val coords = animation.animatedValue as Coordinates
            upwardsBall.coordinates = coords
            //Log.i(TAG, "onon animator update")
            invalidate()
        }
         /*
        upwardsAnimator.addUpdateListener { animation ->
            val x = animation.getAnimatedValue("x") as Float
            val y = animation.getAnimatedValue("x") as Float
            upwardsBall.coordinates.x = x
            upwardsBall.coordinates.y = y
            invalidate()
        }
          */

        /*
        downwardsAnimator.addUpdateListener { animation ->
            val coords = animation.animatedValue as Coordinates
            downwardsBall.coordinates = coords
            //Log.i(TAG, "onon animator update")
            invalidate()
        }
         */


    }

    private fun findActivity(): Activity? {
        var c: Context? = context
        while (c is ContextWrapper) {
            if (c is Activity) {
                return c
            }
            c = c.baseContext as ContextWrapper
        }
        return null
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }
        if (event.action != MotionEvent.ACTION_DOWN) {
            return true
        }

        //Log.i(_TAG, "touched ${event}")
        //Log.i(TAG, "touched x=${event.x}, y=${event.y}")
        val virtualX = (event.x / scaleFactor - transX)
        //Log.i(TAG, "virtualX=${virtualX}")
        val virtualY = (event.y / scaleFactor - transY)

        if(virtualX < 0 || virtualX > boardWidth || virtualY < 0 || virtualY > boardHeight) {
            // left or right, top or bottom outside of board
            return true
        }

        val col = (virtualX / (TUBE_WIDTH + TUBE_PADDING)).toInt()
        Log.i(TAG, "col=${col}")
        val c = findActivity() as MainActivity?

        playSoundEffect(SoundEffectConstants.CLICK)
        c?.tubeClicked(col)
        return true
    }

    override fun onFinishInflate() {
        Log.i(TAG, "onon onFinishInflate()")

        super.onFinishInflate()

        boardWidth = if (app == null) {
            400
        } else {
            app.gameState.numberOfTubes * TUBE_WIDTH + (app.gameState.numberOfTubes - 1) * TUBE_PADDING
        }
        Log.i(TAG, "boardWidth: ${boardWidth}")
        boardHeight = if (app == null) {
            300
        } else {
            (app.gameState.tubeHeight + 1) * BALL_DIAMETER + BALL_PADDING
        }
        Log.i(TAG, "boardHeight: ${boardHeight}")
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
        //radius = 100f

        val scaleX = w / boardWidth.toFloat()
        val scaleY = h / boardHeight.toFloat()
        scaleFactor = min(scaleX, scaleY)
        transX = w.div(scaleFactor).minus(boardWidth).div(2f)
        transY = h.div(scaleFactor).minus(boardHeight).div(2f)
    }

    private fun ballX(col: Int): Int {
        return col * (TUBE_WIDTH + TUBE_PADDING) + BALL_RADIUS + BALL_PADDING
    }

    private fun ballY(row: Int): Int {
        if (app == null) {
            return 100
        }
        return (app.gameState.tubeHeight - row) * BALL_DIAMETER + BALL_RADIUS
    }

    private fun drawTubes(canvas: Canvas) {
        if (app == null) {
            return
        }
        val top = BALL_DIAMETER
        val bottom = top + app.gameState.tubeHeight * BALL_DIAMETER + BALL_PADDING
        val circleY = bottom - TUBE_LOWER_CORNER_RADIUS.toFloat()
        for (col in 0 until app.gameState.numberOfTubes) {
            val left = col * (TUBE_WIDTH + TUBE_PADDING)
            val right = left + TUBE_WIDTH
            val leftCircleX = left + TUBE_LOWER_CORNER_RADIUS
            val rightCircleX = right - TUBE_LOWER_CORNER_RADIUS
            canvas.drawCircle(leftCircleX.toFloat(), circleY.toFloat(), TUBE_LOWER_CORNER_RADIUS.toFloat(), TUBE_PAINT)
            canvas.drawCircle(rightCircleX.toFloat(), circleY.toFloat(), TUBE_LOWER_CORNER_RADIUS.toFloat(), TUBE_PAINT)
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), (bottom - TUBE_LOWER_CORNER_RADIUS).toFloat(), TUBE_PAINT)
            canvas.drawRect(leftCircleX.toFloat(), circleY.toFloat(), rightCircleX.toFloat(), bottom.toFloat(), TUBE_PAINT)
        }
    }

    /**
     * draw fixed balls
     * without upwards and downwards ball
     */
    private fun drawBalls(canvas: Canvas) {
        //Log.i(TAG, "drawBalls()")
        if (app == null) {
            return
        }

        //val acti = findActivity() as MainActivity

        for (col in 0 until app.gameState.numberOfTubes) {
            fixedBall.coordinates.x = ballX(col).toFloat()
            val tube = app.gameState.tubes[col]
            //Log.i(TAG, "col: ${col}")

            var numberOfBalls = if (downwardsCol == col || upwardsCol == col) {
                tube.fillLevel - 1
            } else {
                tube.fillLevel
            }
            for (row in 0 until numberOfBalls) {
                //Log.i(TAG, "col: ${row}")
                fixedBall.coordinates.y = ballY(row).toFloat()
                fixedBall.color = app.gameState.tubes[col].cells[row]
                fixedBall.draw(canvas)
            }
        }
    }

    // Called when the view should render its content.
    override fun onDraw(canvas: Canvas?) {
        //Log.i(TAG, "onDraw()")
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


        //Log.i(TAG, "onon circleX=${circleX}, circleY=${circleY}, radius=${radius}")

        //canvas.drawLine(0f, 0f, boardWidth.toFloat(), boardHeight.toFloat(), linePaint)
        //canvas.drawLine(0f, boardHeight.toFloat(), boardWidth.toFloat(), 0f, linePaint)

        drawTubes(canvas)
        drawBalls(canvas)
        if (upwardsCol != -1) {
            upwardsBall.draw(canvas)
        }
        if (downwardsCol != -1) {
            downwardsBall.draw(canvas)
        }

        canvas.restore()
    }

    fun dumpi() {
        val acti = findActivity() as MainActivity
        Log.i(TAG, "donorIndex=${acti.donorIndex}, donorRow=${acti.donorRow}, upwardsCol=${upwardsCol}, downwardsCol=${downwardsCol}")
    }

    /**
     * Ball wird angehoben. Spielstand ändert sicht nicht.
     * Falls drop ball animation gleiche Röhre betrifft,
     * dann wird sie abgebrochen/beendet.
     */
    // vielleicht besser fun liftBall(from: Int, row: Int)
    fun liftBall(from: Int) {
        Log.i(TAG, "liftBall(from=${from})")
        dumpi()
        if (app == null) {
            return
        }
        val acti = findActivity() as MainActivity
        acti.donorIndex = from;
        acti.donorRow = app.gameState.tubes[from].fillLevel - 1
        upwardsBall.color = app.gameState.tubes[from].colorOfTopmostBall()

        val x = ballX(from).toFloat()
        val startY = ballY(acti.donorRow as Int).toFloat()
        val stopY = BALL_RADIUS.toFloat()
        val start = Coordinates(x, startY)
        val stop = Coordinates(x, stopY)
        upwardsAnimator.setObjectValues(start, stop)

        //Log.i(TAG, "coordinates start: x=${x}, startY=${startY}, stopY=${stopY}")

        upwardsCol = from
        upwardsAnimator.start()
        if(downwardsCol == upwardsCol) {
            downwardsCol = -1
        }

        dumpi()
    }

    fun normalMove(move: Move) {
        // Todo: normal move
        holeBall(move.to)
    }

    /**
     * neuer Spielstand.
     * Ball bewegt sich seitlich und dann runter.
     * lift ball animation wird abgebrochen/beendet
     */
    fun holeBall(to: Int) {
        Log.i(TAG, "holeBall(to=${to})")
        dumpi()
        if (app == null) {
            return
        }
        var toRow = app.gameState.tubes[to].fillLevel - 1;
        val acti = findActivity() as MainActivity

        downwardsBall.color = app.gameState.tubes[to].colorOfTopmostBall()
        val startX = ballX(acti.donorIndex).toFloat()
        val stopX  = ballX(to).toFloat()
        val startY = BALL_RADIUS.toFloat()
        val stopY  = ballY(toRow).toFloat()
        val distance0 = abs(stopX - startX)
        val distance1 = abs(stopY - startY)
        val fract = distance0 / (distance0 + distance1)
        //Log.i(TAG, "fract=${fract}")
        val kX0 = Keyframe.ofFloat(0f, startX)
        val kX1 = Keyframe.ofFloat(fract, stopX)
        val kX2 = Keyframe.ofFloat(1f, stopX)
        val kY0 = Keyframe.ofFloat(0f, startY)
        val kY1 = Keyframe.ofFloat(fract, startY)
        val kY2 = Keyframe.ofFloat(1f, stopY)
        val holderX = PropertyValuesHolder.ofKeyframe("x", kX0, kX1, kX2)
        val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2)
        val downwardsAnimator = ObjectAnimator.ofPropertyValuesHolder(downwardsBall.coordinates, holderX, holderY)
        downwardsAnimator.duration = 1000
        downwardsAnimator.repeatMode = ValueAnimator.RESTART //is default
        downwardsAnimator.repeatCount = 0
        downwardsAnimator.interpolator = LinearInterpolator()
        downwardsAnimator.addUpdateListener { animation ->
            val x = animation.getAnimatedValue("x") as Float
            val y = animation.getAnimatedValue("x") as Float
            downwardsBall.coordinates.x = x
            downwardsBall.coordinates.y = y
            invalidate()
        }
        downwardsAnimator.start()

        acti.donorIndex = -1
        acti.donorRow = -1
        upwardsCol = -1
        downwardsCol = to

        dumpi()
    }

    fun resetGameView() {
        downwardsCol = -1
        upwardsCol = -1
        invalidate()
        // Todo: reset game view
    }

    /**
     * angehobener Ball wird wieder senkrecht gesenkt.
     * lift ball animation wird abgebrochen/beendet
     */
    fun dropBall(donorIndex: Int) {
        /*
        Log.i(TAG, "dropBall(donorIndex=${donorIndex})")
        dumpi()
        if (app == null) {
            return
        }

        val acti = findActivity() as MainActivity
        Log.i(TAG, "donorRow=${acti.donorRow}")

        downwardsBall.color = app.gameState.tubes[donorIndex].colorOfTopmostBall()

        val x = ballX(donorIndex).toFloat()
        val startY = BALL_RADIUS.toFloat()
        val stopY = ballY(acti.donorRow).toFloat()
        val start = Coordinates(x, startY)
        val stop = Coordinates(x, stopY)
        downwardsAnimator.setObjectValues(start, stop)

        Log.i(TAG, "downwards animation started x=${x}, startY=${startY}, stopY=${stopY}")



        downwardsAnimator.start()
        downwardsCol = donorIndex
        upwardsCol = -1
        acti.donorIndex = -1
        acti.donorRow = -1

        dumpi()

         */
    }

    companion object {
        private const val TAG = "ballas MyDrawView"

        /**
         * positions of original game board / puzzle without scaling
         */
        const val BALL_RADIUS = 40
        const val BALL_RADIUS_INSIDE = BALL_RADIUS.toFloat().minus(0.5f)
        private const val BALL_DIAMETER = BALL_RADIUS * 2
        private const val BALL_PADDING = 4
        private const val TUBE_WIDTH = BALL_DIAMETER + BALL_PADDING * 2
        private const val TUBE_LOWER_CORNER_RADIUS = 26
        private const val TUBE_PADDING = 8
        private const val BOUNCE = 20

        private val TUBE_PAINT = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0xe6, 0xe6, 0xe6) // light gray
            style = Paint.Style.FILL
        }

        /*
        private val linePaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.FILL
            strokeWidth = 10f
        }
         */
    }
}