package de.heikozelt.ballakotlin2.model

import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException

class Tube(val tubeHeight: Int) {

    var fillLevel: Int = 0
    var cells = Array<Int>(tubeHeight) { 0 }

    /**
     * liefert wahr, wenn Röhre voll ist
     */
    fun isFull(): Boolean {
        return this.fillLevel == tubeHeight
    }

    /**
     * Liefert wahr, wenn Röhre leer ist
     */
    fun isEmpty(): Boolean {
        return this.fillLevel == 0
    }

    /**
     * Füllt eine Röhre vollständig mit Bällen einer Farbe
     * initialColor: 1...Anzahl Farben
     * 0 ist nicht erlaubt
     */
    fun fillWithOneColor(initialColor: Int) {
        if (initialColor == 0) {
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
    fun addBall(color: Int) {
        if (isFull()) {
            throw IndexOutOfBoundsException("tube is already full")
        }
        cells[fillLevel] = color
        fillLevel++
    }

    /**
     * Entnimmt der Röhre einen Ball
     */
    fun removeBall(): Int {
        if (isEmpty()) {
            throw IndexOutOfBoundsException("tube is already empty")
        }
        fillLevel--
        val color = cells[fillLevel]
        cells[fillLevel] = 0
        return color
    }

    /**
     * Liefert die Farbe des obersten Balles
     */
    fun colorOfTopmostBall(): Int {
        if (isEmpty()) {
            throw IndexOutOfBoundsException("tube is empty")
        }
        val color = cells[fillLevel - 1]
        return color
    }

    fun colorOfTopSecondBall(): Int {
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
}