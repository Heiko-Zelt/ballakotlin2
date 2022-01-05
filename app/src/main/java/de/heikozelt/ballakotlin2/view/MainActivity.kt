package de.heikozelt.ballakotlin2.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import de.heikozelt.ballakotlin2.BallaApplication
import de.heikozelt.ballakotlin2.GameController
import de.heikozelt.ballakotlin2.R
import de.heikozelt.ballakotlin2.model.GameObserverInterface


/**
 * Im Model-View-Controller-Entwurfsmuster gehoert eine Activity in den Bereich View
 * Beim Drehen des Bildschirms wird die Activity zerstört.
 * Es können also keine Daten dauerhaft in einer Activity gespeichert werden. :-(
 */
class MainActivity : AppCompatActivity(), GameObserverInterface {

    private var gameController: GameController? = null

    /**
     * welche Spalte wurde zuerst geklickt
     * -1 bedeutet keine
     */
    //var donorIndex: Int = -1

    /**
     * auf welcher Höhe lag der oberste Ball bevor er angehoben wurde.
     * (nur für den Fall, dass ein Zug abgebrochen wird und er wieder gesenkt wird.)
     * -1 bedeutet keine
     */
    //var donorRow: Int = -1

    /**
     * kleine Verkürzung des Funktionsaufrufs
     */
    private fun getMyDrawView(): MyDrawView? {
        return findViewById(R.id.my_draw_view)
    }


    private var settingsResult: ActivityResultLauncher<Intent>? = null

    /**
     * Methode von Activity geerbt
     * wird z.B. aufgerufen, wenn Bildschirm gedreht wird
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        var v = getMyDrawView()
        if (v == null) {
            Log.d(TAG, "before setContentView(): view not found ")
        } else {
            Log.d(TAG, "before setContentView(): view found ")
        }

        setContentView(R.layout.activity_main)
        /*
        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.onCreate() :-(")
            return
        }
         */
        v = getMyDrawView()
        if (v == null) {
            Log.d(TAG, "after setContentView(): view not found ")
        } else {
            Log.d(TAG, "after setContentView(): view found ")
        }

        // selber Referenz merken
        // Todo: Das ist keine Setter Injection!!!
        gameController = (application as BallaApplication).gameController

        Log.i(TAG, "injecting game state")
        v?.setGameController(gameController)
        gameController?.registerGameStateListener(this)

        // Wenn der Bildschirm gedreht wird, dann wird die Activity neu instanziiert.
        // Status-Informationen der View gehen verloren.
        val gs = gameController?.getGameState()
        if (gs != null) {
            // Wenn das Puzzle bereits gelöst war, dann verschwindet
            // das Glückwunsch-Popup und muss erneut aufpoppen.
            if (gs.isSolved()) {
                puzzleSolved()
            }

            // Undo-Button-Status wiederherstellen
            enableUndoAndReset(gs.moveLog.isNotEmpty())
        }
        val allowed = gameController?.isCheatAllowed()
        if (allowed != null) {
            enableCheat(allowed)
        }

        settingsResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            Log.d(TAG, "activity result code: ${it.resultCode}")
            if (it.resultCode == RESULT_OK) {
                Log.d(TAG, "RESULT_OK")
                val resultIntent = it.data
                val resultBundle = resultIntent?.extras
                if (resultBundle != null) {
                    val colors = resultBundle.getInt("number_of_colors")
                    val extra = resultBundle.getInt("extra_tubes")
                    val height = resultBundle.getInt("height")
                    (application as BallaApplication).gameController.actionNewGame(
                        colors,
                        extra,
                        height
                    )
                }
            } else if (it.resultCode == RESULT_CANCELED) {
                Log.d(TAG, "RESULT_CANCELED")
            } else {
                Log.e(TAG, "unknown activity result code")
            }
        }

        // Klick auf Burger Menu öffnet Settings-Activity
        findViewById<View?>(R.id.main_btn_burger_menu)?.setOnClickListener {
            Log.i(TAG, "user clicked on menu button")
            val launchIntent = Intent(this, SettingsActivity::class.java)
            val gC = gameController
            if (gC != null) {
                val launchBundle = Bundle()
                launchBundle.putInt("number_of_colors", gC.getNumberOfColors())
                launchBundle.putInt("extra_tubes", gC.getInitialExtraTubes())
                Log.d(TAG, "launchBundle.extra_tubes: ${gC.getInitialExtraTubes()}")
                launchBundle.putInt("height", gC.getTubeHeight())
                launchIntent.putExtras(launchBundle)
                // start settings activity
                settingsResult?.launch(launchIntent)
            }
        }

        findViewById<View?>(R.id.main_btn_new_game)?.setOnClickListener {
            Log.i(TAG, "user clicked on new game button")
            gameController?.actionNewGame()
        }

        findViewById<View?>(R.id.main_btn_reset_game)?.setOnClickListener {
            Log.i(TAG, "user clicked on reset game button")
            gameController?.actionResetGame()
        }

        findViewById<View?>(R.id.main_btn_undo)?.setOnClickListener {
            Log.i(TAG, "user clicked on undo button")
            gameController?.actionUndo()
        }

        findViewById<View?>(R.id.btn_plus_one)?.setOnClickListener {
            Log.i(TAG, "user clicked on cheat button")
            gameController?.actionCheat()
        }

        Log.i(TAG, "invalidating / redrawing view")
        v?.invalidate()
    }


    /**
     * Methode von Activity geerbt
     */
    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
    }

    /**
     * Methode von Activity geerbt
     */
    override fun onStop() {
        Log.d(TAG, "onStop()")
        super.onStop()
    }

    /**
     * Methode von Activity geerbt
     */
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        gameController?.unregisterGameStateListener()
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun tubeSolved(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        Log.i(
            TAG,
            "tubeSoled(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)"
        )
        val v = getMyDrawView()
        v?.tubeSolved(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun puzzleSolved() {
        Log.i(TAG, "puzzle solved")
        //setTimeout(function() {
        val alertBuilder = AlertDialog.Builder(this)
        with(alertBuilder) {
            setTitle(getString(R.string.alert_title_puzzle_solved))
            setMessage(getString(R.string.alert_text_puzzle_solved))
            setCancelable(false)
            setPositiveButton(getString(R.string.button_ok)) { _, _ ->
                gameController?.actionNewGame()
                /*
                val v = findViewById<MyDrawView?>(R.id.myDrawView)
                v?.resetGameView()
                */
            }
            show()
        }
    }

    override fun newGameToast() {
        Toast.makeText(applicationContext, getString(R.string.toast_new_game), Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     * called after new game, reset or cheat button clicked
     */
    override fun redraw() {
        Log.i(TAG, "redraw()")
        val v = getMyDrawView()
        // Todo: Animationen stoppen!

        // neu setzten der Dimensionen ist eigentlich nur noetig,
        // falls sich die Ausdehnung des Spielfeldes geaendert hat
        /*
        val gs = gameController?.getGameState()
        if(gs != null) {
        }
        */
        v?.selectBoardLayout()

        // Spielfeld in View einpassen
        v?.calculateTranslation()
        // Alle Animationen beenden und angehobenen Ball senken, falls nötig? Warum?
        //v?.flatten()
        v?.calculateBalls()
        // neu zeichnen
        v?.invalidate()
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun liftBall(col: Int, row: Int, color: Int) {
        val v = getMyDrawView()
        v?.liftBall(col, row, color)
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun dropBall(col: Int, row: Int, color: Int) {
        val v = getMyDrawView()
        v?.dropBall(col, row, color)
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun holeBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        val v = getMyDrawView()
        v?.holeBall(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        val v = getMyDrawView()
        v?.liftAndHoleBall(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun enableUndoAndReset(enabled: Boolean) {
        Log.i(TAG, "enableUndoAndReset(${enabled})")
        enableView(R.id.main_btn_undo, enabled)
        enableView(R.id.main_btn_reset_game, enabled)
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun enableCheat(enabled: Boolean) {
        Log.i(TAG, "enableCheat(${enabled})")
        enableView(R.id.btn_plus_one, enabled)
    }

    /**
     * only to shorten / reuse code
     */
    private fun enableView(viewId: Int, enabled: Boolean) {
        val v = findViewById<View?>(viewId)
        v?.isEnabled = enabled
        v?.alpha = if (enabled) {
            ALPHA_ENABLED
        } else {
            ALPHA_DISABLED
        }
    }

    /*
    fun tubeClicked(clickedCol: Int) {
        Log.i(TAG, "tubeClicked(clickedCol=${clickedCol})")
        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.tubeClicked() :-(")
            return
        }
        val v = findViewById<MyDrawView?>(R.id.myDrawView)
        if (donorIndex == -1) {
            Log.i(TAG, "no first ball selected till now")
            if (app.gameState.tubes[clickedCol].isEmpty()) {
                Log.i(TAG, "clicked on empty tube, can't remove ball")
            } else {
                Log.i(TAG, "chose first tube")
                //oder v?.liftBall(clickedCol, app.gameState.tubes[clickedCol].fillLevel - 1)
                liftBall(clickedCol)
            }
        } else {
            Log.i(TAG, "chose second tube")
            if (app.gameState.isMoveAllowed(donorIndex, clickedCol)) {
                Log.i(TAG, "move is allowed, normal move")
                //console.debug('move from ' + donorIndex + ' to ' + clickedCol);
                val move = Move(donorIndex, clickedCol)
                app.gameState.moveBallAndLog(move)
                v?.normalMove(move)
                if (app.gameState.moveLog.size != 0) {
                    Log.i(TAG, "enable undo button")
                    //val undoButton = document.getElementById('undoButton')
                    //undoButton.disabled = false
                }
                if (app.gameState.isSolved()) {
                    puzzleSolved()
                }
            } else {
                Log.i(TAG, "move not allowed, select new tube as first tube")
                // Ball wieder runter
                v?.dropBall(donorIndex)
                // dafür anderer Ball hoch
                v?.liftBall(clickedCol)
            }
        }
    }
    */

    /*
    fun newGame() {
        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.newGame() :-(")
            return
        }
        app.gameState = GameState(app.numberOfColors, app.numberOfExtraTubes, app.tubeHeight)
        app.gameState.newGame()
        app.originalGameState = app.gameState.clone()
    }
    */

    /*
    fun resetGame(view: View) {
        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.newGame() :-(")
            return
        }
        app.gameState = app.originalGameState.clone()
        val v = findViewById<MyDrawView?>(R.id.myDrawView)
        v?.resetGameView()
    }
     */

    companion object {
        private const val TAG = "balla.MainActivity"
        private const val ALPHA_ENABLED = 1.0f
        private const val ALPHA_DISABLED = 0.5f
    }
}