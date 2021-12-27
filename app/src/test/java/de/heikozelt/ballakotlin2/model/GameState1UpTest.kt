package de.heikozelt.ballakotlin2.model

import org.junit.Assert.*
import org.junit.Test

class GameState1UpTest {

    @Test
    fun constructor_simple() {
        val gs = GameState(3, 2, 3)
        val gs1Up = GameState1Up(gs)
        assertEquals(gs, gs1Up.getGameState())
        assertFalse(gs1Up.isUp())
    }

    @Test
    fun registerGameStateListener() {
        val gs = GameState(3, 2, 3)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        assertFalse(gs1Up.isUp())
        assertTrue(listener.observationsLog.isEmpty())
    }

    @Test
    fun click_on_tube_0_once() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        gs1Up.tubeClicked(0)

        assertTrue(gs1Up.isUp())
        assertEquals(0, gs1Up.getUpCol())
        assertEquals(1, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog.get(0))
    }

    @Test
    fun click_on_tube_0_twice() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        gs1Up.tubeClicked(0)
        gs1Up.tubeClicked(0)

        assertFalse(gs1Up.isUp())
        assertEquals(2, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog.get(0))
        assertEquals("dropBall(col=0, row=0, color=1)", listener.observationsLog.get(1))
    }

    @Test
    fun click_on_tube_0_and_1() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        gs1Up.tubeClicked(0)
        gs1Up.tubeClicked(1)

        assertFalse(gs1Up.isUp())
        assertEquals(3, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog.get(0))
        assertEquals("holeBall(fromCol=0, toCol=1, toRow=0, color=1)", listener.observationsLog.get(1))
        assertEquals("enableResetAndUndo(enabled=true)", listener.observationsLog.get(2))
    }

    @Test
    fun click_on_undo_button() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        gs1Up.tubeClicked(0)
        gs1Up.tubeClicked(1)
        gs1Up.actionUndo()

        assertFalse(gs1Up.isUp())
        assertEquals(5, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog.get(0))
        assertEquals("holeBall(fromCol=0, toCol=1, toRow=0, color=1)", listener.observationsLog.get(1))
        assertEquals("enableResetAndUndo(enabled=true)", listener.observationsLog.get(2))
        assertEquals("enableResetAndUndo(enabled=false)", listener.observationsLog.get(3))
        assertEquals("liftAndHoleBall(fromCol=1, toCol=0, fromRow=0, toRow=0, color=1)", listener.observationsLog.get(4))
    }

    @Test
    fun click_on_new_game_button() {
        val gs = GameState(3, 2, 3)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        gs1Up.actionNewGame()

        assertFalse(gs1Up.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("enableResetAndUndo(enabled=false)", listener.observationsLog.get(0))
        assertEquals("enableCheat(enabled=true)", listener.observationsLog.get(1))
        assertEquals("redraw()", listener.observationsLog.get(2))
        assertEquals("newGameToast()", listener.observationsLog.get(3))
    }

    @Test
    fun click_on_reset_button() {
        val gs = GameState(3, 2, 3)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        gs1Up.actionResetGame()

        assertFalse(gs1Up.isUp())
        assertEquals(3, listener.observationsLog.size)
        assertEquals("enableResetAndUndo(enabled=false)", listener.observationsLog.get(0))
        assertEquals("enableCheat(enabled=true)", listener.observationsLog.get(1))
        assertEquals("redraw()", listener.observationsLog.get(2))
    }

    /**
     * <pre>
     * 1 _ _      1 2 _
     * 1 2 _  =>  1 2 _
     * 1 2 2      1 2 _
     * </pre>
     */
    @Test
    fun puzzle_solved() {
        val gs = GameState(2, 1, 3)

        gs.tubes[0].addBall(1)
        gs.tubes[0].addBall(1)
        gs.tubes[0].addBall(1)
        gs.tubes[1].addBall(2)
        gs.tubes[1].addBall(2)
        gs.tubes[2].addBall(2)
        val gs1Up = GameState1Up(gs)
        val listener = GameStateListenerMock()
        gs1Up.registerGameStateListener(listener)

        // Zug von Spalte 2 in Spalte 1
        gs1Up.tubeClicked(2)
        gs1Up.tubeClicked(1)

        assertFalse(gs1Up.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("liftBall(col=2, row=0, color=2)", listener.observationsLog.get(0))
        assertEquals("holeBall(fromCol=2, toCol=1, toRow=2, color=2)", listener.observationsLog.get(1))
        assertEquals("enableResetAndUndo(enabled=true)", listener.observationsLog.get(2)) // weil erster Zug ueberhaupt
        assertEquals("puzzleSolved()", listener.observationsLog.get(3))
    }
}