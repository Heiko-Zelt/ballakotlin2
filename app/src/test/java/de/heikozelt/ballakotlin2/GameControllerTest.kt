package de.heikozelt.ballakotlin2

import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.view.GameObserverMock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

//import org.junit.Assert.*
//import org.junit.Test

class GameControllerTest {

    @Test
    fun constructor_simple() {
        val gs = GameState(3, 2, 3)
        val controller = GameController(gs)
        assertEquals(gs, controller.getGameState())
        assertFalse(controller.isUp())
    }

    @Test
    fun registerGameStateListener() {
        val gs = GameState(3, 2, 3)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        assertFalse(controller.isUp())
        assertTrue(listener.observationsLog.isEmpty())
    }

    @Test
    fun click_on_tube_0_once() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        controller.tubeClicked(0)

        assertTrue(controller.isUp())
        assertEquals(0, controller.getUpCol())
        assertEquals(1, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog[0])
    }

    @Test
    fun click_on_tube_0_twice() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        controller.tubeClicked(0)
        controller.tubeClicked(0)

        assertFalse(controller.isUp())
        assertEquals(2, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog[0])
        assertEquals("dropBall(col=0, row=0, color=1)", listener.observationsLog[1])
    }

    /**
     * <pre>
     *              1
     * _ _ _ _ _    _ _ _ _ _    _ _ _ _ _
     * _ _ _ _ _ => _ _ _ _ _ => _ _ _ _ _
     * 1 _ _ _ _    _ _ _ _ _    _ 1 _ _ _
     * ^              ^
     * click          click
     * </pre>
     */
    @Test
    fun click_on_tube_0_and_1() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        controller.tubeClicked(0)
        controller.tubeClicked(1)

        listener.dump()
        assertFalse(controller.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog[0])
        assertEquals("holeBall(fromCol=0, toCol=1, toRow=0, color=1)", listener.observationsLog[1])
        assertEquals("enableResetAndUndo(enabled=true)", listener.observationsLog[2])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[3])
    }

    /*
    * <pre>
    *              1
    * _ _ _ _ _    _ _ _ _ _    _ _ _ _ _    _ _ _ _ _
    * _ _ _ _ _ => _ _ _ _ _ => _ _ _ _ _ => _ _ _ _ _
    * 1 _ _ _ _    _ _ _ _ _    _ 1 _ _ _    1 _ _ _ _
    * ^              ^
    * click          click      undo
    * </pre>
     */
    @Test
    fun click_on_undo_button() {
        val gs = GameState(3, 2, 3)
        gs.tubes[0].addBall(1)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        controller.tubeClicked(0)
        controller.tubeClicked(1)
        controller.actionUndo()

        listener.dump()
        assertFalse(controller.isUp())
        assertEquals(7, listener.observationsLog.size)
        // click
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog[0])
        // click
        assertEquals("holeBall(fromCol=0, toCol=1, toRow=0, color=1)", listener.observationsLog[1])
        assertEquals("enableResetAndUndo(enabled=true)", listener.observationsLog[2])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[3])
        // undo
        assertEquals("enableResetAndUndo(enabled=false)", listener.observationsLog[4])
        assertEquals("liftAndHoleBall(fromCol=1, toCol=0, fromRow=0, toRow=0, color=1)", listener.observationsLog[5])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[6])
    }

    @Test
    fun click_on_new_game_button() {
        val gs = GameState(3, 2, 3)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        controller.actionNewGame()

        listener.dump()
        assertFalse(controller.isUp())
        assertEquals(5, listener.observationsLog.size)
        assertEquals("enableResetAndUndo(enabled=false)", listener.observationsLog[0])
        assertEquals("enableCheat(enabled=true)", listener.observationsLog[1])
        assertEquals("redraw()", listener.observationsLog[2])
        assertEquals("newGameToast()", listener.observationsLog[3])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[4])
    }

    @Test
    fun click_on_reset_button() {
        val gs = GameState(3, 2, 3)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        controller.actionResetGame()

        listener.dump()
        assertFalse(controller.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("enableResetAndUndo(enabled=false)", listener.observationsLog[0])
        assertEquals("enableCheat(enabled=true)", listener.observationsLog[1])
        assertEquals("redraw()", listener.observationsLog[2])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[3])
    }


    /**
     * <pre>
     *                2
     * _ _ _ _    _ _ _ _     _ 2 _ _
     * 1 2 _ _ => 1 2 _ _ =>  1 2 _ _
     * 1 2 2 1    1 2 2 1     1 2 _ 1
     *     ^        ^
     *   click    click
     * </pre>
     */
    @Test
    fun tube_solved() {
        val gs = GameState(2, 2, 3)

        gs.tubes[0].addBall(1)
        gs.tubes[0].addBall(1)
        gs.tubes[0].addBall(1)
        gs.tubes[1].addBall(2)
        gs.tubes[1].addBall(2)
        gs.tubes[2].addBall(2)
        gs.tubes[3].addBall(1)
        val controller = GameController(gs)
        val listener = GameObserverMock()
        controller.registerGameStateListener(listener)

        // Zug von Spalte 2 in Spalte 1
        controller.tubeClicked(2)
        controller.tubeClicked(1)

        listener.dump()
        assertFalse(controller.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("liftBall(col=2, row=0, color=2)", listener.observationsLog[0])
        //assertEquals("holeBall(fromCol=2, toCol=1, toRow=2, color=2)", listener.observationsLog[1])
        assertEquals("holeBallTubeSolved(fromCol=2, toCol=1, toRow=2, color=2)", listener.observationsLog[1])
        assertEquals("enableResetAndUndo(enabled=true)", listener.observationsLog[2]) // weil erster Zug ueberhaupt
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[3])
    }

    /**
     * <pre>
     *                2
     * 1 _ _      1 _ _    1 2 _
     * 1 2 _  =>  1 2 _ => 1 2 _
     * 1 2 2      1 2 _    1 2 _
     *     ^        ^
     * click      click
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
        val gs1Up = GameController(gs)
        val listener = GameObserverMock()
        gs1Up.registerGameStateListener(listener)

        // Zug von Spalte 2 in Spalte 1
        gs1Up.tubeClicked(2)
        gs1Up.tubeClicked(1)

        listener.dump()
        assertFalse(gs1Up.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("liftBall(col=2, row=0, color=2)", listener.observationsLog[0])
        assertEquals("holeBallTubeSolved(fromCol=2, toCol=1, toRow=2, color=2)", listener.observationsLog[1])
        assertEquals("puzzleSolved()", listener.observationsLog[2])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[3])
    }
}