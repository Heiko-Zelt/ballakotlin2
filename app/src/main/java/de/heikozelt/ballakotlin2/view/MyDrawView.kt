package de.heikozelt.ballakotlin2.view

//import android.view.animation.Animation
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.animation.LinearInterpolator
import de.heikozelt.ballakotlin2.GameController
import kotlin.math.abs
import kotlin.math.min

class MyDrawView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //private val app = context.applicationContext as BallaApplication?

    /**
     * Spielstatus. Initialisierung mittels Setter-Injection.
     */
    private var gameController: GameController? = null

    /**
     * Board Dimensions depend on number of tubes and tube height
     */
    private var boardWidth = 0
    private var boardHeight = 0

    /**
     * ball, which is
     * - lifting
     * - dropping or
     * - moving sidewards and dropping or
     * - moving upward, sidewards and eventually dropping
     * zunächst einfache Lösung: Es ist immer nur ein Ball animiert,
     * nie mehrere gleichzeitig.
     */
    //private val animatedBall = Ball(Coordinates(0f, 0f), 0)

    /**
     * Nummer der Spalte, in der gerade ein Ball angehoben oder gesenkt wird.
     * In dieser Spalte wird der oberste Ball nicht gezeichnet.
     * -1 fuer keine.
     */
    //private var invisibleBallCol = -1

    /**
     * Normal unanimated / not moving ball.
     * Object is only temporary of relevance but reused to draw different balls.
     */
    //private var fixedBall = Ball(Coordinates(0f, 0f), 1)

    // private var animator = ObjectAnimator.ofFloat(this, View.ROTATION, -360f, 0f)
    /*
    private var object1 = Coordinates(150f, 20f)
    private var object2 = Coordinates(100f, 150f)
    private var object3 = Coordinates(50f, 100f)
    private var upwardsAnimator = ValueAnimator.ofObject(CoordinatesEvaluator(), object1, object2, object3)
     */

    /*
     * Art der Animation ist liftBall oder liftAndHoleBall
     */
    //private var upwardsAnimator: ValueAnimator? = null

    /*
     * Art der Animation ist dropBall oder holeBall
     */
    //private var downwardsAnimator: ValueAnimator? = null

    private var myBallAnimators = MyBallAnimators()

    private var scaleFactor = 0f
    private var transY = 0f
    private var transX = 0f

    private var viewTubes: MutableList<ViewTube>? = null

    /*
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

        upwardsAnimator.addUpdateListener { animation ->
            val x = animation.getAnimatedValue("x") as Float
            val y = animation.getAnimatedValue("x") as Float
            upwardsBall.coordinates.x = x
            upwardsBall.coordinates.y = y
            invalidate()
        }

        downwardsAnimator.addUpdateListener { animation ->
            val coords = animation.animatedValue as Coordinates
            downwardsBall.coordinates = coords
            //Log.i(TAG, "onon animator update")
            invalidate()
        }
    }

         */

    /**
     * selbst definierte Methode. Setter injection.
     * Nach Drehen des Handys, geht der Status der beiden Variablen animatedBall und invisibleBallCol verloren.
     * Sie müssen wieder hergestellt werden.
     * Der Spielstatus ist erst mit setGameState1Up() bekannt.
     */
    fun setGameState1Up(gs1up: GameController?) {
        Log.i(TAG, "game state injected into MyDrawView")
        gameController = gs1up
        if (gs1up != null) {
            // Initialisation of ViewTubes 2-dimensional
            // mit Koordinaten und Ball-Farben
            calculateBalls()
        }

    }

    /**
     * initialisiert die 2-dimensionale Matrix der Bälle neu
     * z.B. nach new oder reset game
     * (Die Spielfeldgröße kann sich geändert haben, also Arrays neu anlegen.)
     */
    fun calculateBalls() {
        val gs1up = gameController
        if (gs1up != null) {
            val gs = gs1up.getGameState()
            gs.dump()
            viewTubes = MutableList(gs.numberOfTubes) { ViewTube(gs.tubeHeight) }
            val vts = viewTubes
            if (vts != null) {
                for (col in vts.indices) {
                    Log.d(TAG, "tubeHeight=${gs.tubeHeight}")
                    for (row in 0 until gs.tubeHeight) {
                        val color = gs.tubes[col].cells[row]
                        vts[col].cells[row] = if(color == 0) {
                            null
                        } else {
                            val coords = Coordinates(ballX(col).toFloat(), ballY(row).toFloat())
                            Ball(coords, color)
                        }
                    }
                }
            }

            // Spezialfall: ein Ball könnte gerade oben sein
            if (gs1up.isUp()) {
                val liftedBallCol = gs1up.getUpCol()
                val liftedBallRow = gs.tubes[gs1up.getUpCol()].fillLevel - 1 // ganz oben
                val liftedBall = viewTubes?.get(liftedBallCol)?.cells?.get(liftedBallRow)
                liftedBall?.coordinates?.y = BALL_RADIUS.toFloat() // Position korrigieren
            }
        }
    }

    /**
     * selbst definierte Methode
     * (Referenz auf Activity wird eigentlich nicht benoetigt.)
     * method is never used!
     */
    /*
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
*/

    /**
     * Methode von View geerbt.
     * Klick auf Spielfeld.
     * 1. ermitteln, welche Röhre angeklickt wurde
     * 2. Ereignis an GameState1Up weiterleiten
     */
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

        if (virtualX < 0 || virtualX > boardWidth || virtualY < 0 || virtualY > boardHeight) {
            // left or right, top or bottom outside of board
            return true
        }

        val col = (virtualX / (TUBE_WIDTH + TUBE_PADDING)).toInt()
        Log.i(TAG, "col=${col}")
        //val c = findActivity() as MainActivity?

        playSoundEffect(SoundEffectConstants.CLICK)
        gameController?.tubeClicked(col)
        return true
    }

    /*
     * Methode von View geerbt.
     * This method will be called after all children have been added
     * Berechnet Ausdehnung des Spielbretts in virtuellen Pixeln.
     * virtuelle Größe bleibt aber gleich. :-(
     * Todo: Code sollte aufgerufen werden, wenn sich die Anzahl der Röhren oder der Zeilen ändert !
    override fun onFinishInflate() {
        Log.i(TAG, "onFinishInflate()")
        super.onFinishInflate()
    }
    */

    /**
     * Berechnet die virtuelle Groesse des Spielbretts.
     */
    fun calculateBoardDimensions() {
        val gs = gameController?.getGameState()
        if (gs == null) {
            Log.e(TAG, "Kein GameState!")
            boardWidth = 400
            boardHeight = 300
            return
        }

        val numTub = gs.numberOfTubes
        boardWidth = numTub * TUBE_WIDTH + (numTub - 1) * TUBE_PADDING
        Log.i(TAG, "boardWidth: $boardWidth")

        val tubHei = gs.tubeHeight
        boardHeight = (tubHei + 1) * BALL_DIAMETER + BALL_PADDING
        Log.i(TAG, "boardHeight: $boardHeight")
    }

    /**
     * Berechnet die Transformations-Paramater zur Umrechnung
     * zwischen realen Pixels und virtuellen Einheiten.
     */
    private fun calculateTranslation(w: Int, h: Int) {
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

    fun calculateTranslation() {
        calculateTranslation(width, height)
    }

    fun flatten() {
        //invisibleBallCol = -1
        myBallAnimators.endRemoveAll()
        //upwardsAnimator?.end()
        //downwardsAnimator?.end()
    }

    /**
     * Methode von View geerbt.
     * Wird aufgerufen, wenn sich die Größe der View/des Spielfelds ändert.
     * Skalierungsfaktor und Verschiebung des Spielfeldes neu berechnen.
     */
    override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int
    ) {
        Log.i(TAG, "onSizeChanged(w=${w}, h=${h})")
        super.onSizeChanged(w, h, oldw, oldh)
        calculateBoardDimensions()
        calculateTranslation(w, h)
        calculateBalls()
    }

    /**
     * eigene Methode
     * Umrechnung von Spalte/Nummer der Röhre zu virtuellen Pixeln
     */
    private fun ballX(col: Int): Int {
        return col * (TUBE_WIDTH + TUBE_PADDING) + BALL_RADIUS + BALL_PADDING
    }

    /**
     * eigene Methode.
     * Umrechnung von Zeile zu virtuellen Pixeln.
     */
    private fun ballY(row: Int): Int {
        val gs = gameController?.getGameState()
        if (gs == null) {
            Log.e(TAG, "Kein GameState!")
            return 77
        }
        return (gs.tubeHeight - row) * BALL_DIAMETER + BALL_RADIUS
    }

    /**
     * eigene Methode.
     * Zeichnet die Röhren als Hintergrund für die Bälle.
     */
    private fun drawTubes(canvas: Canvas) {
        //Log.d(TAG, "drawTubes()")
        val gs = gameController?.getGameState()
        if (gs == null) {
            Log.e(TAG, "Kein GameState!")
            return
        }
        val numTub = gs.numberOfTubes
        //Log.d(TAG, "numTub=${numTub}")
        val tubHei = gs.tubeHeight
        val top = BALL_DIAMETER
        val bottom = top + tubHei * BALL_DIAMETER + BALL_PADDING
        val circleY = bottom - TUBE_LOWER_CORNER_RADIUS
        for (col in 0 until numTub) {
            val left = col * (TUBE_WIDTH + TUBE_PADDING)
            val right = left + TUBE_WIDTH
            val leftCircleX = left + TUBE_LOWER_CORNER_RADIUS
            val rightCircleX = right - TUBE_LOWER_CORNER_RADIUS
            canvas.drawCircle(
                    leftCircleX.toFloat(),
                    circleY.toFloat(),
                    TUBE_LOWER_CORNER_RADIUS.toFloat(),
                    TUBE_PAINT
            )
            canvas.drawCircle(
                    rightCircleX.toFloat(),
                    circleY.toFloat(),
                    TUBE_LOWER_CORNER_RADIUS.toFloat(),
                    TUBE_PAINT
            )
            canvas.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    (bottom - TUBE_LOWER_CORNER_RADIUS).toFloat(),
                    TUBE_PAINT
            )
            canvas.drawRect(
                    leftCircleX.toFloat(),
                    circleY.toFloat(),
                    rightCircleX.toFloat(),
                    bottom.toFloat(),
                    TUBE_PAINT
            )

        }
    }

    /**
     * eigene Methode.
     * draw fixed balls
     * without upwards and downwards ball
     */
    private fun drawBalls(canvas: Canvas) {
        //Log.d(TAG, "drawBalls()")

        val gs = gameController?.getGameState()
        if (gs == null) {
            Log.e(TAG, "Kein GameState!")
            return
        }

        for (col in 0 until gs.numberOfTubes) {
            val tube = gs.tubes[col]
            //Log.i(TAG, "col: ${col}")

            for (row in 0 until tube.fillLevel) {
                //Log.d(TAG, "row: ${row}")
                val ball = viewTubes?.get(col)?.cells?.get(row)
                ball?.draw(canvas)
            }
        }
    }

    /**
     * Methode von View geerbt
     * Called when the view should render its content.
     */
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
        /*
        if (invisibleBallCol != -1) {
            animatedBall.draw(canvas)
        }
        */

        canvas.restore()
    }

    /**
     * eigene Methode
     * Loggt Objekt-Variablen
     */
    private fun dumpi() {
        /*
        val acti = findActivity() as MainActivity
        Log.i(TAG, "donorIndex=${acti.donorIndex}, donorRow=${acti.donorRow}, upwardsCol=${upwardsCol}, downwardsCol=${downwardsCol}")
         */
    }

    /**
     * eigene Methode
     */
    private fun animateLiftBall(col: Int, fromRow: Int, color: Int) {
        Log.i(TAG, "animateLiftBall(col=${col}, fromRow=${fromRow}, color=${color})")
        val animatedBall = viewTubes?.get(col)?.cells?.get(fromRow)
        if (animatedBall != null) {
            //animatedBall.color = color
            //animatedBall.coordinates.x = ballX(col).toFloat()

            val startY = ballY(fromRow).toFloat()
            val topY = BALL_RADIUS.toFloat()

            val wholeTime = ANIMATION_ADDITIONAL_DURATION / 2 + (startY - topY) / ANIMATION_SPEED

            val kY0 = Keyframe.ofFloat(0f, startY)
            val kY1 = Keyframe.ofFloat(1f, topY)

            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1)

            val animator = ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            myBallAnimators.endRemoveAddStart(animator, col, fromRow)
        }
    }

    /**
     * eigene Methode
     * Ball wird angehoben. Spielstand ändert sicht nicht.
     * Falls drop ball animation gleiche Röhre betrifft,
     * dann wird sie abgebrochen/beendet.
     */
    fun liftBall(col: Int, row: Int, color: Int) {
        Log.i(TAG, "liftBall(col=${col})")
        dumpi()

        //invisibleBallCol = col
        // Eventuell laufende Animation beenden, bevor neue gestartet wird.
        //upwardsAnimator?.end()
        /*
        val gs1up = gameState1Up
        if(gs1up != null) {
            if (gs1up.isUp()) {
                animateDropBall(gs1up.getUpCol(), )
            }
        }
        */
        animateLiftBall(col, row, color)
        dumpi()
    }

    /**
     * eigene Methode
     */
    private fun animateDropBall(col: Int, toRow: Int, color: Int) {
        Log.i(TAG, "animateDropBall(col=${col}, toRow=${toRow}, color=${color})")
        val animatedBall = viewTubes?.get(col)?.cells?.get(toRow)
        if (animatedBall != null) {
            //animatedBall.color = color
            //animatedBall.coordinates.x = ballX(col).toFloat()
            val topY = BALL_RADIUS.toFloat()
            val stopY = ballY(toRow).toFloat()
            val bounceY = stopY - BOUNCE

            val time0 = ANIMATION_ADDITIONAL_DURATION + (stopY - topY) / ANIMATION_SPEED
            val wholeTime = time0 + 2 * BOUNCE_DURATION

            val fract1 = time0 / wholeTime
            val fract2 = (time0 + BOUNCE_DURATION) / wholeTime

            val kY0 = Keyframe.ofFloat(0f, topY)
            val kY1 = Keyframe.ofFloat(fract1, stopY)
            val kY2 = Keyframe.ofFloat(fract2, bounceY)
            val kY3 = Keyframe.ofFloat(1f, stopY)
            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3)

            val animator = ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            myBallAnimators.endRemoveAddStart(animator, col, toRow)
        }
    }

    /**
     * eigene Methode
     * angehobener Ball wird wieder senkrecht gesenkt.
     * lift ball animation wird abgebrochen/beendet
     */
    fun dropBall(col: Int, row: Int, color: Int) {
        Log.i(TAG, "dropBall(col=${col}, row=${row}, color=${color})")
        dumpi()

        // Eventuell laufende Animation beenden, bevor neue gestartet wird.
        //downwardsAnimator?.end()
        animateDropBall(col, row, color)
        //invisibleBallCol = col
        dumpi()
    }

    /**
     * eigene Methode
     *
     */
    private fun animateHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(TAG, "animateHoleBall(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)")
        // animierter Ball ist jetzt nicht mehr in Quell- sondern in Zielröhre
        val animatedBall = viewTubes?.get(fromCol)?.eraseTopmostBall()
        viewTubes?.get(toCol)?.cells?.set(toRow, animatedBall)

        if (animatedBall != null) {
            //animatedBall.color = color
            val startX = ballX(fromCol)
            val stopX = ballX(toCol)
            val topY = BALL_RADIUS
            val stopY = ballY(toRow)
            val bounceY = stopY - BOUNCE

            val time0 = ANIMATION_ADDITIONAL_DURATION / 2 + abs(stopX - startX) / ANIMATION_SPEED
            val time1 = ANIMATION_ADDITIONAL_DURATION / 2 + abs(stopY - topY) / ANIMATION_SPEED
            val wholeTime = time0 + time1 + 2 * BOUNCE_DURATION

            val fract1 = time0 / wholeTime
            val fract2 = (time0 + time1) / wholeTime
            val fract3 = (time0 + time1 + BOUNCE_DURATION) / wholeTime

            val kX0 = Keyframe.ofFloat(0f, startX.toFloat())
            val kX1 = Keyframe.ofFloat(fract1, stopX.toFloat())
            val kX2 = Keyframe.ofFloat(fract2, stopX.toFloat())
            val kX3 = Keyframe.ofFloat(fract2, stopX.toFloat())
            val kX4 = Keyframe.ofFloat(1f, stopX.toFloat())

            val kY0 = Keyframe.ofFloat(0f, topY.toFloat())
            val kY1 = Keyframe.ofFloat(fract1, topY.toFloat())
            val kY2 = Keyframe.ofFloat(fract2, stopY.toFloat())
            val kY3 = Keyframe.ofFloat(fract3, bounceY.toFloat())
            val kY4 = Keyframe.ofFloat(1f, stopY.toFloat())

            val holderX = PropertyValuesHolder.ofKeyframe("x", kX0, kX1, kX2, kX3, kX4)
            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3, kY4)

            val animator = ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            myBallAnimators.endRemove(fromCol, fromRow)
            myBallAnimators.endRemoveAddStart(animator, toCol, toRow)
        }
    }

    /**
     * eigene Methode.
     * neuer Spielstand.
     * Ball bewegt sich seitlich und dann runter.
     * lift ball animation wird abgebrochen/beendet
     */
    fun holeBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(TAG, "holeBall(toCol=${toCol})")
        dumpi()
        // Eventuell laufende Animation beenden, bevor neue gestartet wird.
        animateHoleBall(fromCol, toCol, fromRow, toRow, color)
        //invisibleBallCol = toCol
        dumpi()
    }

    /**
     * eigene Methode
     */
    private fun animateLiftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(TAG, "animateLiftAndHoleBall(fromCol=${fromCol}, toCol=${toCol}, fromRow=${fromRow}, toRow=${toRow}, color=${color})")
        // animierter Ball ist jetzt nicht mehr in Quell- sondern in Zielröhre
        val animatedBall = viewTubes?.get(fromCol)?.eraseTopmostBall()
        viewTubes?.get(toCol)?.cells?.set(toRow, animatedBall)

        if (animatedBall != null) {
            // animatedBall.color = color

            val startX = ballX(fromCol)
            val stopX = ballX(toCol)
            val startY = ballY(fromRow)
            val topY = BALL_RADIUS
            val stopY = ballY(toRow)
            val bounceY = stopY - BOUNCE

            val time0 = ANIMATION_ADDITIONAL_DURATION / 3 + (startY - topY) / ANIMATION_SPEED
            val time1 = ANIMATION_ADDITIONAL_DURATION / 3 + abs(stopX - startX) / ANIMATION_SPEED
            val time2 = ANIMATION_ADDITIONAL_DURATION / 3 + (stopY - topY) / ANIMATION_SPEED
            val wholeTime = time0 + time1 + time2 + 2 * BOUNCE_DURATION
            Log.i(TAG, "time: 0=${time0}, 1=${time1}, 2=${time2}, whole=${wholeTime}")

            val fract1 = time0 / wholeTime
            val fract2 = (time0 + time1) / wholeTime
            val fract3 = (time0 + time1 + time2) / wholeTime
            val fract4 = (time0 + time1 + time2 + BOUNCE_DURATION) / wholeTime
            Log.i(TAG, "fracts: 1=${fract1}, 2=${fract2}, 3=${fract3}, 4=${fract4}")

            val kX0 = Keyframe.ofFloat(0f, startX.toFloat())
            val kX1 = Keyframe.ofFloat(fract1, startX.toFloat())
            val kX2 = Keyframe.ofFloat(fract2, stopX.toFloat())
            val kX3 = Keyframe.ofFloat(fract3, stopX.toFloat())
            val kX4 = Keyframe.ofFloat(fract4, stopX.toFloat())
            val kX5 = Keyframe.ofFloat(1f, stopX.toFloat())

            val kY0 = Keyframe.ofFloat(0f, startY.toFloat())
            val kY1 = Keyframe.ofFloat(fract1, topY.toFloat())
            val kY2 = Keyframe.ofFloat(fract2, topY.toFloat())
            val kY3 = Keyframe.ofFloat(fract3, stopY.toFloat())
            val kY4 = Keyframe.ofFloat(fract4, bounceY.toFloat())
            val kY5 = Keyframe.ofFloat(1f, stopY.toFloat())

            val holderX = PropertyValuesHolder.ofKeyframe("x", kX0, kX1, kX2, kX3, kX4, kX5)
            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3, kY4, kY5)

            Log.i(TAG, "X: 0=${kX0.value}, 1=${kX1.value}, 2=${kX2.value}, 3=${kX3.value}, 4=${kX4.value}, 5=${kX5.value}")
            Log.i(TAG, "Y: 0=${kY0.value}, 1=${kY1.value}, 2=${kY2.value}, 3=${kY3.value}, 4=${kY4.value}, 5=${kY5.value}")

            val animator = ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            myBallAnimators.endRemove(fromCol, fromRow)
            myBallAnimators.endRemoveAddStart(animator, toCol, toRow)
        }
    }


    /**
     * Bei Klick auf Undo-Button.
     * Ball hochheben, wagrecht und einlochen.
     */
    fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(TAG, "liftAndHoleBall(fromCol=${fromCol}, toCol=${toCol}, fromRow=${fromRow}, toRow=${toRow}, color=${color})")
        dumpi()
        // Eventuell laufende Animation beenden, bevor neue gestartet wird.
        animateLiftAndHoleBall(fromCol, toCol, fromRow, toRow, color)
        //invisibleBallCol = toCol
        //Log.i(TAG, "invisibleBallCol=${invisibleBallCol}")
        dumpi()
    }

    fun tubeSolved(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(TAG, "tubeSolved(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)")
        val animatedBall = viewTubes?.get(fromCol)?.eraseTopmostBall()
        viewTubes?.get(toCol)?.cells?.set(toRow, animatedBall)

        var time0 = 0f
        var time1 = 0f

        if (animatedBall != null) {
            //animatedBall.color = color
            val startX = ballX(fromCol)
            val stopX = ballX(toCol)
            val topY = BALL_RADIUS
            val stopY = ballY(toRow)
            val bounceY = stopY - BOUNCE

            // Zeit für waagrechte Bewegung
            time0 = ANIMATION_ADDITIONAL_DURATION / 2 + abs(stopX - startX) / ANIMATION_SPEED

            // Zeit für abwärts Bewegung
            time1 = ANIMATION_ADDITIONAL_DURATION / 2 + abs(stopY - topY) / ANIMATION_SPEED

            val wholeTime = time0 + time1 + 2 * BOUNCE_DURATION

            val fract1 = time0 / wholeTime
            val fract2 = (time0 + time1) / wholeTime
            val fract3 = (time0 + time1 + BOUNCE_DURATION) / wholeTime

            val kX0 = Keyframe.ofFloat(0f, startX.toFloat())
            val kX1 = Keyframe.ofFloat(fract1, stopX.toFloat())
            val kX2 = Keyframe.ofFloat(fract2, stopX.toFloat())
            val kX3 = Keyframe.ofFloat(fract2, stopX.toFloat())
            val kX4 = Keyframe.ofFloat(1f, stopX.toFloat())

            val kY0 = Keyframe.ofFloat(0f, topY.toFloat())
            val kY1 = Keyframe.ofFloat(fract1, topY.toFloat())
            val kY2 = Keyframe.ofFloat(fract2, stopY.toFloat())
            val kY3 = Keyframe.ofFloat(fract3, bounceY.toFloat())
            val kY4 = Keyframe.ofFloat(1f, stopY.toFloat())

            val holderX = PropertyValuesHolder.ofKeyframe("x", kX0, kX1, kX2, kX3, kX4)
            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3, kY4)

            val animator = ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            myBallAnimators.endRemove(fromCol, fromRow)
            myBallAnimators.endRemoveAddStart(animator, toCol, toRow)
        }

        for(row in 0 until toRow) {
            val i = toRow - row
            val ball1 = viewTubes?.get(toCol)?.cells?.get(row)
            if (ball1 != null) {
                val startY = ballY(row)
                val bounceY = startY - BOUNCE
                val wholeTime = time0 + time1 + BOUNCE_DURATION * i + 2 * BOUNCE_DURATION

                val fract1 = (time0 + time1 + BOUNCE_DURATION * i) / wholeTime
                val fract2 = (time0 + time1 + BOUNCE_DURATION * (i + 1)) / wholeTime

                val kY0 = Keyframe.ofFloat(0f, startY.toFloat())
                val kY1 = Keyframe.ofFloat(fract1, startY.toFloat())
                val kY2 = Keyframe.ofFloat(fract2, bounceY.toFloat())
                val kY3 = Keyframe.ofFloat(1f, startY.toFloat())
                val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3)
                val animator = ObjectAnimator.ofPropertyValuesHolder(ball1.coordinates, holderY)
                animator.duration = wholeTime.toLong()
                animator.repeatMode = ValueAnimator.RESTART //is default
                animator.repeatCount = 0
                animator.interpolator = LinearInterpolator()
                animator.addUpdateListener {
                    invalidate()
                }
                myBallAnimators.endRemoveAddStart(animator, toCol, row)
            }
        }
    }

    /**
    fun resetGameView() {
    // Animationen stoppen und Lifted Ball löschen???!?
    }
     */

    companion object {
        private const val TAG = "balla.MyDrawView"

        /**
         * positions and sizes of original game board / puzzle without scaling and translation
         */

        /**
         * virtueller Radius eine Balls
         */
        const val BALL_RADIUS = 40
        const val BALL_RADIUS_INSIDE = BALL_RADIUS.toFloat().minus(0.5f)

        /**
         * virtueller Durchmesser eines Balls
         */
        private const val BALL_DIAMETER = BALL_RADIUS * 2

        /**
         * virtueller (seitlicher) Abstand zwischen Ball und Röhre
         */
        private const val BALL_PADDING = 4

        /**
         * Breite einer Röhre
         */
        private const val TUBE_WIDTH = BALL_DIAMETER + BALL_PADDING * 2

        /**
         * Radius der unteren Rundung einer "Ecke" einer Röhre
         */
        private const val TUBE_LOWER_CORNER_RADIUS = 26

        /**
         * Abstand zwischen 2 Röhren
         */
        private const val TUBE_PADDING = 8

        /**
         * Höhe des Dotzens eines Balles beim fallen lassen/einlochen
         */
        private const val BOUNCE = 30

        /**
         * Speed of animations in virtual pixels per millisecond
         * (except while bouncing)
         */
        private const val ANIMATION_SPEED = 4f

        /**
         * Time for bouncing up or down (slow)
         */
        private const val BOUNCE_DURATION = 60f

        /**
         * Minimum duration of animations, if the distance would be zero
         * (without bouncing)
         */
        private const val ANIMATION_ADDITIONAL_DURATION = 200f

        /**
         * Farbe der Röhren
         */
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