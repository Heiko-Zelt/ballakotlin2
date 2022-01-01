package de.heikozelt.ballakotlin2.view

class ViewTube(val tubeHeight: Int) {

    /**
     * null bedeutet, freies Feld
     */
    var cells = Array<Ball?>(tubeHeight) { null }

    /**
     * ersetzt die Referenz auf den obersten Ball durch null
     * und lifert eine Referenz zurueck.
     */
    fun eraseTopmostBall(): Ball? {
        for (i in (cells.size - 1) downTo 0) {
            if (cells[i] != null) {
                val ball = cells[i]
                cells[i] = null
                return ball
            }

        }
        return null
    }
}