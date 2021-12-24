package de.heikozelt.ballakotlin2.model

/**
 * Methods of this Interface are called, whenever game state (GameState1Up) changes and something in the view(s) should be changed.
 */

interface GameStateListenerInterface {

    /**
     * called after new game, reset or cheat button clicked
     */
    fun redraw()

    /**
     * Ein Ball senkrecht anheben.
     */
    fun liftBall(col: Int, row: Int, color: Int)

    /**
     * Ein Ball wieder senkrecht fallen lassen.
     * Der Spieler hat es sich anders überlegt.
     */
    fun dropBall(col: Int, row: Int, color: Int)

    /**
     * Ball einlochen (erst waagrecht und dann senkrecht runter)
     */
    fun holeBall(fromCol: Int, toCol: Int, toRow: Int, color: Int)

    /**
     * Bei Klick auf Undo-Button.
     * Ball hochheben, wagrecht und einlochen.
     */
    fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int)

    /**
     * called after first move
     */
    fun enableUndo()

    /**
     * called, when undo-log is empty
     */
    fun disableUndo()

    /**
     * called, when the puzzle is solved, game sucessfully finished.
     */
    fun puzzleSolved()

    /**
     * called, after new game was started
     */
    fun newGameToast()
}