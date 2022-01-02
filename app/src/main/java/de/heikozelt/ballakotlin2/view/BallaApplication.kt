package de.heikozelt.ballakotlin2.view


import android.app.Application
import android.util.Log
import de.heikozelt.ballakotlin2.GameController
import de.heikozelt.ballakotlin2.model.GameState

class BallaApplication : Application() {

    /**
     * Der Spielstand muss erhalten bleiben, auch wenn der Bildschirm gedreht wird.
     * Activity und View sind eher temporäre Objekte.
     * Deswegen ist Spielstand von Application statt Activity oder View referenziert.
     */
    private var gameController: GameController? = null

    init {
        Log.i(TAG, "init")
        val gs = GameState(NUMBER_OF_COLORS, NUMBER_OF_EXTRA_TUBES, TUBE_HEIGHT)
        gs.newGame()
        gameController = GameController(gs)
    }

    fun getGameController(): GameController? {
        return gameController
    }

    companion object {
        private const val TAG = "balla.BallaApplication"
        const val NUMBER_OF_COLORS = 7
        const val NUMBER_OF_EXTRA_TUBES = 2
        const val TUBE_HEIGHT = 4

    }
}