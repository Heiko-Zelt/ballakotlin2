package de.heikozelt.ballakotlin2

import android.util.Log
import androidx.core.app.RemoteInput
import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.GameObserverInterface
import de.heikozelt.ballakotlin2.model.Move
import kotlinx.coroutines.*
//import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.CoroutineContext


/**
 * Represents the state of the game.
 * One ball may be lifted.
 * Main input is clicks on tubes.
 */
class GameController(private var gameState: GameState) {

    private var gameObserver: GameObserverInterface? = null

    private var feedbackContext: CoroutineContext? = null

    /**
     * true, if one ball ist lifted.
     */
    private var up = false

    /**
     * Only relevant, if one ball is lifted.
     * Contains column number of lifted Ball.
     */
    private var upCol = 0

    /**
     * remember initial game state for reset.
     */
    private var initialGameState = gameState.cloneWithoutLog()

    /**
     * computer found solution, next possible move
     * todo: There may be more than one move possible
     * todo: show arrows
     */
    var helpMove: Move? = null

    /**
     * Hintergrund-Coroutine, um naechten Zug zu berechnen
     */
    private var job: Job? = null

    init {
        Log.i(TAG, "init")
    }

    fun isHelpAvailable(): Boolean {
        return (helpMove != null)
    }

    fun findHelp() {
        Log.d(TAG, "findHelp()")
        helpMove = null
        gameObserver?.enableHelp(false)

        job?.cancel()
        job = GlobalScope.launch(Default) {
            Log.d(TAG, "coroutine launched with GlobalScope in Default Dispatcher")
            val moves = gameState.findSolution()
            Log.d(TAG, "findSolution finished")
            helpMove = if (moves == null) {
                Log.d(TAG, "keine (einfache) Lösung gefunden")
                null
            } else if (moves.isEmpty()) {
                Log.d(TAG, "kein Zug mehr möglich, trauriger Smiley :-(")
                null
            } else {
                Log.d(TAG, "found solution")
                moves[0]
            }

            feedbackContext?.let {
                withContext(it) {
                    Log.d(TAG, "withContext(Main)")
                    if (isHelpAvailable()) {
                        Log.d(TAG, "help is available")
                        gameObserver?.enableHelp(true)
                    }
                }
            }
            // todo: Event-Handler zuerst (und ein einziges Mal) registrieren, dann Job starten, aber wie?
            // todo: in welchem Thread läuft invokeOnCompletion?
            // switch to Main/GUI thread
            // doesn't work in unit tests
            //withContext(Main) {
            //            delay(100L)
            //job?.invokeOnCompletion {
        }

    }

    fun getNumberOfColors(): Int {
        return initialGameState.numberOfColors
    }

    /**
     * wenn der cheat button angeklickt wird,
     * dann erhoeht sich numberOfExtraTubes.
     * Aber im initialen Spielstatus ist die Original-Zahl erhalten.
     */
    fun getInitialExtraTubes(): Int {
        Log.i(TAG, "numberOfExtraTubes: ${initialGameState.numberOfExtraTubes}")
        return initialGameState.numberOfExtraTubes
    }

    fun getTubeHeight(): Int {
        return initialGameState.tubeHeight
    }

    /**
     * muss aufgerufen werden, wenn eine Activity zum Leben kommt
     */
    fun registerGameObserver(gsl: GameObserverInterface) {
        gameObserver = gsl
    }

    /**
     * wegen Rückmeldung von findHelp() wenn Hintergrundjob fertig ist.
     */
    fun registerFeedbackContext(fd: CoroutineContext) {
        feedbackContext = fd
    }

    /**
     * muss aufgerufen werden, wenn eine Activity beendet wird
     */
    fun unregisterGameStateListener() {
        gameObserver = null
    }

    fun getGameState(): GameState {
        return gameState
    }

    fun isUp(): Boolean {
        return up
    }

    fun getUpCol(): Int {
        return upCol
    }

    /**
     * Dimensionen wie letztes Spiel
     */
    fun actionNewGame() {
        actionNewGame(
            initialGameState.numberOfColors,
            initialGameState.numberOfExtraTubes,
            initialGameState.tubeHeight
        )
    }

