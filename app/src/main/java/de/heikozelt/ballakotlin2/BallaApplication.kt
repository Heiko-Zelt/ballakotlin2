package de.heikozelt.ballakotlin2


import android.app.Application
import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState

class BallaApplication : Application() {

    /**
     * Der Spielstand muss erhalten bleiben, auch wenn der Bildschirm gedreht wird.
     * Activity und View sind eher temporäre Objekte.
     * Deswegen ist Spielstand von Application statt Activity oder View referenziert.
     */

    /*
    private var gamei: GameController? = null
    var gameController: GameController?
        get() = gamei
        set(value) {
            gamei = value
        }
     */
    var gameController: GameController

    init {
        Log.i(TAG, "init")
        val gs = GameState(NUMBER_OF_COLORS, NUMBER_OF_EXTRA_TUBES, TUBE_HEIGHT)
        gs.newGame()
        gameController = GameController(gs)
    }


    companion object {
        private const val TAG = "balla.BallaApplication"
        const val NUMBER_OF_COLORS = 7
        const val NUMBER_OF_EXTRA_TUBES = 2
        const val TUBE_HEIGHT = 4

    }
}