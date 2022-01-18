package de.heikozelt.ballakotlin2.view

//import android.view.animation.Animation
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import de.heikozelt.ballakotlin2.GameController
import de.heikozelt.ballakotlin2.R


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
     * verantwortlich für die Anordnung der Röhren und Bälle
     */
    var boardLayout: BoardLayout? = null

    /**
     * registriert und verwaltet Animators
     */
    private var myBallAnimators = MyBallAnimators()

    /**
     * Tubes
     */
    private var viewTubes: MutableList<ViewTube>? = null

    /**
     * wird von MainActivity injiziert.
     * Animationen abspielen oder nicht?
     */
    var playAnimations: Boolean = true

    /**
     * wird von MainActivity injiziert
     * Töne abspielen oder nicht?
     */
    var playSound: Boolean = true

    /**
     * Geräusche
     */
    private var soundPool: SoundPool? = null

    /**
     * Dotzender Tennisball in Röhre
     */
    private var bounceSound = 0

    /**
     * Farben mit Farbwerten von R.id.color.ball0 .. ball15
     */
    private var paints = emptyArray<Paint?>()

    private var tubePaint: Paint? = null

    /**
     * läd paints[] mit Farbwerten von R.id.color.ball0 .. ball15
     */
    fun initPaints(context: Context) {
        tubePaint = Paint()
        tubePaint?.color = ContextCompat.getColor(context, R.color.tube)
        tubePaint?.style = Paint.Style.FILL

        paints = Array(DimensionsActivity.MAX_COLORS + 1) { null }

        val paintsArray: TypedArray = resources.obtainTypedArray(R.array.ball_colors)
        for (i in 0..DimensionsActivity.MAX_COLORS) {
            paints[i] =  Paint(ANTI_ALIAS_FLAG)
            paints[i]?.color = paintsArray.getColor(i,0)
            paints[i]?.style = Paint.Style.FILL
        }
        paintsArray.recycle()

        /*

        for (i in 0..DimensionsActivity.MAX_COLORS) {
            // Introspection
            val colorClass = R.color::class.java
            // null = Java static field / no object
            val color = colorClass.getDeclaredField("ball$i").getInt(null)
            paints[i] = Paint(Paint.ANTI_ALIAS_FLAG)
            paints[i]?.color = ContextCompat.getColor(context, color)
            paints[i]?.style = Paint.Style.FILL
        }
         */
    }


    fun initSoundPool(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool =
            SoundPool.Builder().setMaxStreams(PARALLEL_SOUNDS)
                .setAudioAttributes(audioAttributes).build()

        soundPool?.let { sp ->
            bounceSound = sp.load(context, R.raw.bounce, 1)
        }
    }

    fun destroySoundPool() {
        soundPool?.release()
        soundPool = null
    }

    /**
     * Spiel (sofort) einen Bounce-Ton ab.
     * (wenn Töne aktiviert sind)
     */
    private fun playBounceSound() {
        if (playSound) {
            soundPool?.play(bounceSound, 1f, 1f, 0, 0, 1f)
            Log.d(TAG, "playing bounce sound now")
        }
    }

    /**
     * Spiel nach der angegebene Zeit einen Bounce-Ton ab.
     * @param time Zeitverzögerung in Millisekunden
     */
    private fun playBounceSoundAfter(time: Float) {
        if (playSound) {
            Handler(Looper.getMainLooper()).postDelayed({
                playBounceSound()
            }, time.toLong())
        }
    }

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
        val controller = gameController ?: return
        val gs = gameController?.getGameState() ?: return
        gs.dump()
        viewTubes = MutableList(gs.numberOfTubes) { ViewTube(gs.tubeHeight) }
        val vts = viewTubes
        if (vts != null) {
            for (column in vts.indices) {
                //Log.d(TAG, "tubeHeight=${gs.tubeHeight}")
                for (row in 0 until gs.tubeHeight) {
                    val color = gs.tubes[column].cells[row]
                    vts[column].cells[row] = if (color == 0) {
                        null
                    } else {
                        val coords =
                            Coordinates(bL.ballX(column).toFloat(), bL.ballY(column, row).toFloat())
                        val b = Ball(coords)
                        b.setColor(color, paints)
                        b
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
        val virtualY = bL.virtualY(event.y)

        if (bL.isInside(virtualX, virtualY)) {
            val column = bL.column(virtualX, virtualY)
            Log.i(TAG, "clicked on col=${column}")
            if (playSound) {
                Log.d(TAG, "play click sound")
                playSoundEffect(SoundEffectConstants.CLICK)
            }
            gameController?.tubeClicked(column)
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

    /*
    // wozu eigentlich?
    fun flatten() {
        //invisibleBallCol = -1
        //myBallAnimators.endRemoveAll()
        //upwardsAnimator?.end()
        //downwardsAnimator?.end()
    }
    */

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
        tubePaint?.let { tp ->
            for (column in 0 until numTub) {
                val left = bL.tubeX(column)
                val top = bL.tubeY(column)
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
                    tp
                )
                canvas.drawCircle(
                    rightCircleX.toFloat(),
                    circleY.toFloat(),
                    TUBE_LOWER_CORNER_RADIUS.toFloat(),
                    tp
                )
                canvas.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    (bottom - TUBE_LOWER_CORNER_RADIUS).toFloat(),
                    tp
                )
                canvas.drawRect(
                    leftCircleX.toFloat(),
                    circleY.toFloat(),
                    rightCircleX.toFloat(),
                    bottom.toFloat(),
                    tp
                )
            }
        }
    }

    /**
     * eigene Methode.
     * Erst werden die Hintergrundbälle gezeichnet, dann bewegte Bälle.
     */
    private fun drawBalls(canvas: Canvas) {
        //Log.d(TAG, "drawBalls()")
        //val yellow = ContextCompat.getColor(context, R.color.ball1)

        val gs = gameController?.getGameState()
        if (gs == null) {
            Log.e(TAG, "Kein GameState!")
            return
        }

        // erst (viele) Hintergrund-Bälle zeichnen
        for (column in 0 until gs.numberOfTubes) {
            val tube = gs.tubes[column]
            //Log.i(TAG, "col: ${col}")

            for (row in 0 until tube.fillLevel) {
                //Log.d(TAG, "row: ${row}")
                val ball = viewTubes?.get(column)?.cells?.get(row)
                if (ball != null) {
                    if (!ball.foreground) {
                        ball.draw(canvas)
                    }
                }
            }
        }

        // dann (wenige) Vordergrundbälle zeichnen
        for (column in 0 until gs.numberOfTubes) {
            val tube = gs.tubes[column]
            //Log.i(TAG, "col: ${col}")

            for (row in 0 until tube.fillLevel) {
                //Log.d(TAG, "row: ${row}")
                val ball = viewTubes?.get(column)?.cells?.get(row)
                if (ball != null) {
                    if (ball.foreground) {
                        ball.draw(canvas)
                    }
                }
            }
        }
    }

    /**
     * Methode von View geerbt.
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

        //Log.i(TAG, "canvas.height=${canvas.height}, canvas.width=${canvas.width}, scaleFacor=${scaleFactor}")
        canvas.scale(bL.scaleFactor, bL.scaleFactor)
        //Log.i(TAG, "canvas.height=${canvas.height}, canvas.width=${canvas.width}")
        //canvas.translate((canvas.width - boardWidth) / 2f, (canvas.height - boardHeight) / 2f)

        canvas.translate(bL.translateX, bL.translateY)

        //Log.i(TAG, "circleX=${circleX}, circleY=${circleY}, radius=${radius}")
        //canvas.drawLine(0f, 0f, boardWidth.toFloat(), boardHeight.toFloat(), linePaint)
        //canvas.drawLine(0f, boardHeight.toFloat(), boardWidth.toFloat(), 0f, linePaint)

        drawTubes(canvas)
        drawBalls(canvas)
        canvas.restore()
    }

    /**
     * eigene Methode
     * Ball wird angehoben. Spielstand ändert sicht nicht.
     * Falls drop ball animation gleiche Röhre betrifft,
     * dann wird sie abgebrochen/beendet.
     * todo: nicht animieren, wenn Benutzer Animationen abgeschaltet hat
     */
    fun liftBall(column: Int, row: Int) {
        Log.i(TAG, "liftBall(col=${column}, row=${row})")
        if (playAnimations) {
            animateLiftBall(column, row)
        } else {
            immediateLiftBall(column, row)
        }
    }

    /**
     * eigene Methode
     * angehobener Ball wird wieder senkrecht gesenkt.
     * lift ball animation wird abgebrochen/beendet
     */
    fun dropBall(column: Int, row: Int) {
        Log.i(TAG, "dropBall(col=${column}, row=${row})")
        if (playAnimations) {
            animateDropBall(column, row)
        } else {
            immediateDropBall(column, row)
        }
    }

    /**
     * eigene Methode.
     * neuer Spielstand.
     * Ball bewegt sich seitlich und dann runter.
     * lift ball animation wird abgebrochen/beendet
     */
    fun holeBall(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        Log.i(TAG, "holeBall(fromCol=$fromColumn, toCol=$toColumn, fromRow=$fromRow, toRow=$toRow)")

        // animierter Ball ist jetzt nicht mehr in Quell- sondern in Zielröhre
        // (logisch betrachtet, Koordinaten ändern sich erst danach)
        val animatedBall = viewTubes?.get(fromColumn)?.eraseTopmostBall()
        viewTubes?.get(toColumn)?.cells?.set(toRow, animatedBall)

        if (playAnimations) {
            animateHoleBall(fromColumn, toColumn, fromRow, toRow)
        } else {
            immediateHoleBall(toColumn, toRow)
        }
    }

    /**
     * Bei Klick auf Undo-Button.
     * Ball hochheben, wagrecht und einlochen.
     */
    fun liftAndHoleBall(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        Log.i(
            TAG,
            "liftAndHoleBall(fromCol=$fromColumn, toCol=$toColumn, fromRow=$fromRow, toRow=$toRow)"
        )
        // Ball wechselt seine "Identität", gehört jetzt zur neuen Röhre
        // Seine Koordinaten sind aber vorerst die gleichen.
        val animatedBall = viewTubes?.get(fromColumn)?.eraseTopmostBall()
        viewTubes?.get(toColumn)?.cells?.set(toRow, animatedBall)

        if (playAnimations) {
            animateLiftAndHoleBall(fromColumn, toColumn, fromRow, toRow)
        } else {
            immediateHoleBall(toColumn, toRow)
        }
    }

    /**
     * von GameObserverInterface geerbt
     */
    fun holeBallTubeSolved(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        // Ball wechselt seine "Identität", gehört jetzt zur neuen Röhre
        // Seine Koordinaten sind aber vorerst die gleichen.
        val animatedBall = viewTubes?.get(fromColumn)?.eraseTopmostBall()
        viewTubes?.get(toColumn)?.cells?.set(toRow, animatedBall)

        if (playAnimations) {
            animateHoleBallTubeSolved(fromColumn, toColumn, fromRow, toRow)
        } else {
            immediateHoleBall(toColumn, toRow)
        }
    }

    /**
     * von GameObserverInterface geerbt
     */
    fun liftAndHoleBallTubeSolved(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        // Ball wechselt seine "Identität", gehört jetzt zur neuen Röhre
        // Seine Koordinaten sind aber vorerst die gleichen.
        val animatedBall = viewTubes?.get(fromColumn)?.eraseTopmostBall()
        viewTubes?.get(toColumn)?.cells?.set(toRow, animatedBall)

        if (playAnimations) {
            animateLiftAndHoleBallTubeSolved(fromColumn, toColumn, fromRow, toRow)
        } else {
            immediateHoleBall(toColumn, toRow)
        }
    }

    private fun immediateLiftBall(column: Int, row: Int) {
        boardLayout?.let { bl ->
            val topY = bl.liftedBallY(column).toFloat()
            viewTubes?.get(column)?.cells?.get(row)?.coordinates?.y = topY
            Log.i(TAG, "Ball hat Position geändert / Spielbrett neu zeichnen")
            invalidate()
        }
    }

    private fun immediateDropBall(column: Int, row: Int) {
        boardLayout?.let { bl ->
            val endY = bl.ballY(column, row).toFloat()
            viewTubes?.get(column)?.cells?.get(row)?.coordinates?.y = endY
            Log.i(TAG, "Ball hat Position geändert / Spielbrett neu zeichnen")
            invalidate()
        }
        playBounceSound()
    }

    private fun immediateHoleBall(toColumn: Int, toRow: Int) {
        boardLayout?.let { bl ->
            val stopY = bl.ballY(toColumn, toRow).toFloat()
            val stopX = bl.ballX(toColumn).toFloat()
            viewTubes?.get(toColumn)?.cells?.get(toRow)?.coordinates?.y = stopY
            viewTubes?.get(toColumn)?.cells?.get(toRow)?.coordinates?.x = stopX
            Log.i(TAG, "Ball hat Position geändert / Spielbrett neu zeichnen")
            invalidate()
        }
        playBounceSound()
    }

    /**
     * eigene Methode
     * todo: Animation langsamer oder schnellers, je nach Einstellung des Benutzers
     */
    private fun animateLiftBall(column: Int, fromRow: Int) {
        Log.i(TAG, "animateLiftBall(col=$column, fromRow=$fromRow)")
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(column)?.cells?.get(fromRow)
        if (animatedBall != null) {
            //animatedBall.color = color
            //animatedBall.coordinates.x = ballX(col).toFloat()

            val startY = bL.ballY(column, fromRow).toFloat()
            val topY = bL.liftedBallY(column).toFloat() //BALL_RADIUS.toFloat()

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
                myBallAnimators.remove(column, fromRow)
                viewTubes?.get(column)?.cells?.get(fromRow)?.foreground = false
            }
            myBallAnimators.endRemoveAddStart(animator, column, fromRow)
        }
    }

    /**
     * eigene Methode
     */
    private fun animateDropBall(column: Int, toRow: Int) {
        Log.i(TAG, "animateDropBall(col=$column, toRow=$toRow)")
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(column)?.cells?.get(toRow)
        if (animatedBall != null) {
            //animatedBall.color = color
            //animatedBall.coordinates.x = ballX(col).toFloat()
            val topY = bL.liftedBallY(column).toFloat() //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(column, toRow).toFloat()
            val bounceY1 = stopY - BOUNCE1
            val bounceY2 = stopY - BOUNCE2
            val bounceY3 = stopY - BOUNCE2

            val durations = arrayOf(
                ANIMATION_ADDITIONAL_DURATION + (stopY - topY) / ANIMATION_SPEED, // down
                BOUNCE_DURATION1, // bounce up
                BOUNCE_DURATION1, // bounce down
                BOUNCE_DURATION2, // bounce up again
                BOUNCE_DURATION2, // bounce down again
                BOUNCE_DURATION3,
                BOUNCE_DURATION3
            )
            val wholeDuration = durations.sum()
            /*
            val fraction = arrayOf(
                time[0] / wholeTime,  //down
                (time[0] + time[1]) / wholeTime, // bounce up
                (time[0] + time[1] + time[2]) / wholeTime, // bounce down
                (time[0] + time[1] + time[2] + time[3]) / wholeTime, // bounce up again
            )
             */
            val fractions = durationsToFractions(durations, wholeDuration)

            val keyframesY = arrayOf(
                Keyframe.ofFloat(0f, topY),
                Keyframe.ofFloat(fractions[0], stopY),
                Keyframe.ofFloat(fractions[1], bounceY1),
                Keyframe.ofFloat(fractions[2], stopY),
                Keyframe.ofFloat(fractions[3], bounceY2),
                Keyframe.ofFloat(fractions[4], stopY),
                Keyframe.ofFloat(fractions[5], bounceY3),
                Keyframe.ofFloat(1f, stopY)
            )

            val holderY = PropertyValuesHolder.ofKeyframe("y", *keyframesY)

            val animator = ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderY)
            animator.duration = wholeDuration.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(column, toRow)
                viewTubes?.get(column)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemoveAddStart(animator, column, toRow)

            playBounceSoundAfter(durations[0])
        }
    }

    /**
     * eigene Methode
     *
     */
    private fun animateHoleBall(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        Log.i(
            TAG,
            "animateHoleBall(fromCol=$fromColumn, toCol=$toColumn, fromRow=$fromRow, toRow=$toRow)"
        )
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(toColumn)?.cells?.get(toRow)

        if (animatedBall != null) {
            val startX = bL.ballX(fromColumn)
            val stopX = bL.ballX(toColumn)
            val topY1 = bL.liftedBallY(fromColumn) //BALL_RADIUS.toFloat()
            val topY2 = bL.liftedBallY(toColumn) //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(toColumn, toRow)
            val bounceY1 = stopY - BOUNCE1
            val bounceY2 = stopY - BOUNCE2
            val bounceY3 = stopY - BOUNCE3

            val durations = arrayOf(
                ANIMATION_ADDITIONAL_DURATION / 2 + diagonalDistance(
                    startX,
                    stopX,
                    topY1,
                    topY2
                ) / ANIMATION_SPEED,
                ANIMATION_ADDITIONAL_DURATION / 2 + (stopY - topY2) / ANIMATION_SPEED,
                BOUNCE_DURATION1, // bounce up
                BOUNCE_DURATION1, // bounce down
                BOUNCE_DURATION2, // bounce up again
                BOUNCE_DURATION2, // bounce down again
                BOUNCE_DURATION3,
                BOUNCE_DURATION3
            )
            val wholeDuration = durations.sum()
            val fractions = durationsToFractions(durations, wholeDuration)

            val keyframesX = arrayOf(
                Keyframe.ofFloat(0f, startX.toFloat()),
                Keyframe.ofFloat(fractions[0], stopX.toFloat()),
                Keyframe.ofFloat(1f, stopX.toFloat())
            )

            val keyframesY = arrayOf(
                Keyframe.ofFloat(0f, topY1.toFloat()),
                Keyframe.ofFloat(fractions[0], topY2.toFloat()),
                Keyframe.ofFloat(fractions[1], stopY.toFloat()),
                Keyframe.ofFloat(fractions[2], bounceY1.toFloat()),
                Keyframe.ofFloat(fractions[3], stopY.toFloat()),
                Keyframe.ofFloat(fractions[4], bounceY2.toFloat()),
                Keyframe.ofFloat(fractions[5], stopY.toFloat()),
                Keyframe.ofFloat(fractions[6], bounceY3.toFloat()),
                Keyframe.ofFloat(1f, stopY.toFloat())
            )

            val holderX = PropertyValuesHolder.ofKeyframe("x", *keyframesX)
            val holderY = PropertyValuesHolder.ofKeyframe("y", *keyframesY)

            val animator =
                ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeDuration.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(toColumn, toRow)
                viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemove(fromColumn, fromRow)
            viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = true
            myBallAnimators.endRemoveAddStart(animator, toColumn, toRow)

            playBounceSoundAfter(durations[0] + durations[1])
        }
    }

    /**
     * eigene Methode
     */
    private fun animateLiftAndHoleBall(
        fromColumn: Int,
        toColumn: Int,
        fromRow: Int,
        toRow: Int
    ) {
        Log.i(
            TAG,
            "animateLiftAndHoleBall(fromCol=$fromColumn, toCol=$toColumn, fromRow=$fromRow, toRow=$toRow)"
        )
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(toColumn)?.cells?.get(toRow)

        if (animatedBall != null) {
            // animatedBall.color = color

            val startX = bL.ballX(fromColumn)
            val stopX = bL.ballX(toColumn)
            val startY = bL.ballY(fromColumn, fromRow)
            //val topY = BALL_RADIUS
            val topY1 = bL.liftedBallY(fromColumn) //BALL_RADIUS.toFloat()
            val topY2 = bL.liftedBallY(toColumn) //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(toColumn, toRow)
            val bounceY1 = stopY - BOUNCE1
            val bounceY2 = stopY - BOUNCE2
            val bounceY3 = stopY - BOUNCE3

            val durations = arrayOf(
                ANIMATION_ADDITIONAL_DURATION / 3 + (startY - topY1) / ANIMATION_SPEED, // upwards
                ANIMATION_ADDITIONAL_DURATION / 3 + diagonalDistance(
                    startX,
                    stopX,
                    topY1,
                    topY2
                ) / ANIMATION_SPEED,
                ANIMATION_ADDITIONAL_DURATION / 3 + (stopY - topY2) / ANIMATION_SPEED, // downwards
                BOUNCE_DURATION1, // bounce up
                BOUNCE_DURATION1, // bounce down
                BOUNCE_DURATION2, // bounce up again
                BOUNCE_DURATION2, // bounce down again
                BOUNCE_DURATION3,
                BOUNCE_DURATION3
            )
            val wholeDuration = durations.sum()
            val fractions = durationsToFractions(durations, wholeDuration)

            val keyframesX = arrayOf(
                Keyframe.ofFloat(0f, startX.toFloat()),
                Keyframe.ofFloat(fractions[0], startX.toFloat()),
                Keyframe.ofFloat(fractions[1], stopX.toFloat()),
                Keyframe.ofFloat(1f, stopX.toFloat())
            )

            val keyframesY = arrayOf(
                Keyframe.ofFloat(0f, startY.toFloat()),
                Keyframe.ofFloat(fractions[0], topY1.toFloat()), // hoch
                Keyframe.ofFloat(fractions[1], topY2.toFloat()), // diagonal
                Keyframe.ofFloat(fractions[2], stopY.toFloat()), // runter
                Keyframe.ofFloat(fractions[3], bounceY1.toFloat()),
                Keyframe.ofFloat(fractions[4], stopY.toFloat()),
                Keyframe.ofFloat(fractions[5], bounceY2.toFloat()),
                Keyframe.ofFloat(fractions[6], stopY.toFloat()),
                Keyframe.ofFloat(fractions[7], bounceY3.toFloat()),
                Keyframe.ofFloat(1f, stopY.toFloat())
            )

            val holderX = PropertyValuesHolder.ofKeyframe("x", *keyframesX)
            val holderY = PropertyValuesHolder.ofKeyframe("y", *keyframesY)

            val animator =
                ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeDuration.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(toColumn, toRow)
                viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemove(fromColumn, fromRow)
            viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = true
            myBallAnimators.endRemoveAddStart(animator, toColumn, toRow)

            playBounceSoundAfter(durations[0] + durations[1] + durations[2])
        }
    }

    private fun animateHoleBallTubeSolved(
        fromColumn: Int,
        toColumn: Int,
        fromRow: Int,
        toRow: Int
    ) {
        Log.i(
            TAG,
            "tubeSolved(fromCol=$fromColumn, toCol=$toColumn, fromRow=$fromRow, toRow=$toRow)"
        )
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(toColumn)?.cells?.get(toRow)

        if (animatedBall != null) {
            val startX = bL.ballX(fromColumn)
            val stopX = bL.ballX(toColumn)
            val topY1 = bL.liftedBallY(fromColumn) //BALL_RADIUS.toFloat()
            val topY2 = bL.liftedBallY(toColumn) //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(toColumn, toRow)
            val bounceY1 = stopY - BOUNCE1
            val bounceY2 = stopY - BOUNCE2
            val bounceY3 = stopY - BOUNCE3

            val durations = arrayOf(
                ANIMATION_ADDITIONAL_DURATION / 2 + diagonalDistance(
                    startX,
                    stopX,
                    topY1,
                    topY2
                ) / ANIMATION_SPEED,
                ANIMATION_ADDITIONAL_DURATION / 2 + (stopY - topY2) / ANIMATION_SPEED,
                BOUNCE_DURATION1, // bounce up
                BOUNCE_DURATION1, // bounce down
                BOUNCE_DURATION2, // bounce up again
                BOUNCE_DURATION2, // bounce down again
                BOUNCE_DURATION3,
                BOUNCE_DURATION3
            )
            for (d in durations.indices) {
                Log.d(TAG, "durations[$d]: ${durations[d]}")
            }

            val wholeDuration = durations.sum()
            val fractions = durationsToFractions(durations, wholeDuration)

            val keyframesX = arrayOf(
                Keyframe.ofFloat(0f, startX.toFloat()),
                Keyframe.ofFloat(fractions[0], stopX.toFloat()),
                Keyframe.ofFloat(1f, stopX.toFloat())
            )

            val keyframesY = arrayOf(
                Keyframe.ofFloat(0f, topY1.toFloat()),
                Keyframe.ofFloat(fractions[0], topY2.toFloat()),
                Keyframe.ofFloat(fractions[1], stopY.toFloat()),
                Keyframe.ofFloat(fractions[2], bounceY1.toFloat()),
                Keyframe.ofFloat(fractions[3], stopY.toFloat()),
                Keyframe.ofFloat(fractions[4], bounceY2.toFloat()),
                Keyframe.ofFloat(fractions[5], stopY.toFloat()),
                Keyframe.ofFloat(fractions[6], bounceY3.toFloat()),
                Keyframe.ofFloat(1f, stopY.toFloat())
            )

            val holderX = PropertyValuesHolder.ofKeyframe("x", *keyframesX)
            val holderY = PropertyValuesHolder.ofKeyframe("y", *keyframesY)

            val animator =
                ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeDuration.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(toColumn, toRow)
                viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemove(fromColumn, fromRow)
            viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = true
            myBallAnimators.endRemoveAddStart(animator, toColumn, toRow)

            playBounceSoundAfter(durations[0] + durations[1])

            hurrayLaOla(toRow, toColumn, durations[0] + durations[1])
        }
    }

    /**
     * Ball hoch, seitlich oder diagonal, runter und La Ola
     */
    private fun animateLiftAndHoleBallTubeSolved(
        fromColumn: Int,
        toColumn: Int,
        fromRow: Int,
        toRow: Int
    ) {
        Log.i(
            TAG,
            "animateLiftAndHoleBallTubeSolved(fromCol=$fromColumn, toCol=$toColumn, fromRow=$fromRow, toRow=$toRow)"
        )
        val bL = boardLayout ?: return
        val animatedBall = viewTubes?.get(toColumn)?.cells?.get(toRow)

        if (animatedBall != null) {
            val startX = bL.ballX(fromColumn)
            val stopX = bL.ballX(toColumn)
            val startY = bL.ballY(fromColumn, fromRow)
            val topY1 = bL.liftedBallY(fromColumn) //BALL_RADIUS.toFloat()
            val topY2 = bL.liftedBallY(toColumn) //BALL_RADIUS.toFloat()
            val stopY = bL.ballY(toColumn, toRow)
            val bounceY1 = stopY - BOUNCE1
            val bounceY2 = stopY - BOUNCE2
            val bounceY3 = stopY - BOUNCE3

            val durations = arrayOf(
                ANIMATION_ADDITIONAL_DURATION / 3 + (startY - topY1) / ANIMATION_SPEED, // upwards
                ANIMATION_ADDITIONAL_DURATION / 3 + diagonalDistance(
                    startX,
                    stopX,
                    topY1,
                    topY2
                ) / ANIMATION_SPEED,
                ANIMATION_ADDITIONAL_DURATION / 3 + (stopY - topY2) / ANIMATION_SPEED, // downwards
                BOUNCE_DURATION1, // bounce up
                BOUNCE_DURATION1, // bounce down
                BOUNCE_DURATION2, // bounce up again
                BOUNCE_DURATION2, // bounce down again
                BOUNCE_DURATION3,
                BOUNCE_DURATION3
            )
            val wholeDuration = durations.sum()
            val fractions = durationsToFractions(durations, wholeDuration)

            val keyframesX = arrayOf(
                Keyframe.ofFloat(0f, startX.toFloat()),
                Keyframe.ofFloat(fractions[0], startX.toFloat()),
                Keyframe.ofFloat(fractions[1], stopX.toFloat()),
                Keyframe.ofFloat(1f, stopX.toFloat())
            )

            val keyframesY = arrayOf(
                Keyframe.ofFloat(0f, startY.toFloat()),
                Keyframe.ofFloat(fractions[0], topY1.toFloat()), // hoch
                Keyframe.ofFloat(fractions[1], topY2.toFloat()), // diagonal
                Keyframe.ofFloat(fractions[2], stopY.toFloat()), // runter
                Keyframe.ofFloat(fractions[3], bounceY1.toFloat()),
                Keyframe.ofFloat(fractions[4], stopY.toFloat()),
                Keyframe.ofFloat(fractions[5], bounceY2.toFloat()),
                Keyframe.ofFloat(fractions[6], stopY.toFloat()),
                Keyframe.ofFloat(fractions[7], bounceY3.toFloat()),
                Keyframe.ofFloat(1f, stopY.toFloat())
            )

            val holderX = PropertyValuesHolder.ofKeyframe("x", *keyframesX)
            val holderY = PropertyValuesHolder.ofKeyframe("y", *keyframesY)

            val animator =
                ObjectAnimator.ofPropertyValuesHolder(animatedBall.coordinates, holderX, holderY)
            animator.duration = wholeDuration.toLong()
            animator.repeatMode = ValueAnimator.RESTART //is default
            animator.repeatCount = 0
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {
                invalidate()
            }
            animator.doOnEnd {
                myBallAnimators.remove(toColumn, toRow)
                viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = false
            }
            myBallAnimators.endRemove(fromColumn, fromRow)
            viewTubes?.get(toColumn)?.cells?.get(toRow)?.foreground = true
            myBallAnimators.endRemoveAddStart(animator, toColumn, toRow)

            playBounceSoundAfter(durations[0] + durations[1] + durations[2])

            hurrayLaOla(toRow, toColumn, durations[0] + durations[1] + durations[2])
        }
    }

    /**
     * La Ola Wave
     * @param belowRow all Balls below this row are animated
     * @param column Balls in this column are animated
     * @param startTime animation starts after this time
     */
    private fun hurrayLaOla(belowRow: Int, column: Int, startTime: Float) {
        val bL = boardLayout ?: return

        for (row in belowRow - 1 downTo 0) {
            val i = belowRow - row
            val bouncingBall = viewTubes?.get(column)?.cells?.get(row)
            if (bouncingBall != null) {
                val bStartStopY = bL.ballY(column, row)
                val bBounceY1 = bStartStopY - BOUNCE1
                val bBounceY2 = bStartStopY - BOUNCE2
                val bBounceY3 = bStartStopY - BOUNCE3

                val bDurations = arrayOf(
                    startTime + BOUNCE_DURATION1 * 2 * i, // do nothing
                    BOUNCE_DURATION1, // bounce up
                    BOUNCE_DURATION1, // bounce down
                    BOUNCE_DURATION2, // bounce up again
                    BOUNCE_DURATION2, // bounce down again
                    BOUNCE_DURATION3,
                    BOUNCE_DURATION3
                )
                for (d in bDurations.indices) {
                    Log.d(TAG, "row=$row, i=$i: bDurations[$d]: ${bDurations[d]}")
                }

                val bWholeDuration = bDurations.sum()
                val bFractions = durationsToFractions(bDurations, bWholeDuration)

                val bKeyframesY = arrayOf(
                    Keyframe.ofFloat(0f, bStartStopY.toFloat()),
                    Keyframe.ofFloat(bFractions[0], bStartStopY.toFloat()),
                    Keyframe.ofFloat(bFractions[1], bBounceY1.toFloat()),
                    Keyframe.ofFloat(bFractions[2], bStartStopY.toFloat()),
                    Keyframe.ofFloat(bFractions[3], bBounceY2.toFloat()),
                    Keyframe.ofFloat(bFractions[4], bStartStopY.toFloat()),
                    Keyframe.ofFloat(bFractions[5], bBounceY3.toFloat()),
                    Keyframe.ofFloat(1f, bStartStopY.toFloat())
                )


                val bHolderY = PropertyValuesHolder.ofKeyframe("y", *bKeyframesY)
                val bAnimator =
                    ObjectAnimator.ofPropertyValuesHolder(bouncingBall.coordinates, bHolderY)
                bAnimator.duration = bWholeDuration.toLong()
                bAnimator.repeatMode = ValueAnimator.RESTART //is default
                bAnimator.repeatCount = 0
                bAnimator.interpolator = LinearInterpolator()
                bAnimator.addUpdateListener {
                    invalidate()
                }
                bAnimator.doOnEnd {
                    myBallAnimators.remove(column, row)
                }
                myBallAnimators.endRemoveAddStart(bAnimator, column, row)

                playBounceSoundAfter(startTime + BOUNCE_DURATION1 * 2 * i)
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
         * Speed factor
         * 1.0f for normal speed
         * 0.1f for testing in slow motion
         */
        const val SPEED_FACTOR = 1.0f
        //const val SPEED_FACTOR = 0.04f

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
         * Höhe des ersten Dotzens eines Balles beim fallen lassen/einlochen
         */
        private const val BOUNCE1 = 16

        /**
         * Höhe des zweiten Dotzens eines Balles beim fallen lassen/einlochen
         */
        private const val BOUNCE2 = 8

        /**
         * Höhe des dritten Dotzens eines Balles beim fallen lassen/einlochen
         */
        private const val BOUNCE3 = 4

        /**
         * Speed of animations in virtual pixels per millisecond
         * (except while bouncing)
         */
        private const val ANIMATION_SPEED = 1f * SPEED_FACTOR

        /**
         * Time for bouncing up or down (slow)
         */
        private const val BOUNCE_DURATION1 = 135f / SPEED_FACTOR

        /**
         * Time for bouncing up or down (slow)
         */
        private const val BOUNCE_DURATION2 = 90f / SPEED_FACTOR

        /**
         * Time for bouncing up or down (slow)
         */
        private const val BOUNCE_DURATION3 = 60f / SPEED_FACTOR

        /**
         * Minimum duration of animations, if the distance would be zero
         * (without bouncing)
         */
        private const val ANIMATION_ADDITIONAL_DURATION = 200f / SPEED_FACTOR

        /*
         * Farbe der Röhren

        private val TUBE_PAINT = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0xe6, 0xe6, 0xe6) // light gray
            style = Paint.Style.FILL
        }
         */

        /*
        private val linePaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.FILL
            strokeWidth = 10f
        }
         */

        private const val PARALLEL_SOUNDS = 4

    }
}