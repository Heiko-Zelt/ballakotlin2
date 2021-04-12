package de.heikozelt.ballakotlin2

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.Move


class MainActivity : AppCompatActivity() {

    /**
     * welche Spalte wurde zuerst geklickt
     * -1 bedeutet keine
     */
    var donorIndex: Int = -1

    /**
     * auf welcher Höhe lag der oberste Ball bevor er angehoben wurde.
     * (nur für den Fall, dass ein Zug abgebrochen wird und er wieder gesenkt wird.)
     * -1 bedeutet keine
     */
    var donorRow: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.onCreate() :-(")
            return
        }

        //val gameState = app.gameState
        //Log.i(_TAG, gameState.toJson())
    }

    fun puzzleSolved() {
        Log.i(TAG, "puzzle solved")
        //setTimeout(function() {
        val alertBuilder = AlertDialog.Builder(this)
        with(alertBuilder) {
            setTitle("Genial!")
            setMessage("Du hast das Puzzle gelöst.")
            setCancelable(false)
            setPositiveButton("ok") { dialogInterface, which ->
                Toast.makeText(applicationContext, "neues Spiel", Toast.LENGTH_LONG).show()
            }
            show()
        }
        newGame()
        val v = findViewById<MyDrawView?>(R.id.myDrawView)
        v?.resetGameView()
    }

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
                v?.liftBall(clickedCol)
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

    fun newGame(view: View) {
        newGame()
    }

    fun newGame() {
        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.newGame() :-(")
            return
        }
        app.gameState = GameState(app.numberOfColors, app.numberOfExtraTubes, app.tubeHeight)
        app.gameState.newGame()
        app.originalGameState = app.gameState.clone()
        val v = findViewById<MyDrawView?>(R.id.myDrawView)
        v?.resetGameView()
    }

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

    companion object {
        private const val TAG = "ballas MainActivity"
    }
}