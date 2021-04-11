package de.heikozelt.ballakotlin2

import android.app.Application
import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.Move

class BallaApplication: Application() {

    var numberOfColors = 7
    var numberOfExtraTubes = 2
    var tubeHeight = 4
    var gameState = GameState(numberOfColors, numberOfExtraTubes, tubeHeight)
    var originalGameState = gameState
    var donorIndex: Int? = null

    init {
        gameState.newGame()
        originalGameState = gameState.clone()
    }
}