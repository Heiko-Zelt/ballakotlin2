package de.heikozelt.ballakotlin2.model

/**
 * Repräsentiert einen Spielzug
 * @param from  Index der Röhre, aus der ein Ball entnommen wird
 * @param to    Index der Röhre, in die ein Ball hinzugefügt wird
 * equals()-Methode wird automatisch generiert
 */
data class Move(val from: Int, val to: Int) {

    fun backwards(): Move {
        val retro = Move(this.to, this.from)
        return retro
    }
}