    /**
     * neue Dimensionen
     */
    fun actionNewGame(numberOfColors: Int, numberOfExtraTubes: Int, tubeHeight: Int) {
        gameState = GameState(numberOfColors, numberOfExtraTubes, tubeHeight)
        gameState.newGame()
        initialGameState = gameState.cloneWithoutLog()
        up = false
        helpMove = null
        gameObserver?.enableUndoAndReset(false)
        gameObserver?.enableCheat(true)
        gameObserver?.redraw()
        gameObserver?.newGameToast()
        findHelp()
    }

    /**
     * Klick auf Reset-Button, zurück an Spielanfang.
     */
    fun actionResetGame() {
        gameState = initialGameState.cloneWithoutLog()
        up = false
        helpMove = null
        gameObserver?.enableUndoAndReset(false)
        gameObserver?.enableCheat(true)
        gameObserver?.redraw()
        findHelp()
    }

    /**
     * Klick auf Undo-Button
     */
    fun actionUndo() {
        if (up) {
            gameObserver?.dropBall(
                upCol,
                gameState.tubes[upCol].fillLevel - 1,
                gameState.tubes[upCol].colorOfTopmostBall()
            )
            up = false
        } else if (gameState.moveLog.isNotEmpty()) {
            val move = gameState.undoLastMove()
            if (gameState.moveLog.isEmpty()) {
                gameObserver?.enableUndoAndReset(false)
            }
            gameObserver?.liftAndHoleBall(
                move.from,
                move.to,
                gameState.tubes[move.from].fillLevel,
                gameState.tubes[move.to].fillLevel - 1,
                gameState.tubes[move.to].colorOfTopmostBall()
            )
            helpMove = null
            findHelp()
        }
    }

    fun isCheatAllowed(): Boolean {
        return gameState.numberOfTubes < initialGameState.numberOfTubes + ALLOWED_CHEATS
    }

    /**
     * Cheat Button klicked.
     * One additional column.
     */
    fun actionCheat() {
        Log.i(TAG, "actionCheat()")
        if (isCheatAllowed()) {
            gameState.cheat()
            gameObserver?.redraw()
        }
        if (gameState.numberOfTubes == initialGameState.numberOfTubes + ALLOWED_CHEATS) {
            gameObserver?.enableCheat(false)
        }
        findHelp()
    }

    /**
     * Fälle:
     * 1. kein Ball ist oben
     * 2. richtiger Ball ist oben
     * 3. falscher Ball ist oben (2 Aninmationen)
     * a. Röhre gelöst (La Ola)
     * b. Röhre nicht gelöst
     */

    fun actionHelp() {
        Log.i(TAG, "actionHelp()")
        val move = helpMove
        if (move != null) {
            gameState.dump()
            Log.d(TAG, "move=$move")
            // todo: java.lang.IndexOutOfBoundsException: tube is already full
            gameState.moveBallAndLog(move)
            val fromRow = gameState.tubes[move.from].fillLevel
            val toRow = gameState.tubes[move.to].fillLevel - 1
            val color = gameState.tubes[move.to].colorOfTopmostBall()
            if (gameState.tubes[move.to].isSolved()) {
                if (up) {
                    if (upCol == move.from) {
                        Log.d(TAG, "richtiger Ball oben")
                        gameObserver?.holeBallTubeSolved(
                            move.from,
                            move.to,
                            fromRow,
                            toRow,
                            color
                        )
                    } else {
                        Log.d(TAG, "falscher Ball oben")
                        val downToRow = gameState.tubes[upCol].fillLevel - 1
                        val downColor = gameState.tubes[upCol].cells[downToRow]
                        gameObserver?.dropBall(upCol, downToRow, downColor)
                        gameObserver?.liftAndHoleBallTubeSolved(
                            move.from,
                            move.to,
                            fromRow,
                            toRow,
                            color
                        )
                    }
                } else {
                    Log.d(TAG, "kein Ball oben")
                    gameObserver?.liftAndHoleBallTubeSolved(
                        move.from,
                        move.to,
                        fromRow,
                        toRow,
                        color
                    )
                }
                gameObserver?.enableUndoAndReset(true)
            } else { // keine Röhre gelöst. Also keine La Ola.
                if (up) {
                    if (upCol == move.from) {
                        Log.d(TAG, "richtiger Ball oben")
                        gameObserver?.holeBall(move.from, move.to, fromRow, toRow, color)
                    } else {
                        Log.d(TAG, "falscher Ball oben")
                        val downToRow = gameState.tubes[upCol].fillLevel - 1
                        val downColor = gameState.tubes[upCol].cells[downToRow]
                        gameObserver?.dropBall(upCol, downToRow, downColor)
                        gameObserver?.liftAndHoleBall(move.from, move.to, fromRow, toRow, color)
                    }
                } else {
                    Log.d(TAG, "kein Ball oben")
                    gameObserver?.liftAndHoleBall(move.from, move.to, fromRow, toRow, color)
                }
                gameObserver?.enableUndoAndReset(true)
            }
            if (gameState.isSolved()) {
                gameObserver?.puzzleSolved()
                gameObserver?.enableUndoAndReset(false)
            }
            up = false
            findHelp()
        }
    }

