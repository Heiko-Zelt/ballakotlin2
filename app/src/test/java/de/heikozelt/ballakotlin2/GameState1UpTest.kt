package de.heikozelt.ballakotlin2

import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.GameState1Up
import org.junit.Test
import org.junit.Assert.assertEquals

class GameState1UpTest {

    @Test
    fun constructor_simple() {
        val gs = GameState(3, 2, 3)
        val listener = GameStateListenerMock()
        val gs1Up = GameState1Up(gs, listener)
        assertEquals(gs, gs1Up.getGameState())
    }
}