package de.heikozelt.ballakotlin2.view

import android.util.Log
import kotlin.math.min

abstract class BoardLayout(var numberOfTubes: Int, var tubeHeight: Int) {

    /**
     * virtuelle Spielbrett-Koordinaten
     */
    var boardWidth = 0
    var boardHeight = 0

    /**
     * Transformations-Parameter zwischen Spielbrett- und DrawView-Koordinaten
     */
    var scaleFactor = 1.0f
    var transX = 0.0f
    var transY = 0.0f

    init {
        calculateBoardDimensions()
    }

    /**
     * wird aufgerufen wenn
     * der Spieler schummelt / eine Röhre hinzufuegt
     * oder unter Einstellungen die Dimensionen ändert.
     */
    fun setBoardDimensions(newNumberOfTubes: Int, newTubeHeight: Int) {
        numberOfTubes = newNumberOfTubes
        tubeHeight = newTubeHeight
        calculateBoardDimensions()
    }

    /**
     * Berechnet die virtuelle Groesse des Spielbretts.
     */
    abstract fun calculateBoardDimensions()

    /**
     * Berechnet die Transformations-Paramater zur Umrechnung
     * zwischen realen Pixels und virtuellen Einheiten.
     */
    fun calculateTranslation(w: Int, h: Int) {
        //Log.i(TAG, "onon onSizeChanged(w=${w}, h=${h})")
        //circleX = w / 2f
        //circleY = h / 2f
        //radius = 100f

        val scaleX = w / boardWidth.toFloat()
        val scaleY = h / boardHeight.toFloat()
        scaleFactor = min(scaleX, scaleY)
        transX = w.div(scaleFactor).minus(boardWidth).div(2f)
        transY = h.div(scaleFactor).minus(boardHeight).div(2f)
    }

    /**
     * eigene Methode
     * Umrechnung von Spalte/Nummer der Röhre zu virtuellen X-Koordinaten des Mittelpunktes
     */
    abstract fun ballX(col: Int): Int

    /**
     * eigene Methode.
     * Umrechnung von Zeile zu virtuellen Y-Koordinaten des Mittelpunktes.
     */
    abstract fun ballY(col: Int, row: Int): Int

    abstract fun liftedBallY(col: Int): Int

    abstract fun tubeX(col: Int): Int

    abstract fun tubeY(col: Int): Int

    /**
     * Umrechnung von Maus-Klick/DrawView-X-Koordinate in virtuelle Spielbrett-Koordinate
     */
    fun virtualX(x: Float): Int {
        return (x / scaleFactor - transX).toInt()
    }

    /**
     * Umrechnung von Maus-Klick/DrawView-Y-Koordinate in virtuelle Spielbrett-Koordinate
     */
    fun virtualY(y: Float): Int {
        return (y / scaleFactor - transY).toInt()
    }

    /**
     * Umrechnung von Spielbrett-X-Y-Koordinaten in Spalte
     */
    abstract fun column(virtualX: Int, virtualY: Int): Int

    /**
     * Auf eine Röhre geklickt?
     * Die schmale Spalte zwischen 2 Röhren wird auch akzeptiert.
     */
    abstract fun isInside(virtualX: Int, virtualY: Int): Boolean

    companion object {
        private const val TAG = "balla.BoardLayout"
    }


}