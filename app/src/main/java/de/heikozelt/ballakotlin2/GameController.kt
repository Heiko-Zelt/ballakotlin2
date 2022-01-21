package de.heikozelt.ballakotlin2

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.GameObserverInterface
import de.heikozelt.ballakotlin2.model.Move
import de.heikozelt.ballakotlin2.model.SearchResult
import kotlinx.coroutines.*
//import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlin.coroutines.CoroutineContext


/**
 * Represents the state of the game.
 * One ball may be lifted.
 * User inputs are clicks on tubes and buttons.
 * Primary constructor creates GameController without GameState.
 * GameState is loaded later (asynchronously)
 */
class GameController {
    /**
     * Initial gibt es keinen Spielstatus.
     * Er muss erst vom persistenten Speicher geladen werden.
     */
    private var gameState: GameState? = null

    /**
     * Die GUI lauscht auf Änderungen des Spielstatus
     */
    private var gameObserver: GameObserverInterface? = null

    /**
     * Der Coroutinen-Context, in dem Benachrichtigung von Hintergrund-Jobs übergeben werden.
     * In Android Main / UI thread, bei Tests auch andere
     */
    private var feedbackContext: CoroutineContext? = null

    /**
     * true, if one ball ist lifted.
     */
    private var up = false

    /**
     * Only relevant, if one ball is lifted.
     * Contains column number of lifted Ball.
     */
    private var upColumn = 0

    /**
     * remember initial game state for reset.
     * (and number of tubes for new game)
     */
    private var initialGameState: GameState? = null

    /**
     * Should computer help solve the puzzle
     */
    private var isComputerSupportive = true

    /**
     * computer found solution, next possible move
     * todo: There may be more than one move possible
     * todo: show arrows
     */
    //var helpMove: Move? = null
    var searchResult: SearchResult? = null

    /**
     * Hintergrund-Coroutine, um nächsten Zug zu berechnen
     */
    private var job: Job? = null

    init {
        Log.i(TAG, "init")
    }

    /*
    fun isHelpAvailable(): Boolean {
        return (searchResult?.move != null)
    }
    */

