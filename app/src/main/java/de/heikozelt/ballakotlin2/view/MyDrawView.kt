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
import androidx.core.animation.doOnEnd
import de.heikozelt.ballakotlin2.GameController
import kotlin.math.abs
import kotlin.math.sqrt


class MyDrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //private val app = context.applicationContext as BallaApplication?

    /**
     * Spielstatus. Initialisierung mittels Setter-Injection.
     */
    private var gameController: GameController? = null

    /*
     * Board Dimensions depend on number of tubes and tube height
     */
    //private var boardWidth = 0
    //private var boardHeight = 0

    var boardLayout: BoardLayout? = null

    private var myBallAnimators = MyBallAnimators()

    //private var scaleFactor = 0f
    //private var transY = 0f
    //private var transX = 0f

    private var viewTubes: MutableList<ViewTube>? = null

    /**
     * selbst definierte Methode. Setter injection.
     * Nach Drehen des Handys, geht der Status der beiden Variablen animatedBall und invisibleBallCol verloren.
     * Sie müssen wieder hergestellt werden.
     * Der Spielstatus ist erst mit setGameState1Up() bekannt.
     */
    fun setGameController(controller: GameController?) {
        Log.i(TAG, "game state injected into MyDrawView")
        gameController = controller ?: return

        selectBoardLayout(width, height)

        // Initialisation of ViewTubes 2-dimensional
        // mit Koordinaten und Ball-Farben
        calculateBalls()
    }

    fun selectBoardLayout() {
        selectBoardLayout(width, height)
    }

    fun selectBoardLayout(w: Int, h: Int) {
        val gs = gameController?.getGameState() ?: return
        val candidate1 = OneRowLayout(gs.numberOfTubes, gs.tubeHeight)
        candidate1.calculateTransformation(w, h)
        val candidate2 = TwoRowsLayout(gs.numberOfTubes, gs.tubeHeight)
        candidate2.calculateTransformation(w, h)
        Log.d(
            TAG,
            "OneRowLayout.scaleFactor=$candidate1.scaleFactor, TwoRowsLayout.scaleFactor=$candidate2.scaleFactor"
        )
        boardLayout = if (candidate1.scaleFactor > candidate2.scaleFactor) {
            Log.d(TAG, "selecting OneRowLayout")
            candidate1
        } else {
            Log.d(TAG, "selecting TwoRowsLayout")
            candidate2
        }
    }

    /**
     * initialisiert die 2-dimensionale Matrix der Bälle neu
     * z.B. nach new oder reset game
     * (Die Spielfeldgröße kann sich geändert haben, also Arrays neu anlegen.)
     */
    fun calculateBalls() {
        val bL = boardLayout ?: return

        val controller = gameController
        if (controller != null) {
            val gs = controller.getGameState()
            gs.dump()
            viewTubes = MutableList(gs.numberOfTubes) { ViewTube(gs.tubeHeight) }
            val vts = viewTubes
            if (vts != null) {
                for (col in vts.indices) {
                    //Log.d(TAG, "tubeHeight=${gs.tubeHeight}")
                    for (row in 0 until gs.tubeHeight) {
                        val color = gs.tubes[col].cells[row]
                        vts[col].cells[row] = if (color == 0) {
                            null
                        } else {
                            val coords =
                                Coordinates(bL.ballX(col).toFloat(), bL.ballY(col, row).toFloat())
                            Ball(coords, color)
                        }
                    }
                }
            }

            // Spezialfall: ein Ball könnte gerade oben sein
            if (controller.isUp()) {
                val liftedBallCol = controller.getUpCol()
                val liftedBallRow = gs.tubes[controller.getUpCol()].fillLevel - 1 // oberer Ball
                val liftedBall = viewTubes?.get(liftedBallCol)?.cells?.get(liftedBallRow)
                liftedBall?.coordinates?.y = bL.liftedBallY(liftedBallCol)
                    .toFloat() // Position korrigieren, oberhalb der Röhre
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

        Log.d(TAG, "touched x=${event.x}, y=${event.y}")

        val bL = boardLayout ?: return true

        val virtualX = bL.virtualX(event.x)
        //Log.i(TAG, "virtualX=${virtualX}")
        val virtualY = bL.virtualY(event.y)

        if (bL.isInside(virtualX, virtualY)) {
            val col = bL.column(virtualX, virtualY)
            Log.i(TAG, "clicked on col=${col}")
            playSoundEffect(SoundEffectConstants.CLICK)
            gameController?.tubeClicked(col)
        }

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

    fun calculateTranslation() {
        boardLayout?.calculateTransformation(width, height)
    }

    // wozu eigentlich?
    fun flatten() {
        //invisibleBallCol = -1
        //myBallAnimators.endRemoveAll()
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
        selectBoardLayout(w, h)
        calculateBalls()
    }


    /**
     * eigene Methode.
     * Zeichnet die Röhren als Hintergrund für die Bälle.
     */
    private fun drawTubes(canvas: Canvas) {
        //Log.d(TAG, "drawTubes()")
        val bL = boardLayout ?: return

        val gs = gameController?.getGameState()
        if (gs == null) {
            Log.e(TAG, "Kein GameState!")
            return
        }
        val numTub = gs.numberOfTubes
        //Log.d(TAG, "numTub=${numTub}")
        val tubHei = gs.tubeHeight
        for (col in 0 until numTub) {
            val left = bL.tubeX(col)
            val top = bL.tubeY(col)
            //Log.d(TAG, "left: $left, top: $top")

            val right = left + TUBE_WIDTH
            val bottom = top + tubHei * BALL_DIAMETER + BALL_PADDING
            val circleY = bottom - TUBE_LOWER_CORNER_RADIUS
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
     * Todo: erst Hintergrundbälle zeichnen, dann bewegte Bälle
     */
    private fun drawBalls(canvas: Canvas) {
        //Log.d(TAG, "drawBalls()")

        val gs = gameController?.getGameState()
        if (gs == null) {
            Log.e(TAG, "Kein GameState!")
            return
        }

        // erst (viele) Hintergrund-Bälle zeichnen
        for (col in 0 until gs.numberOfTubes) {
            val tube = gs.tubes[col]
            //Log.i(TAG, "col: ${col}")

            for (row in 0 until tube.fillLevel) {
                //Log.d(TAG, "row: ${row}")
                val ball = viewTubes?.get(col)?.cells?.get(row)
                if (ball != null) {
                    if (!ball.foreground) {
                        ball.draw(canvas)
                    }
                }
            }
        }

        // dann (wenige) Vordergrundbälle zeichnen
        for (col in 0 until gs.numberOfTubes) {
            val tube = gs.tubes[col]
            //Log.i(TAG, "col: ${col}")

            for (row in 0 until tube.fillLevel) {
                //Log.d(TAG, "row: ${row}")
                val ball = viewTubes?.get(col)?.cells?.get(row)
                if (ball != null) {
                    if (ball.foreground) {
                        ball.draw(canvas)
                    }
                }
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


        val bL = boardLayout ?: return

        canvas.save()

        //Log.i(TAG, "onon canvas.height=${canvas.height}, canvas.width=${canvas.width}, scaleFacor=${scaleFactor}")
        canvas.scale(bL.scaleFactor, bL.scaleFactor)
        //Log.i(TAG, "onon canvas.height=${canvas.height}, canvas.width=${canvas.width}")
        //canvas.translate((canvas.width - boardWidth) / 2f, (canvas.height - boardHeight) / 2f)

        canvas.translate(bL.translateX, bL.translateY)

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
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(col)?.cells?.get(fromRow)
        if (animatedBall != null) {
            //animatedBall.color = color
            //animatedBall.coordinates.x = ballX(col).toFloat()

            val startY = bL.ballY(col, fromRow).toFloat()
            val topY = bL.liftedBallY(col).toFloat() //BALL_RADIUS.toFloat()

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
            animator.doOnEnd {
                myBallAnimators.remove(col, fromRow)
                viewTubes?.get(col)?.cells?.get(fromRow)?.foreground = false
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
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(col)?.cells?.get(toRow)
        if (animatedBall != null) {
            //animatedBall.color = color
            //animatedBall.coordinates.x = ballX(col).toFloat()
            val topY = bL.liftedBallY(col).toFloat() //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(col, toRow).toFloat()
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
            animator.doOnEnd {
                myBallAnimators.remove(col, toRow)
                viewTubes?.get(col)?.cells?.get(toRow)?.foreground = false
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
        Log.i(
            TAG,
            "animateHoleBall(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)"
        )
        val bL = boardLayout ?: return
        // animierter Ball ist jetzt nicht mehr in Quell- sondern in Zielröhre
        val animatedBall = viewTubes?.get(fromCol)?.eraseTopmostBall()
        viewTubes?.get(toCol)?.cells?.set(toRow, animatedBall)

        if (animatedBall != null) {
            //animatedBall.color = color
            val startX = bL.ballX(fromCol)
            val stopX = bL.ballX(toCol)
            val topY1 = bL.liftedBallY(fromCol) //BALL_RADIUS.toFloat()
            val topY2 = bL.liftedBallY(toCol) //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(toCol, toRow)
            val bounceY = stopY - BOUNCE

            // waagrecht oder diagonale
            val time0 = ANIMATION_ADDITIONAL_DURATION / 2 + diagonalDistance(
                startX,
                stopX,
                topY1,
                topY2
            ) / ANIMATION_SPEED
            // senkrecht runter
            val time1 = ANIMATION_ADDITIONAL_DURATION / 2 + (stopY - topY2) / ANIMATION_SPEED
            val wholeTime = time0 + time1 + 2 * BOUNCE_DURATION

            val fract1 = time0 / wholeTime // waagrecht oder diagonal
            val fract2 = (time0 + time1) / wholeTime // senkrecht runter
            val fract3 = (time0 + time1 + BOUNCE_DURATION) / wholeTime // Bounce hoch

            val kX0 = Keyframe.ofFloat(0f, startX.toFloat())
            val kX1 = Keyframe.ofFloat(fract1, stopX.toFloat()) // diagonal
            val kX2 = Keyframe.ofFloat(fract2, stopX.toFloat()) // senkrecht runter
            val kX3 = Keyframe.ofFloat(fract3, stopX.toFloat()) // bounce hoch
            val kX4 = Keyframe.ofFloat(1f, stopX.toFloat()) // bounce runter

            val kY0 = Keyframe.ofFloat(0f, topY1.toFloat())
            val kY1 = Keyframe.ofFloat(fract1, topY2.toFloat()) // diagonal
            val kY2 = Keyframe.ofFloat(fract2, stopY.toFloat()) // senkrecht runter
            val kY3 = Keyframe.ofFloat(fract3, bounceY.toFloat()) // bounce hoch
            val kY4 = Keyframe.ofFloat(1f, stopY.toFloat()) // bounce runter

            val holderX = PropertyValuesHolder.ofKeyframe("x", kX0, kX1, kX2, kX3, kX4)
            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3, kY4)

            val animator =
                ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(toCol, toRow)
                viewTubes?.get(toCol)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemove(fromCol, fromRow)
            viewTubes?.get(toCol)?.cells?.get(toRow)?.foreground = true
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
    private fun animateLiftAndHoleBall(
        fromCol: Int,
        toCol: Int,
        fromRow: Int,
        toRow: Int,
        color: Int
    ) {
        Log.i(
            TAG,
            "animateLiftAndHoleBall(fromCol=${fromCol}, toCol=${toCol}, fromRow=${fromRow}, toRow=${toRow}, color=${color})"
        )
        val bL = boardLayout ?: return
        // animierter Ball ist jetzt nicht mehr in Quell- sondern in Zielröhre
        val animatedBall = viewTubes?.get(fromCol)?.eraseTopmostBall()
        viewTubes?.get(toCol)?.cells?.set(toRow, animatedBall)

        if (animatedBall != null) {
            // animatedBall.color = color

            val startX = bL.ballX(fromCol)
            val stopX = bL.ballX(toCol)
            val startY = bL.ballY(fromCol, fromRow)
            //val topY = BALL_RADIUS
            val topY1 = bL.liftedBallY(fromCol) //BALL_RADIUS.toFloat()
            val topY2 = bL.liftedBallY(toCol) //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(toCol, toRow)
            val bounceY = stopY - BOUNCE

            val time0 =
                ANIMATION_ADDITIONAL_DURATION / 3 + (startY - topY1) / ANIMATION_SPEED // hoch

            // waagrecht oder diagonale
            val time1 = ANIMATION_ADDITIONAL_DURATION / 3 + diagonalDistance(
                startX,
                stopX,
                topY1,
                topY2
            ) / ANIMATION_SPEED

            val time2 =
                ANIMATION_ADDITIONAL_DURATION / 3 + (stopY - topY2) / ANIMATION_SPEED // runter
            val wholeTime = time0 + time1 + time2 + 2 * BOUNCE_DURATION
            Log.i(TAG, "time: 0=${time0}, 1=${time1}, 2=${time2}, whole=${wholeTime}")

            val fract1 = time0 / wholeTime
            val fract2 = (time0 + time1) / wholeTime
            val fract3 = (time0 + time1 + time2) / wholeTime
            val fract4 = (time0 + time1 + time2 + BOUNCE_DURATION) / wholeTime
            Log.i(TAG, "fracts: 1=${fract1}, 2=${fract2}, 3=${fract3}, 4=${fract4}")

            val kX0 = Keyframe.ofFloat(0f, startX.toFloat())
            val kX1 = Keyframe.ofFloat(fract1, startX.toFloat()) // hoch
            val kX2 = Keyframe.ofFloat(fract2, stopX.toFloat()) // seitlich oder diagonal
            val kX3 = Keyframe.ofFloat(fract3, stopX.toFloat()) // runter
            val kX4 = Keyframe.ofFloat(fract4, stopX.toFloat()) // bounce hoch
            val kX5 = Keyframe.ofFloat(1f, stopX.toFloat()) // bounce runter

            val kY0 = Keyframe.ofFloat(0f, startY.toFloat())
            val kY1 = Keyframe.ofFloat(fract1, topY1.toFloat()) // hoch
            val kY2 = Keyframe.ofFloat(fract2, topY2.toFloat()) // seitlich oder diagonal
            val kY3 = Keyframe.ofFloat(fract3, stopY.toFloat()) // runter
            val kY4 = Keyframe.ofFloat(fract4, bounceY.toFloat()) // bounce hoch
            val kY5 = Keyframe.ofFloat(1f, stopY.toFloat()) // bounce runter

            val holderX = PropertyValuesHolder.ofKeyframe("x", kX0, kX1, kX2, kX3, kX4, kX5)
            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3, kY4, kY5)

            /*
            Log.d(
                TAG,
                "X: 0=${kX0.value}, 1=${kX1.value}, 2=${kX2.value}, 3=${kX3.value}, 4=${kX4.value}, 5=${kX5.value}"
            )
            Log.d(
                TAG,
                "Y: 0=${kY0.value}, 1=${kY1.value}, 2=${kY2.value}, 3=${kY3.value}, 4=${kY4.value}, 5=${kY5.value}"
            )
            */

            val animator =
                ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(toCol, toRow)
                viewTubes?.get(toCol)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemove(fromCol, fromRow)
            viewTubes?.get(toCol)?.cells?.get(toRow)?.foreground = true
            myBallAnimators.endRemoveAddStart(animator, toCol, toRow)
        }
    }


    /**
     * Bei Klick auf Undo-Button.
     * Ball hochheben, wagrecht und einlochen.
     */
    fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(
            TAG,
            "liftAndHoleBall(fromCol=${fromCol}, toCol=${toCol}, fromRow=${fromRow}, toRow=${toRow}, color=${color})"
        )
        dumpi()
        // Eventuell laufende Animation beenden, bevor neue gestartet wird.
        animateLiftAndHoleBall(fromCol, toCol, fromRow, toRow, color)
        //invisibleBallCol = toCol
        //Log.i(TAG, "invisibleBallCol=${invisibleBallCol}")
        dumpi()
    }

    fun tubeSolved(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(
            TAG,
            "tubeSolved(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)"
        )
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(fromCol)?.eraseTopmostBall()
        viewTubes?.get(toCol)?.cells?.set(toRow, animatedBall)

        var time0 = 0f
        var time1 = 0f

        if (animatedBall != null) {
            //animatedBall.color = color
            val startX = bL.ballX(fromCol)
            val stopX = bL.ballX(toCol)
            val topY1 = bL.liftedBallY(fromCol) //BALL_RADIUS.toFloat()
            val topY2 = bL.liftedBallY(toCol) //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(toCol, toRow)
            val bounceY = stopY - BOUNCE

            // waagrecht oder diagonale
            time0 = ANIMATION_ADDITIONAL_DURATION / 2 + diagonalDistance(
                startX,
                stopX,
                topY1,
                topY2
            ) / ANIMATION_SPEED

            // Zeit für abwärts Bewegung
            time1 = ANIMATION_ADDITIONAL_DURATION / 2 + abs(stopY - topY2) / ANIMATION_SPEED

            val wholeTime = time0 + time1 + 2 * BOUNCE_DURATION

            val fract1 = time0 / wholeTime
            val fract2 = (time0 + time1) / wholeTime
            val fract3 = (time0 + time1 + BOUNCE_DURATION) / wholeTime

            val kX0 = Keyframe.ofFloat(0f, startX.toFloat())
            val kX1 = Keyframe.ofFloat(fract1, stopX.toFloat()) // waagrecht oder diagnonal
            val kX2 = Keyframe.ofFloat(fract2, stopX.toFloat()) // senkrecht runter
            val kX3 = Keyframe.ofFloat(fract2, stopX.toFloat()) // bounce hoch
            val kX4 = Keyframe.ofFloat(1f, stopX.toFloat()) // bounce runter

            val kY0 = Keyframe.ofFloat(0f, topY1.toFloat())
            val kY1 = Keyframe.ofFloat(fract1, topY2.toFloat()) // waagrecht oder diagnonal
            val kY2 = Keyframe.ofFloat(fract2, stopY.toFloat()) // senkrecht runter
            val kY3 = Keyframe.ofFloat(fract3, bounceY.toFloat()) // bounce hoch
            val kY4 = Keyframe.ofFloat(1f, stopY.toFloat()) // bounce runter

            val holderX = PropertyValuesHolder.ofKeyframe("x", kX0, kX1, kX2, kX3, kX4)
            val holderY = PropertyValuesHolder.ofKeyframe("y", kY0, kY1, kY2, kY3, kY4)

            val animator =
                ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeTime.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(toCol, toRow)
                viewTubes?.get(toCol)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemove(fromCol, fromRow)
            viewTubes?.get(toCol)?.cells?.get(toRow)?.foreground = true
            myBallAnimators.endRemoveAddStart(animator, toCol, toRow)
        }

        for (row in 0 until toRow) {
            val i = toRow - row
            val ball1 = viewTubes?.get(toCol)?.cells?.get(row)
            if (ball1 != null) {
                val startY = bL.ballY(toCol, row)
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
                animator.doOnEnd {
                    myBallAnimators.remove(toCol, row)
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

    /**
     * Integer precision is sufficient
     */
    private fun diagonalDistance(x1: Int, x2: Int, y1: Int, y2: Int): Int {
        return diagonalDistance(x2 - x1, y2 - y1)
    }

    /**
     * Pythagorean theorem
     * c^2 = a^2 + b^2
     */
    private fun diagonalDistance(a: Int, b: Int): Int {
        return sqrt((a * a + b * b).toDouble()).toInt()
    }

    companion object {
        private const val TAG = "balla.MyDrawView"

        /**
         * Speed factor
         * 1.0f for normal speed
         * 0.1f for testing in slow motion
         */
        const val SPEED_FACTOR = 1.0f
        //const val SPEED_FACTOR = 0.1f

        /**
         * virtual positions and sizes of original game board / puzzle without scaling and translation
         */

        /**
         * virtueller Radius eine Balls
         */
        const val BALL_RADIUS = 10

        /**
         * Es sieht haesslich aus, wenn die Bälle sich berühren.
         * Deswegen ein bisschen kleiner zeichnen.
         */
        const val BALL_RADIUS_INSIDE = BALL_RADIUS.toFloat() - 0.13f

        /**
         * virtueller Durchmesser eines Balls
         */
        const val BALL_DIAMETER = BALL_RADIUS * 2

        /**
         * virtueller (seitlicher und unterer) Abstand zwischen Ball und Röhre
         */
        const val BALL_PADDING = 1

        /**
         * Breite einer Röhre
         */
        const val TUBE_WIDTH = BALL_DIAMETER + BALL_PADDING * 2

        /**
         * Radius der unteren Rundung einer "Ecke" einer Röhre
         */
        private const val TUBE_LOWER_CORNER_RADIUS = 7

        /**
         * Abstand zwischen 2 Röhren
         */
        const val TUBE_PADDING = 2

        /**
         * Höhe des Dotzens eines Balles beim fallen lassen/einlochen
         */
        private const val BOUNCE = 8

        /**
         * Speed of animations in virtual pixels per millisecond
         * (except while bouncing)
         */
        private const val ANIMATION_SPEED = 1f * SPEED_FACTOR

        /**
         * Time for bouncing up or down (slow)
         */
        private const val BOUNCE_DURATION = 60f / SPEED_FACTOR

        /**
         * Minimum duration of animations, if the distance would be zero
         * (without bouncing)
         */
        private const val ANIMATION_ADDITIONAL_DURATION = 200f / SPEED_FACTOR

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