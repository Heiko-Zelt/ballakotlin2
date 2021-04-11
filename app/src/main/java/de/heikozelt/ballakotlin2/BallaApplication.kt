package de.heikozelt.ballakotlin2

import android.app.Application
import de.heikozelt.ballakotlin2.model.GameState

class BallaApplication: Application() {

    var numberOfColors = 7
    var numberOfExtraTubes = 2
    var tubeHeight = 4
    var gameState = GameState(numberOfColors, numberOfExtraTubes, tubeHeight)
    var originalGameState = gameState

    init {
        gameState.newGame()
        originalGameState = gameState.clone()
    }
}