package de.heikozelt.ballakotlin2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import de.heikozelt.ballakotlin2.model.GameState1Up
import de.heikozelt.ballakotlin2.model.GameStateListenerInterface


/**
 * Im Model-View-Controller-Entwurfsmuster gehoert eine Activity in den Bereich View
 * Beim Drehen des Bildschirms wird die Activity zerstört.
 * Es können also keine Daten dauerhaft in einer Activity gespeichert werden. :-(
 */
class MainActivity : AppCompatActivity(), GameStateListenerInterface {

    private var gameState1Up: GameState1Up? = null

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
        return findViewById<MyDrawView?>(R.id.myDrawView)
    }

    /**
     * Methode von Activity geerbt
     * wird z.B. aufgerufen, wenn Bildschirm gedreht wird
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        val app = application as BallaApplication

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
        gameState1Up = app.getGameState1Up()
        Log.i(TAG, "injecting game state")
        v?.setGameState1Up(gameState1Up)
        gameState1Up?.registerGameStateListener(this)
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
        gameState1Up?.unregisterGameStateListener()
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
            setPositiveButton(getString(R.string.button_ok)) { dialogInterface, which ->
                gameState1Up?.actionNewGame()
                /*
                val v = findViewById<MyDrawView?>(R.id.myDrawView)
                v?.resetGameView()
                */
            }
            show()
        }
    }

    override fun newGameToast() {
        Toast.makeText(applicationContext, getString(R.string.toast_new_game), Toast.LENGTH_SHORT).show()
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     * called after new game, reset or cheat button clicked
     */
    override fun redraw() {
        Log.i(MainActivity.TAG, "redraw()")
        val v = getMyDrawView()
        // Todo: Animationen stoppen!
        // falls sich die Ausdehnung des Spielfeldes geaendert hat
        v?.calculateBoardDimensions()
        // Spielfeld in View einpassen
        v?.calculateTranslation()
        // Animation beenden und angehobenen Ball senken, falls nötig
        v?.flatten()
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
    override fun holeBall(fromCol: Int, toCol: Int, toRow: Int, color: Int) {
        val v = getMyDrawView()
        v?.holeBall(fromCol, toCol, toRow, color)
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        val v = getMyDrawView()
        v?.liftAndHoleBall(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * User clicked on menu button
     */
    fun onMenuKlicked(vi: View) {
        Log.i(TAG, "user clicked on menu button")
        Intent(this, SettingsActivity::class.java).also {
            startActivity(it)
        }
    }

    /**
     * User clicked on new game button
     */
    fun onNewGameKlicked(vi: View) {
        Log.i(TAG, "user clicked on new game button")
        gameState1Up?.actionNewGame()
    }

    /**
     * User clicked on new game button
     */
    fun onResetGameKlicked(vi: View) {
        Log.i(TAG, "user clicked on reset game button")
        gameState1Up?.actionResetGame()
    }

    /**
     * User clicked on undo button
     */
    fun onUndoKlicked(vi: View) {
        Log.i(TAG, "user clicked on undo button")
        gameState1Up?.actionUndo()
    }

    /**
     * User clicked on new game button
     */
    fun onCheatKlicked(vi: View) {
        Log.i(TAG, "user clicked on cheat button")
        gameState1Up?.actionCheat()
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun enableUndo() {
        Log.i(TAG, "enableUndo()")
        val v = findViewById<ImageView?>(R.id.undo)
        v?.isEnabled = true
        // volle Sichtbarkeit
        v?.alpha = 1.0F
    }

    /**
     * Methode von GameStateListenerInterface geerbt
     */
    override fun disableUndo() {
        Log.i(TAG, "disableUndo()")
        val v = findViewById<ImageView?>(R.id.undo)
        v?.isEnabled = false
        // halbtransparent / ausgegraut
        v?.alpha = 0.5F
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
    }
}