    /**
     * Normalfall A:
     * Ein Spielzug besteht normalerweise aus 2 Klicks:
     * 1. Erster Klick auf Spalte hebt Ball an.
     * 2. Zweiter Klick (auf andere Spalte) locht Ball ein.
     *
     * Sonderfall B: Klick nochmal auf gleiche Spalte:
     * Ball senkt sich wieder.
     *
     * Sonderfall C: Klick auf andere Spalte mit falscher Farbe:
     * Alter Ball senkt sich wieder und es hebt sich der neue Ball.
     *
     * Sonderfall A.1: Röhre gelöst. Hurra!
     *
     * Sonderfall A.2: Spiel beendet. Hurra!
     */
    fun tubeClicked(col: Int) {
        if (up) { // zweiter Klick
            when {
                col == upCol -> { // Sonderfall B
                    val row = gameState.tubes[col].fillLevel - 1
                    val color = gameState.tubes[col].colorOfTopmostBall()
                    gameObserver?.dropBall(col, row, color)
                    up = false
                }
                gameState.isMoveAllowed(upCol, col) -> { // Normalfall A
                    val move = Move(upCol, col)
                    gameState.moveBallAndLog(move)
                    val fromRow = gameState.tubes[upCol].fillLevel
                    val toRow = gameState.tubes[col].fillLevel - 1
                    val color = gameState.tubes[col].colorOfTopmostBall()
                    if (gameState.isSolved()) {
                        gameObserver?.holeBallTubeSolved(upCol, col, fromRow, toRow, color)
                        gameObserver?.puzzleSolved()
                    } else if (gameState.tubes[col].isSolved()) {
                        gameObserver?.holeBallTubeSolved(upCol, col, fromRow, toRow, color)
                        gameObserver?.enableUndoAndReset(true)
                    } else {
                        gameObserver?.holeBall(upCol, col, fromRow, toRow, color)
                        gameObserver?.enableUndoAndReset(true)
                    }
                    up = false
                    findHelp()
                }
                else -> { // Sonderfall C
                    val downToRow = gameState.tubes[upCol].fillLevel - 1
                    val downColor = gameState.tubes[upCol].colorOfTopmostBall()

                    val upFromRow = gameState.tubes[col].fillLevel - 1
                    val upColor = gameState.tubes[col].colorOfTopmostBall()

                    gameObserver?.dropBall(upCol, downToRow, downColor)
                    gameObserver?.liftBall(col, upFromRow, upColor)
                    upCol = col
                }
            }
        } else { // erster Klick
            if (!gameState.tubes[col].isEmpty()) {
                up = true
                upCol = col
                val fromRow = gameState.tubes[col].fillLevel - 1
                val color = gameState.tubes[col].colorOfTopmostBall()
                gameObserver?.liftBall(col, fromRow, color)
            }
        }
    }

    companion object {
        private const val TAG = "balla.GameController"
        private const val ALLOWED_CHEATS = 3
    }
}