    /**
     * nach jeder Spielstandsänderung (Spielzug, Undo, etc...)
     * Computer-Unterstützung anfordern.
     * (auch wenn Computer-Unterstützung deaktiviert war und jetzt aktiviert wird.)
     */
    fun findHelp() {
        Log.d(TAG, "findHelp()")
        searchResult = null
        job?.cancel()
        if(isComputerSupportive) {
            gameObserver?.updateStatusSearching()
            gameState?.let { gs ->
                job = GlobalScope.launch(Default) {
                    Log.d(TAG, "coroutine launched with GlobalScope in Default Dispatcher")
                    searchResult = gs.findSolution()
                    Log.d(TAG, "findSolution finished")
                    //todo: in der view unterscheiden zwischen keine Lösung gefunden und keine Lösung möglich
                    /*
                    if (searchResult.status == SearchResult.STATUS_FOUND_SOLUTION) {
                        helpMove = searchResult.move
                    }
                    */

                    feedbackContext?.let {
                        withContext(it) {
                            Log.d(TAG, "withContext(Main)")
                            //if (isHelpAvailable()) {
                            if(isComputerSupportive) {
                                Log.d(TAG, "update Status with SearchResult")
                                //gameObserver?.enableHelp(true)
                                searchResult?.let {
                                    gameObserver?.updateStatusSearchResult(it)
                                }
                            }
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
    }

    fun getNumberOfColors(): Int {
        return initialGameState?.numberOfColors ?: 0
    }

    /**
     * wenn der cheat button angeklickt wird,
     * dann erhoeht sich numberOfExtraTubes.
     * Aber im initialen Spielstatus ist die Original-Zahl erhalten.
     */
    fun getInitialExtraTubes(): Int {
        Log.i(TAG, "numberOfExtraTubes: ${initialGameState?.numberOfExtraTubes}")
        return initialGameState?.numberOfExtraTubes ?: 0
    }

    fun getTubeHeight(): Int {
        return initialGameState?.tubeHeight ?: 0
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

    fun getInitialGameState(): GameState? {
        return initialGameState
    }

    fun setInitialGameState(_initialGs: GameState) {
        initialGameState = _initialGs
    }

    fun getGameState(): GameState? {
        return gameState
    }

    /**
     * Setter injection
     */
    fun setGameState(_gs: GameState) {
        Log.d(TAG, "setGameState()")
        _gs.dump()
        initialGameState = _gs.cloneWithoutLog()
        gameState = _gs
        gameObserver?.redraw()
        gameObserver?.newGameToast()
    }

    fun isUp(): Boolean {
        return up
    }

    fun getUpCol(): Int {
        return upColumn
    }

    /**
     * Dimensionen wie letztes Spiel
     */
    fun actionNewGame() {
        initialGameState?.let { igs ->
            actionNewGame(
                igs.numberOfColors,
                igs.numberOfExtraTubes,
                igs.tubeHeight
            )
        }
    }

    /**
     * neue Dimensionen
     */
    fun actionNewGame(numberOfColors: Int, numberOfExtraTubes: Int, tubeHeight: Int) {
        if (gameState == null) {
            gameState = GameState()
        }
        gameState?.let { gs ->
            gs.resize(numberOfColors, numberOfExtraTubes, tubeHeight)
            gs.newGame()
            initialGameState = gs.cloneWithoutLog()
            up = false
        }
        gameObserver?.apply {
            enableUndoAndReset(false)
            enableCheat(true)
            redraw()
            newGameToast()
        }
        findHelp()
    }

    /**
     * Klick auf Reset-Button, zurück an Spielanfang.
     */
    fun actionResetGame() {
        initialGameState?.let { igs ->
            gameState = igs.cloneWithoutLog()
            up = false
            gameObserver?.enableUndoAndReset(false)
            gameObserver?.enableCheat(true)
            gameObserver?.redraw()
            findHelp()
        }
    }

    /**
     * Klick auf Undo-Button
     */
    fun actionUndo() {
        gameState?.let { gs ->
            if (up) {
                gameObserver?.dropBall(
                    upColumn,
                    gs.tubes[upColumn].fillLevel - 1
                )
                up = false
            } else if (gs.moveLog.isNotEmpty()) {
                val move = gs.undoLastMove()
                if (gs.moveLog.isEmpty()) {
                    gameObserver?.enableUndoAndReset(false)
                }
                gameObserver?.liftAndHoleBall(
                    move.from,
                    move.to,
                    gs.tubes[move.from].fillLevel,
                    gs.tubes[move.to].fillLevel - 1
                )
                findHelp()
            }
        }
    }

    fun isCheatAllowed(): Boolean {
        val gs = gameState
        val igs = initialGameState
        return if (gs != null && igs != null) {
            gs.numberOfTubes < igs.numberOfTubes + ALLOWED_CHEATS
        } else {
            false
        }
    }

    /**
     * Cheat Button klicked.
     * One additional column.
     */
    fun actionCheat() {
        Log.i(TAG, "actionCheat()")
        gameState?.let { gs ->
            initialGameState?.let { igs ->
                if (isCheatAllowed()) {
                    gs.cheat()
                    gameObserver?.redraw()
                }
                if (gs.numberOfTubes == igs.numberOfTubes + ALLOWED_CHEATS) {
                    gameObserver?.enableCheat(false)
                }
                findHelp()
            }
        }
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
        gameState?.let { gs ->
            searchResult?.move?.let {move ->
                gs.dump()
                Log.d(TAG, "move=$move")
                // todo: java.lang.IndexOutOfBoundsException: tube is already full
                gs.moveBallAndLog(move)
                val fromRow = gs.tubes[move.from].fillLevel
                val toRow = gs.tubes[move.to].fillLevel - 1
                if (gs.tubes[move.to].isSolved()) {
                    if (up) {
                        if (upColumn == move.from) {
                            Log.d(TAG, "richtiger Ball oben")
                            gameObserver?.holeBallTubeSolved(
                                move.from,
                                move.to,
                                fromRow,
                                toRow
                            )
                        } else {
                            Log.d(TAG, "falscher Ball oben")
                            val downToRow = gs.tubes[upColumn].fillLevel - 1
                            gameObserver?.dropBall(upColumn, downToRow)
                            gameObserver?.liftAndHoleBallTubeSolved(
                                move.from,
                                move.to,
                                fromRow,
                                toRow
                            )
                        }
                    } else {
                        Log.d(TAG, "kein Ball oben")
                        gameObserver?.liftAndHoleBallTubeSolved(
                            move.from,
                            move.to,
                            fromRow,
                            toRow
                        )
                    }
                    gameObserver?.enableUndoAndReset(true)
                } else { // keine Röhre gelöst. Also keine La Ola.
                    if (up) {
                        if (upColumn == move.from) {
                            Log.d(TAG, "richtiger Ball oben")
                            gameObserver?.holeBall(move.from, move.to, fromRow, toRow)
                        } else {
                            Log.d(TAG, "falscher Ball oben")
                            val downToRow = gs.tubes[upColumn].fillLevel - 1
                            gameObserver?.dropBall(upColumn, downToRow)
                            gameObserver?.liftAndHoleBall(move.from, move.to, fromRow, toRow)
                        }
                    } else {
                        Log.d(TAG, "kein Ball oben")
                        gameObserver?.liftAndHoleBall(move.from, move.to, fromRow, toRow)
                    }
                    gameObserver?.enableUndoAndReset(true)
                }
                if (gs.isSolved()) {
                    gameObserver?.puzzleSolved()
                    gameObserver?.enableUndoAndReset(false)
                }
                up = false
                findHelp()
            }
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
    fun tubeClicked(column: Int) {
        gameState?.let { gs ->
            if (up) { // zweiter Klick
                when {
                    column == upColumn -> { // Sonderfall B
                        val row = gs.tubes[column].fillLevel - 1
                        gameObserver?.dropBall(column, row)
                        up = false
                    }
                    gs.isMoveAllowed(upColumn, column) -> { // Normalfall A
                        val move = Move(upColumn, column)
                        gs.moveBallAndLog(move)
                        val fromRow = gs.tubes[upColumn].fillLevel
                        val toRow = gs.tubes[column].fillLevel - 1
                        when {
                            gs.isSolved() -> {
                                gameObserver?.holeBallTubeSolved(upColumn, column, fromRow, toRow)
                                gameObserver?.puzzleSolved()
                            }
                            gs.tubes[column].isSolved() -> {
                                gameObserver?.holeBallTubeSolved(upColumn, column, fromRow, toRow)
                                gameObserver?.enableUndoAndReset(true)
                            }
                            else -> {
                                gameObserver?.holeBall(upColumn, column, fromRow, toRow)
                                gameObserver?.enableUndoAndReset(true)
                            }
                        }
                        up = false
                        findHelp()
                    }
                    else -> { // Sonderfall C
                        val downToRow = gs.tubes[upColumn].fillLevel - 1
                        val upFromRow = gs.tubes[column].fillLevel - 1
                        gameObserver?.dropBall(upColumn, downToRow)
                        gameObserver?.liftBall(column, upFromRow)
                        upColumn = column
                    }
                }
            } else { // erster Klick
                if (!gs.tubes[column].isEmpty()) {
                    up = true
                    upColumn = column
                    val fromRow = gs.tubes[column].fillLevel - 1
                    gameObserver?.liftBall(column, fromRow)
                }
            }
        }
    }

    /**
     * Should computer try to help with solving the puzzle?
     */
    fun enableComputerSupport(enabled: Boolean) {
        // war ausgeschaltet und wird jetzt eingeschaltet
        if(!isComputerSupportive && enabled) {
            isComputerSupportive = true
            findHelp()
        }
        if(!enabled) {
            job?.cancel()
            isComputerSupportive = false
            searchResult = null
            gameObserver?.updateStatusHelpOff()
        }
    }

    companion object {
        private const val TAG = "balla.GameController"
        private const val ALLOWED_CHEATS = 3
    }
}