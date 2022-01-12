package de.heikozelt.ballakotlin2.view

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameObserverInterface

class GameObserverMock: GameObserverInterface {

    var observationsLog = mutableListOf<String>()

    override fun redraw() {
        observationsLog.add("redraw()")
    }

    override fun liftBall(col: Int, row: Int, color: Int) {
        observationsLog.add("liftBall(col=$col, row=$row, color=$color)")
    }

    override fun dropBall(col: Int, row: Int, color: Int) {
        observationsLog.add("dropBall(col=$col, row=$row, color=$color)")
    }

    override fun holeBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        observationsLog.add("holeBall(fromCol=$fromCol, toCol=$toCol, toRow=$toRow, color=$color)")
    }

    override fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        observationsLog.add("liftAndHoleBall(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)")
    }

    override fun holeBallTubeSolved(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        observationsLog.add("holeBallTubeSolved(fromCol=$fromCol, toCol=$toCol, toRow=$toRow, color=$color)")
    }

    override fun liftAndHoleBallTubeSolved(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        observationsLog.add("liftAndHoleBallTubeSolved(fromCol=$fromCol, toCol=$toCol, toRow=$toRow, color=$color)")
    }

    override fun enableUndoAndReset(enabled: Boolean) {
        observationsLog.add("enableResetAndUndo(enabled=$enabled)")
    }

    override fun enableHelp(enabled: Boolean) {
        observationsLog.add("enableHelp(enabled=$enabled)")
    }

    override fun enableCheat(enabled: Boolean) {
        observationsLog.add("enableCheat(enabled=$enabled)")
    }

    override fun puzzleSolved() {
        observationsLog.add("puzzleSolved()")
    }

    override fun newGameToast() {
        observationsLog.add("newGameToast()")
    }

    fun dump() {
        Log.d(TAG, "ObservationsLog: size=${observationsLog.size}")
        for(i in observationsLog.indices) {
            Log.d(TAG, "# $i: ${observationsLog[i]}")
        }
    }

    companion object {
        private const val TAG = "balla.GameObserverMock"
    }
}