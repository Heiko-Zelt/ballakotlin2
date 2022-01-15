package de.heikozelt.ballakotlin2.model

/**
 * Methods of this Interface are called, whenever game state (GameState1Up) changes and something in the view(s) should be changed.
 */

interface GameObserverInterface {

    /**
     * called after new game, reset or cheat button clicked
     */
    fun redraw()

    /**
     * Ein Ball senkrecht anheben.
     */
    fun liftBall(column: Int, row: Int)

    /**
     * Ein Ball wieder senkrecht fallen lassen.
     * Der Spieler hat es sich anders überlegt.
     */
    fun dropBall(column: Int, row: Int)

    /**
     * Ball einlochen (erst waagrecht und dann senkrecht runter)
     */
    fun holeBall(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int)

    /**
     * Bei Klick auf Undo-Button.
     * Ball hochheben, wagrecht und einlochen.
     */
    fun liftAndHoleBall(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int)

    /**
     * called, when a tube is solved
     * and correct ball was up.
     * example:
     * <pre>
     *     2
     * 1 _ _    1 2 _
     * 1 2 _ => 1 2 _
     * 1 2 _    1 2 _
     * </pre>
     */
    fun holeBallTubeSolved(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int)

    /**
     * called when a tube is solved and
     * computer does help move and no or wrong ball is up.
     * example:
     * <pre>
     * 1 _ _    1 2 _
     * 1 2 _ => 1 2 _
     * 1 2 2    1 2 _
     * </pre>
     */
    fun liftAndHoleBallTubeSolved(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int)

    /**
     * method is called with parameter true after first move.
     * and with paramater false after last undo, when undo log is empty.
     */
    fun enableUndoAndReset(enabled: Boolean)

    /**
     * method is called with parameter false after user pressed 3 times the cheat button
     */
    fun enableCheat(enabled: Boolean)

    /**
     * method is called with parameter false when no more help is available.
     * method is called with parameter true if help is available (and wasn't before)
     */
    fun enableHelp(enabled: Boolean)

    /**
     * called, when the puzzle is solved, game sucessfully finished.
     */
    fun puzzleSolved()

    /**
     * called, after new game was started
     */
    fun newGameToast()
}