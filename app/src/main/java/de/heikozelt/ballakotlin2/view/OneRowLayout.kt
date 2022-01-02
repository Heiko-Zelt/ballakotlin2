package de.heikozelt.ballakotlin2.view

import android.util.Log
import kotlin.math.min

class OneRowLayout(numberOfTubes: Int, tubeHeight: Int) : BoardLayout(numberOfTubes, tubeHeight) {


    /**
     * Berechnet die virtuelle Groesse des Spielbretts.
     */
    override fun calculateBoardDimensions() {
        Log.d(TAG,"calculateBoardDimensions() numberOfTubes=$numberOfTubes, boardHeight=$boardHeight")
        boardWidth = numberOfTubes * MyDrawView.TUBE_WIDTH + (numberOfTubes - 1) * MyDrawView.TUBE_PADDING
        Log.i(TAG, "boardWidth: $boardWidth")

        boardHeight = (tubeHeight + 1) * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING
        Log.i(TAG, "boardHeight: $boardHeight")
    }

    /**
     * eigene Methode
     * Umrechnung von Spalte/Nummer der Röhre zu virtuellen X-Koordinaten des Mittelpunktes
     */
    override fun ballX(col: Int): Int {
        return col * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) + MyDrawView.BALL_PADDING+ MyDrawView.BALL_RADIUS
    }

    /**
     * eigene Methode.
     * Umrechnung von Zeile zu virtuellen Y-Koordinaten des Mittelpunktes.
     */
    override fun ballY(col: Int, row: Int): Int {
        return (tubeHeight - row) * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS
    }

    override fun liftedBallY(col: Int): Int {
        return MyDrawView.BALL_RADIUS
    }

    override fun tubeX(col: Int): Int {
        return col * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING)
    }

    override fun tubeY(col:Int): Int {
        return MyDrawView.BALL_DIAMETER
    }

    /**
     * Umrechnung von Spielbrett-X-Y-Koordinaten in Spalte
     */
    override fun column(virtualX: Int, virtualY: Int): Int {
        return (virtualX / (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING)).toInt()
    }

    override fun isInside(virtualX: Int, virtualY: Int): Boolean {
        return virtualX >= 0 && virtualX < boardWidth && virtualY >= 0 && virtualY < boardHeight
    }

    companion object {
        private const val TAG = "balla.OneRowLayout"
    }
}