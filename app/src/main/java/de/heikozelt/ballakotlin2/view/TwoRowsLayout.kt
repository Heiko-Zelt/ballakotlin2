package de.heikozelt.ballakotlin2.view

import android.util.Log
import kotlin.math.min

class TwoRowsLayout(numberOfTubes: Int, tubeHeight: Int) : BoardLayout(numberOfTubes, tubeHeight) {

    var numberOfUpperTubes = 0
    var lowerTubesTop = 0
    var lowerTubesLeft = 0

    /**
     * Berechnet die virtuelle Groesse des Spielbretts.
     */
    override fun calculateBoardDimensions() {
        Log.d(
            TAG,
            "calculateBoardDimensions() numberOfTubes=$numberOfTubes, boardHeight=$boardHeight"
        )

        numberOfUpperTubes = numberOfTubes - numberOfTubes / 2

        boardWidth =
            numberOfUpperTubes * MyDrawView.TUBE_WIDTH + (numberOfUpperTubes - 1) * MyDrawView.TUBE_PADDING
        Log.i(TAG, "boardWidth: $boardWidth")

        boardHeight =
            2 * ((tubeHeight + 1) * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING) + TUBE_VERTICAL_PADDING
        Log.i(TAG, "boardHeight: $boardHeight")

        lowerTubesTop =
            (tubeHeight + 1) * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING + TUBE_VERTICAL_PADDING
        lowerTubesLeft = if (numberOfTubes % 2 == 0) {
            // gerade. Oben und unten sind gleich viele Röhren.
            0
        } else {
            // ungerade. Unten befindet sich eine Röhre weniger als oben.
            (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) / 2
        }
    }

    /**
     * eigene Methode
     * Umrechnung von Spalte/Nummer der Röhre zu virtuellen X-Koordinaten des Mittelpunktes
     */
    override fun ballX(col: Int): Int {
        //Log.d(TAG, "lowerTubesLeft=$lowerTubesLeft, col =$col, numberOfTubes=$numberOfTubes")
        return if (col < numberOfUpperTubes) {
            // obere Reihe
            col * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS
        } else {
            // untere Reihe
            lowerTubesLeft + (col - numberOfUpperTubes) * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS
        }
    }

    /**
     * eigene Methode.
     * Umrechnung von Zeile zu virtuellen Y-Koordinaten des Mittelpunktes.
     */
    override fun ballY(col: Int, row: Int): Int {
        return if (col < numberOfUpperTubes) {
            // obere Reihe
            (tubeHeight - row) * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS
        } else {
            // untere Reihe
            lowerTubesTop + (tubeHeight - row) * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS
        }
    }

    override fun liftedBallY(col: Int): Int {
        return if (col < numberOfUpperTubes) {
            // obere Reihe
            MyDrawView.BALL_RADIUS
        } else {
            // untere Reihe
            lowerTubesTop + MyDrawView.BALL_RADIUS
        }
    }

    override fun tubeX(col: Int): Int {
        //Log.d(TAG, "tubeX(col=$col)")
        //Log.d(TAG, "lowerTubesLeft=$lowerTubesLeft, numberOfUpperTubes=$numberOfUpperTubes")
        return if (col < numberOfUpperTubes) {
            col * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING)
        } else {
            lowerTubesLeft + (col - numberOfUpperTubes) * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING)
        }
    }

    override fun tubeY(col: Int): Int {
        return if (col < numberOfUpperTubes) {
            MyDrawView.BALL_DIAMETER
        } else {
            lowerTubesTop + MyDrawView.BALL_DIAMETER
        }
    }

    /**
     * Umrechnung von Spielbrett-X-Y-Koordinaten in Spalte.
     * erst Auswertung der Y-Koordinate und dann der X-Koordinate
     */
    override fun column(virtualX: Int, virtualY: Int): Int {
        return if (virtualY < lowerTubesTop) {
            Log.d(TAG, "obere Reihe")
            (virtualX / (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING)).toInt()
        } else {
            Log.d(TAG, "untere Reihe")
            if (numberOfTubes % 2 == 0) {
                Log.d(TAG, "gerade, unten gleich viele Röhren")
                (virtualX / (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING)).toInt() + numberOfUpperTubes
            } else {
                Log.d(TAG, "ungerade, unten eine Röhre weniger")
                Log.d(TAG, "virtualX=$virtualX, lowerTubesLeft=$lowerTubesLeft, numberOfUpperTubes=$numberOfUpperTubes")
                ((virtualX - lowerTubesLeft) / (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING)).toInt() + numberOfUpperTubes
            }
        }
    }

    /**
     * erst Auswertung der Y-Koordinate und dann der X-Koordinate
     */
    override fun isInside(virtualX: Int, virtualY: Int): Boolean {
        return if ((virtualY >= 0) && virtualY < (lowerTubesTop - TUBE_VERTICAL_PADDING)) {
            // obere Reihe
            (virtualX >= 0) && (virtualX < boardWidth)
        } else if ((virtualY >= lowerTubesTop) && (virtualY < boardHeight)) {
            // untere Reihe
            if (numberOfTubes % 2 == 0) {
                // gerade, unten gleich viele Röhren
                (virtualX >= 0) && (virtualX < boardWidth)
            } else {
                // ungerade, unten eine Röhre weniger
                (virtualX >= lowerTubesLeft) && (virtualX < (boardWidth - lowerTubesLeft))
            }
        } else {
            // weder obere noch untere Reihe
            false
        }
    }

    companion object {
        private const val TAG = "balla.TwoRowsLayout"

        const val TUBE_VERTICAL_PADDING = 5
    }
}