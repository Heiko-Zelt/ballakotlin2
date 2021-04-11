package de.heikozelt.ballakotlin2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import de.heikozelt.ballakotlin2.model.GameState
import de.heikozelt.ballakotlin2.model.Move

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = application as BallaApplication?
        if(app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.onCreate() :-(")
            return
        }

        //val gameState = app.gameState
        //Log.i(_TAG, gameState.toJson())
    }

    fun tubeClicked(col: Int) {
        val app = application as BallaApplication?
        if(app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.tubeClicked() :-(")
            return
        }
        if(app.donorIndex == null) {
            app.donorIndex = col
        } else {
            val m = Move(app.donorIndex as Int, col)
            app.gameState.moveBallAndLog(m)
            app.donorIndex = null
        }
        invalidateBoardView()
    }

    private fun invalidateBoardView() {
        val v = findViewById<MyDrawView?>(R.id.myDrawView)
        v?.invalidate()
    }

    fun newGame(view: View) {
        val app = application as BallaApplication?
        if(app == null) {
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
        if(app == null) {
            Log.e(TAG, "No reference to BallaApplication in MainActivity.newGame() :-(")
            return
        }
        app.gameState = app.originalGameState
        invalidateBoardView()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}