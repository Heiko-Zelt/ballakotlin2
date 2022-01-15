package de.heikozelt.ballakotlin2.view

import android.util.Log
import de.heikozelt.ballakotlin2.model.GameObserverInterface

class GameObserverMock: GameObserverInterface {

    var observationsLog = mutableListOf<String>()

    override fun redraw() {
        observationsLog.add("redraw()")
    }

    override fun liftBall(column: Int, row: Int) {
        observationsLog.add("liftBall(column=$column, row=$row)")
    }

    override fun dropBall(column: Int, row: Int) {
        observationsLog.add("dropBall(column=$column, row=$row)")
    }

    override fun holeBall(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        observationsLog.add("holeBall(fromColumn=$fromColumn, toColumn=$toColumn, toRow=$toRow)")
    }

    override fun liftAndHoleBall(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        observationsLog.add("liftAndHoleBall(fromColumn=$fromColumn, toColumn=$toColumn, fromRow=$fromRow, toRow=$toRow)")
    }

    override fun holeBallTubeSolved(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        observationsLog.add("holeBallTubeSolved(fromColumn=$fromColumn, toColumn=$toColumn, toRow=$toRow)")
    }

    override fun liftAndHoleBallTubeSolved(fromColumn: Int, toColumn: Int, fromRow: Int, toRow: Int) {
        observationsLog.add("liftAndHoleBallTubeSolved(fromColumn=$fromColumn, toColumn=$toColumn, toRow=$toRow)")
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