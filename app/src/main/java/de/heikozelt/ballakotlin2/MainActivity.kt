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
     */
    var donorIndex: Int? = null

    /**
     * auf welcher Höhe lag der oberste Ball bevor er angehoben wurde.
     * (nur für den Fall, dass ein Zug abgebrochen wird und er wieder gesenkt wird.)
     */
    var donorRow: Int? = null

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

    fun tubeClicked(clickedCol: Int) {
        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.tubeClicked() :-(")
            return
        }
        val v = findViewById<MyDrawView?>(R.id.myDrawView)
        if (donorIndex == null) {
            if (!app.gameState.tubes[clickedCol].isEmpty()) {
                //oder v?.liftBall(clickedCol, app.gameState.tubes[clickedCol].fillLevel - 1)
                v?.liftBall(clickedCol)
            } else {
                if (app.gameState.isMoveAllowed(donorIndex as Int, clickedCol)) {
                    //console.debug('move from ' + donorIndex + ' to ' + clickedCol);
                    val move = Move(donorIndex as Int, clickedCol)
                    app.gameState.moveBallAndLog(move)
                    v?.normalMove(move)
                    /*
                    Todo: Undo Button disabeln
                    if (app.gameState.moveLog.size != 0) {
                        val undoButton = document.getElementById('undoButton')
                        undoButton.disabled = false
                    }
                    */
                    if (app.gameState.isSolved()) {
                        //setTimeout(function() {
                        val alertBuilder = AlertDialog.Builder(this)
                        with(alertBuilder) {
                            setTitle("Your Alert")
                            setMessage("Your Message")
                            setCancelable(false)
                            setPositiveButton("ok") {dialogInterface, which ->
                                Toast.makeText(applicationContext,"clicked yes",Toast.LENGTH_LONG).show()
                            }
                            show()
                        }
                        newGame()
                        v?.resetGameView()
                    }
                } else {
                    //console.debug('Wechsel');
                    // Ball wieder runter
                    v?.dropBall(donorIndex as Int)
                    // dafür anderer Ball hoch
                    v?.liftBall(clickedCol)
                }
            }
        }
        invalidateBoardView()
    }

    private fun invalidateBoardView() {
        val v = findViewById<MyDrawView?>(R.id.myDrawView)
        v?.invalidate()
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
        invalidateBoardView()
    }

    fun resetGame(view: View) {
        val app = application as BallaApplication?
        if (app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.newGame() :-(")
            return
        }
        app.gameState = app.originalGameState.clone()
        invalidateBoardView()
    }

    companion object {
        private const val TAG = "balla MainActivity"
    }
}