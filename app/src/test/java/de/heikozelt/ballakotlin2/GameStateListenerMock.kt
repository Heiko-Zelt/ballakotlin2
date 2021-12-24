package de.heikozelt.ballakotlin2

import de.heikozelt.ballakotlin2.model.GameStateListenerInterface

class GameStateListenerMock: GameStateListenerInterface {
    override fun redraw() {
        TODO("Not yet implemented")
    }

    override fun liftBall(col: Int, row: Int, color: Int) {
        TODO("Not yet implemented")
    }

    override fun dropBall(col: Int, row: Int, color: Int) {
        TODO("Not yet implemented")
    }

    override fun holeBall(fromCol: Int, toCol: Int, toRow: Int, color: Int) {
        TODO("Not yet implemented")
    }

    override fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        TODO("Not yet implemented")
    }

    override fun enableUndo() {
        TODO("Not yet implemented")
    }

    override fun disableUndo() {
        TODO("Not yet implemented")
    }

    override fun puzzleSolved() {
        TODO("Not yet implemented")
    }
}