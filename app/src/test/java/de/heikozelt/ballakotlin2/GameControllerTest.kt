package de.heikozelt.ballakotlin2

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.Move
import de.heikozelt.ballakotlin2.view.GameObserverMock
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

//import org.junit.Assert.*
//import org.junit.Test

class GameControllerTest {

    /*
    //private val dispatcher = newSingleThreadContext("UI thread")
    @ExperimentalCoroutinesApi
    private val testDispatcher = StandardTestDispatcher()
    // private val dispatcher = TestCoroutineDispatcher() deprecated

    @BeforeEach
    fun setip() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cancelChildren()
        //dispatcher.close()
        //dispatcher.cleanupTestCoroutines()
    }

     */

    @Test
    fun constructor_simple() {
        val gs = GameState()
        gs.resize(3,2,3)
        val controller = GameController()
        controller.setGameState(gs)
        assertEquals(gs, controller.getGameState())
        assertFalse(controller.isUp())
    }

    @Test
    fun registerGameStateListener() {
        val gs = GameState()
        gs.resize(3,2,3)
        val controller = GameController()
        controller.setGameState(gs)

        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

        assertFalse(controller.isUp())
        assertTrue(listener.observationsLog.isEmpty())
    }

    @Test
    fun click_on_tube_0_once() {
        val gs = GameState().apply {
            resize(3,2,3)
            tubes[0].addBall(1)
        }
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

        controller.tubeClicked(0)

        assertTrue(controller.isUp())
        assertEquals(0, controller.getUpCol())
        assertEquals(1, listener.observationsLog.size)
        assertEquals("liftBall(col=0, row=0, color=1)", listener.observationsLog[0])
    }

    @Test
    fun click_on_tube_0_twice() {
        val gs = GameState().apply {
            resize(3,2,3)
            tubes[0].addBall(1)
        }
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

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
        val gs = GameState().apply {
            resize(3,2,3)
            tubes[0].addBall(1)
        }
        val listener = GameObserverMock()
        val controller = GameController()
        controller.setGameState(gs)
        controller.registerGameObserver(listener)
        controller.tubeClicked(0)
        assertTrue(controller.isUp())
        assertEquals(0, controller.getUpCol())

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
        val gs = GameState().apply {
            resize(3,2,3)
            tubes[0].addBall(1)
        }
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

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
        assertEquals(
            "liftAndHoleBall(fromCol=1, toCol=0, fromRow=0, toRow=0, color=1)",
            listener.observationsLog[5]
        )
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[6])
    }

    @Test
    fun click_on_new_game_button() {
        val gs = GameState()
        gs.resize(3,2,3)
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

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
        val gs = GameState()
        gs.resize(3,2,3)
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

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
     * todo: Parser für ASCII-Grafik zum Initialisieren des GameState ist viel anschaulicher
     */
    @Test
    fun tube_solved() {
        val gs = GameState().apply {
            resize(3,2,3)
            tubes[0].apply {
                addBall(1); addBall(1); addBall(1)
            }
            tubes[1].apply {
                addBall(2); addBall(2)
            }
            tubes[2].addBall(2)
            tubes[3].addBall(1)
        }
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

        // Zug von Spalte 2 in Spalte 1
        controller.tubeClicked(2)
        controller.tubeClicked(1)

        listener.dump()
        assertFalse(controller.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("liftBall(col=2, row=0, color=2)", listener.observationsLog[0])
        //assertEquals("holeBall(fromCol=2, toCol=1, toRow=2, color=2)", listener.observationsLog[1])
        assertEquals(
            "holeBallTubeSolved(fromCol=2, toCol=1, toRow=2, color=2)",
            listener.observationsLog[1]
        )
        assertEquals(
            "enableResetAndUndo(enabled=true)",
            listener.observationsLog[2]
        ) // weil erster Zug ueberhaupt
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
        val gs = GameState().apply {
            resize(2,1,3)
            tubes[0].apply {
                addBall(1); addBall(1); addBall(1)
            }
            tubes[1].apply {
                addBall(2); addBall(2)
            }
            tubes[2].addBall(2)
        }
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

        // Zug von Spalte 2 in Spalte 1
        controller.tubeClicked(2)
        controller.tubeClicked(1)

        listener.dump()
        assertFalse(controller.isUp())
        assertEquals(4, listener.observationsLog.size)
        assertEquals("liftBall(col=2, row=0, color=2)", listener.observationsLog[0])
        assertEquals(
            "holeBallTubeSolved(fromCol=2, toCol=1, toRow=2, color=2)",
            listener.observationsLog[1]
        )
        assertEquals("puzzleSolved()", listener.observationsLog[2])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[3])
    }

    /**
     * <pre>
     * 1 _ _      1 2 _
     * 1 2 _   => 1 2 _
     * 1 2 2      1 2 _
     * findHelp() actionHelp()
     * </pre>
     */
    @ExperimentalCoroutinesApi
    @Test
    fun findHelp_easy() {
        val gs = GameState().apply {
            resize(2,1,3)
            tubes[0].apply {
                addBall(1); addBall(1); addBall(1)
            }
            tubes[1].apply {
                addBall(2); addBall(2)
            }
            tubes[2].addBall(2)
        }
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

        runBlocking {
            controller.registerFeedbackContext(currentCoroutineContext())
            controller.findHelp()
            // eine Sekunde sollte normalerweise ausreichen, um eine Lösung zu finden
            delay(1000L)
            Log.d(TAG, "in runTest: eine Sekunde später")
        }
        Log.d(TAG, "nach runTest: eine Sekunde später")
        listener.dump()
        assertEquals(2, listener.observationsLog.size)
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[0])
        assertEquals("enableHelp(enabled=true)", listener.observationsLog[1])
        assertEquals(Move(2, 1), controller.helpMove)

        controller.actionHelp()
        listener.dump()
        assertEquals(7, listener.observationsLog.size)
        assertEquals(
            "liftAndHoleBallTubeSolved(fromCol=2, toCol=1, toRow=2, color=2)",
            listener.observationsLog[2]
        )
        assertEquals("enableResetAndUndo(enabled=true)", listener.observationsLog[3])
        assertEquals("puzzleSolved()", listener.observationsLog[4])
        assertEquals("enableResetAndUndo(enabled=false)", listener.observationsLog[5])
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[6])
        assertEquals(null, controller.helpMove)
    }

    /**
     * _ _ _ _ _ _
     * 1 2 _ _ _ _
     * 1 2 _ _ _ _
     * findHelp() actionHelp()
     * </pre>
     */
    @ExperimentalCoroutinesApi
    @Test
    fun findHelp_unsolvable() {
        val gs = GameState().apply {
            resize(2, 4, 3)
            tubes[0].apply {
                addBall(1); addBall(1)
            }
            tubes[1].apply {
                addBall(2); addBall(2)
            }
        }
        val controller = GameController()
        controller.setGameState(gs)
        val listener = GameObserverMock()
        controller.registerGameObserver(listener)

        runBlocking {
            controller.registerFeedbackContext(currentCoroutineContext())
            controller.findHelp()
            // eine Sekunde sollte normalerweise ausreichen, um eine Lösung zu finden
            delay(500L)
        }
        listener.dump()
        assertEquals(1, listener.observationsLog.size)
        assertEquals("enableHelp(enabled=false)", listener.observationsLog[0])
        assertEquals(null, controller.helpMove)

        controller.actionHelp()
        listener.dump()
        assertEquals(1, listener.observationsLog.size)
    }

    companion object {
        private const val TAG = "balla.GameControllerTest"
    }
}