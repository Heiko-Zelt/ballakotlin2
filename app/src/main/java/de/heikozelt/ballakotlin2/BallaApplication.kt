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
    var gameController = GameController()

    init {
        Log.i(TAG, "primary constructor")
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()

        if (loadSettings()) {
            Log.d(TAG, "Spielstatus erfolgreich geladen. :-)")
        } else {
            Log.d(
                TAG,
                "Spielstatus konnte nicht geladen werden. Neues Spiel mit Standard-Dimensionen."
            )
            defaultSettings()
        }
    }

    /**
     * speichert:
     *  - Spiel-Status
     *  - initialen Spiel-Status (für Reset)
     *  - Log mit Spielzügen (Undo Log)
     */
    fun saveSettings() {
        Log.d(TAG, "saveSettings()")
        val gs = gameController.getGameState()
        val initialGs = gameController.getInitialGameState()
        if (gs != null && initialGs != null) {
            val prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
            prefs.edit().apply {
                clear()
                putString(PREF_INITIAL_GAMESTATE, initialGs.toAscii())
                putString(PREF_GAMESTATE, gs.toAscii())
                putString(PREF_MOVE_LOG, gs.moveLog.toAscii())
                commit()
            }
            Log.d(TAG, "Settings erfolgreich gespeichert.")
        }
    }

    /**
     * läd:
     *  - Spiel-Status
     *  - initialen Spiel-Status (für Reset)
     *  - Log mit Spielzügen (Undo Log)
     */
    fun loadSettings(): Boolean {
        Log.d(TAG, "lodSettings()")
        val prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val boardAscii = prefs.getString(PREF_GAMESTATE, null)
        val moveLogAscii = prefs.getString(PREF_MOVE_LOG, null)
        Log.d(TAG, "boardAscii=$boardAscii")
        Log.d(TAG, "moveLogAscii=$moveLogAscii")
        if (boardAscii == null || moveLogAscii == null) {
            return false
        } else {
            val gs = GameState()
            val initialGs = GameState()
            try {
                initialGs.fromAscii(boardAscii)
                gs.fromAscii(boardAscii)
                gs.moveLog.fromAscii(moveLogAscii)
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "fromAscii: $e")
                return false
            }
            Log.d(TAG, "after fromAscii(): gameController.setGameState(gs)")
            gameController.setInitialGameState(initialGs)
            gameController.setGameState(gs)
            return true
        }
    }

    fun defaultSettings() {
        val gs = GameState().apply {
            resize(NUMBER_OF_COLORS, NUMBER_OF_EXTRA_TUBES, TUBE_HEIGHT)
            newGame()
        }
        Log.d(TAG, "after newGame(): gameController.setGameState(gs)")
        gameController.setGameState(gs)
    }

    companion object {
        private const val TAG = "balla.BallaApplication"
        private const val SHARED_PREFS_NAME = "settings"
        private const val PREF_INITIAL_GAMESTATE = "initial_gamestate"
        private const val PREF_GAMESTATE = "gamestate"
        private const val PREF_MOVE_LOG = "move_log"
        const val NUMBER_OF_COLORS = 7
        const val NUMBER_OF_EXTRA_TUBES = 2
        const val TUBE_HEIGHT = 4
    }
}