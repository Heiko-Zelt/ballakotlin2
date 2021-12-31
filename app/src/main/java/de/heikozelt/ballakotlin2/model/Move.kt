package de.heikozelt.ballakotlin2.model

/**
 * Repräsentiert einen Spielzug
 * @param from  Index der Röhre, aus der ein Ball entnommen wird
 * @param to    Index der Röhre, in die ein Ball hinzugefügt wird
 * equals()-Methode wird automatisch generiert
 */
data class Move(val from: Int, val to: Int) {

    /**
     * liefert einen umgekehrten/undo Spielzug.
     */
    fun backwards(): Move {
        return Move(to, from)
    }

    /**
     * Liefert wahr, wenn der andere Spielzug diesem gleicht
     * Methode wird implizit generiert
    fun equals(otherMove: Move): Boolean {
        return (otherMove.from == from) && (otherMove.to == to)
    }
    */
}