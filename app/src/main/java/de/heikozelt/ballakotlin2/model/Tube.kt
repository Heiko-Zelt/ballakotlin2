package de.heikozelt.ballakotlin2.model

import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException

/**
 * Array indices are of type Int. Color numbers (elements in array) are of type Byte.
 */
class Tube(val tubeHeight: Int) {

    /**
     * tatsächlicher Füllstand
     */
    var fillLevel: Int = 0

    /**
     * Inhalt einer Röhre.
     * Höhe einer Röhre ist konstant, deswegen Array
     */
    var cells = Array(tubeHeight) { 0.toByte() }

    /**
     * liefert wahr, wenn Röhre voll ist
     */
    fun isFull(): Boolean {
        return fillLevel == tubeHeight
    }

    /**
     * Liefert wahr, wenn Röhre leer ist
     * (inline liefert kaum spürbare Performance-Verbesserung)
     */
    fun isEmpty(): Boolean {
        return fillLevel == 0
    }

    /**
     * Füllt eine Röhre vollständig mit Bällen einer Farbe
     * initialColor: 1...Anzahl Farben
     * 0 ist nicht erlaubt
     */
    fun fillWithOneColor(initialColor: Byte) {
        if (initialColor == 0.toByte()) {
            throw IllegalArgumentException("Farbe 0 als Parameter nicht erlaubt")
        }
        for (i in 0 until tubeHeight) {
            this.cells[i] = initialColor
        }
        fillLevel = tubeHeight
    }

    /**
     * Fügt der Röhre einen Ball hinzu
     */
    fun addBall(color: Byte) {
        if (isFull()) {
            throw IndexOutOfBoundsException("tube is already full")
        }
        cells[fillLevel] = color
        fillLevel++
    }

    /**
     * Entnimmt der Röhre einen Ball
     */
    fun removeBall(): Byte {
        if (isEmpty()) {
            throw IndexOutOfBoundsException("tube is already empty")
        }
        fillLevel--
        val color = cells[fillLevel]
        cells[fillLevel] = 0.toByte()
        return color
    }

    /**
     * Liefert die Farbe des obersten Balles
     */
    fun colorOfTopmostBall(): Byte {
        if (isEmpty()) {
            throw IndexOutOfBoundsException("tube is empty")
        }
        return cells[fillLevel - 1]
    }

    fun colorOfTopSecondBall(): Byte {
        if (fillLevel < 2) {
            throw IndexOutOfBoundsException("tube has less than 2 balls")
        }
        return cells[fillLevel - 2]
    }

    /**
     * Der Spielstand reflektiert den Stand nach dem Vorwärts-Spielzug.
     * Daher ist die Berechnung bei einem Rückwärts-Spielzug anders.
     */
    fun isReverseDonorCandidate(): Boolean {
        // aus einer leeren Röhre kann kein Zug erfolgen
        if (isEmpty()) {
            return false
        }
        // vorwärts gedacht: auf den Boden der leeren Röhre kann immer gezogen werden
        if (fillLevel == 1) {
            return true
        }
        // vorwärts gedacht: Zug auf gleiche Farbe ist erlaubt
        if (colorOfTopmostBall() == colorOfTopSecondBall()) {
            return true
        }
        return false
    }

    fun isReverseReceiverCandidate(): Boolean {
        return !isFull()
    }
    /**
     * Gibt Anzhal der Bälle zurück, wenn alle die gleiche Farbe haben.
     * Gibt null zurück, wenn Röhre leer ist oder die Bälle unterschiedliche Farbe haben.
     * Gibt 1 zurück, wenn nur ein Ball in der Röhre ist.
     */
    fun unicolor(): Int {
        if(isEmpty()) {
            //Log.i("unicolor: Sonderfall leer")
            return 0
        }
        if(fillLevel == 1) {
            //Log.i("unicolor: Sonderfall 1")
            return 1
        }
        val color = cells[0]
        var i = 0
        do {
            if(cells[i] != color) {
                //Log.i("unicolor: unterschiedlich")
                return 0
            }
            i++
        } while( i < fillLevel)
        //Log.i("unicolor: i=${i}")
        return i
    }

    /**
     * Zähl von oben nach unten, wieviel Bälle die gleiche Farbe haben.
     * @return 0 für leere Röhre, sonst Anzahl gleichfarbiger Bälle.
     */
    fun countTopBallsWithSameColor(): Int {
        // Sonderfall: leere Röhre
        if(fillLevel == 0) {
            return 0
        }
        // Normalfall
        val color = colorOfTopmostBall()
        var row = fillLevel
        var count = 0
        do {
            row--
            //Log.d(TAG,"row=${row}")
            if(cells[row] == color) {
                count++
            } else {
                break
            }
        } while(row > 0)
        return count
    }

    fun freeCells(): Int {
        return tubeHeight - fillLevel
    }

    /**
     * liefert wahr, wenn die Röhre gelöst ist,
     * also die Röhre voll ist und alle Bälle die gleiche Farbe haben.
     */
    fun isSolved(): Boolean {
        if(!isFull()) {
            return false
        }
        val color = this.cells[0]
        for(i in 1 until tubeHeight) {
            if(cells[i] != color) {
                return false
            }
        }
        return true
    }

    fun clone(): Tube {
        val miniMe = Tube(tubeHeight)
        for(i in 0 until tubeHeight) {
            miniMe.cells[i] = cells[i]
        }
        miniMe.fillLevel = fillLevel
        return miniMe
    }

    fun contentEquals(other: Tube): Boolean {
        return cells contentEquals other.cells
    }

    companion object {
        private const val TAG = "balla.Tube"
    }
}