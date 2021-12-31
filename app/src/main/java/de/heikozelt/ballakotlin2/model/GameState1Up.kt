package de.heikozelt.ballakotlin2.model

import android.util.Log

/**
 * Represents the state of the game.
 * One ball may be lifted.
 * Main input is clicks on tubes.
 */
class GameState1Up(private var gameState: GameState) {

    private var gameStateListener: GameStateListenerInterface? = null

    /**
     * true, if one ball ist lifted.
     */
    private var up = false

    /**
     * Only relevant, if one ball is lifted.
     * Column number of lifted Ball.
     */
    private var upCol = 0

    /**
     * remember initial game state for reset
     */
    private var initialGameState = gameState.clone()

    init {
        Log.i(TAG, "init")
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
    fun registerGameStateListener(gsl: GameStateListenerInterface) {
        gameStateListener = gsl
    }

    /**
     * muss aufgerufen werden, wenn eine Activity beendet wird
     */
    fun unregisterGameStateListener() {
        gameStateListener = null
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
        actionNewGame(initialGameState.numberOfColors, initialGameState.numberOfExtraTubes, initialGameState.tubeHeight)
    }

    /**
     * neue Dimensionen
     */
    fun actionNewGame(numberOfColors: Int, numberOfExtraTubes: Int, tubeHeight: Int) {
        gameState = GameState(numberOfColors, numberOfExtraTubes, tubeHeight)
        gameState.newGame()
        initialGameState = gameState.clone()
        up = false
        gameStateListener?.enableUndoAndReset(false)
        gameStateListener?.enableCheat(true)
        gameStateListener?.redraw()
        gameStateListener?.newGameToast()
    }

    /**
     * Klick auf Reset-Button, zurück an Spielanfang.
     */
    fun actionResetGame() {
        gameState = initialGameState.clone()
        up = false
        gameStateListener?.enableUndoAndReset(false)
        gameStateListener?.enableCheat(true)
        gameStateListener?.redraw()
        // Todo: gameStateListener no invisibleBall
    }

    /**
     * Klick auf Undo-Button
     */
    fun actionUndo() {
        if(up) {
            gameStateListener?.dropBall(upCol, gameState.tubes[upCol].fillLevel -1, gameState.tubes[upCol].colorOfTopmostBall())
            up = false
        } else if(gameState.moveLog.isNotEmpty()) {
            val move = gameState.undoLastMove()
            if (gameState.moveLog.isEmpty()) {
                gameStateListener?.enableUndoAndReset(false)
            }
            gameStateListener?.liftAndHoleBall(move.from, move.to, gameState.tubes[move.from].fillLevel, gameState.tubes[move.to].fillLevel - 1, gameState.tubes[move.to].colorOfTopmostBall())
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
        Log.i(TAG, "actionCheat")
        if(isCheatAllowed()) {
            gameState.cheat()
            gameStateListener?.redraw()
        }
        if(gameState.numberOfTubes == initialGameState.numberOfTubes + ALLOWED_CHEATS) {
            gameStateListener?.enableCheat(false)
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
     */
    fun tubeClicked(col: Int) {
        if (up) { // zweiter Klick
            if(col == upCol) { // Sonderfall B
                val row = gameState.tubes[col].fillLevel - 1
                val color = gameState.tubes[col].colorOfTopmostBall()
                gameStateListener?.dropBall(col, row, color)
                up = false
            } else if (gameState.isMoveAllowed(upCol, col)) { // Normalfall A
                val move = Move(upCol, col)
                gameState.moveBallAndLog(move)
                val toRow = gameState.tubes[col].fillLevel - 1
                val color = gameState.tubes[col].colorOfTopmostBall()
                gameStateListener?.holeBall(upCol, col, toRow, color)
                gameStateListener?.enableUndoAndReset(true)
                up = false
                if (gameState.isSolved()) {
                    gameStateListener?.puzzleSolved()
                }
            } else { // Sonderfall C
                val fromRow = gameState.tubes[col].fillLevel - 1
                val color = gameState.tubes[col].colorOfTopmostBall()
                //gameStateListener.dropBall(upCol, toRow, color)
                gameStateListener?.liftBall(col, fromRow, color)
                upCol = col
            }
        } else { // erster Klick
            if (!gameState.tubes[col].isEmpty()) {
                up = true
                upCol = col
                val fromRow = gameState.tubes[col].fillLevel - 1
                val color = gameState.tubes[col].colorOfTopmostBall()
                gameStateListener?.liftBall(col, fromRow, color)
            }
        }
    }

    companion object {
        private const val TAG = "balla.GameState1Up"
        private const val ALLOWED_CHEATS = 3
    }
}