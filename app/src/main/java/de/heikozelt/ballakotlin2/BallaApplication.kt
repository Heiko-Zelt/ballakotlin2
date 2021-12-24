package de.heikozelt.ballakotlin2


import android.app.Application
import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.GameState1Up

class BallaApplication : Application() {

    /**
     * Der Spielstand muss erhalten bleiben, auch wenn der Bildschirm gedreht wird.
     * Activity und View sind eher temporäre Objekte.
     * Deswegen ist Spielstand von Application statt Activity oder View referenziert.
     */
    private var gameState1Up: GameState1Up? = null

    init {
        Log.i(TAG, "init")
        val gs = GameState(NUMBER_OF_COLORS, NUMBER_OF_EXTRA_TUBES, TUBE_HEIGHT)
        gs.newGame()
        gameState1Up = GameState1Up(gs)
    }

    fun getGameState1Up(): GameState1Up? {
        return gameState1Up
    }

    companion object {
        private const val TAG = "balla.BallaApplication"
        const val NUMBER_OF_COLORS = 7
        const val NUMBER_OF_EXTRA_TUBES = 2
        const val TUBE_HEIGHT = 4

    }
}