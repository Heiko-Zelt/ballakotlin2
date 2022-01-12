package de.heikozelt.ballakotlin2.model

/**
 * Repräsentiert einen Spielzug
 * (equals()-Methode wird automatisch generiert, wenn es eine data-class wäre)
 */
class Move() {

    var from: Int = 0
    var to: Int = 0

    /*
     * @param _from Index der Röhre, aus der ein Ball entnommen wird
     * @param _to   Index der Röhre, in die ein Ball hinzugefügt wird
     */
    constructor(_from: Int, _to: Int): this() {
        from = _from
        to = _to
    }

    /**
     * liefert einen umgekehrten/undo Spielzug.
     */
    fun backwards(): Move {
        return Move(to, from)
    }

    /**
     * Liefert wahr, wenn der andere Spielzug diesem gleicht
     * (Methode equals(Any?) wird für data class implizit generiert.)
     */
    fun equalsMove(otherMove: Move): Boolean {
        return (otherMove.from == from) && (otherMove.to == to)
    }

    /**
     * (Methode equals(Any?) wird für data class implizit generiert.)
     */
    override fun equals(other: Any?): Boolean {
        return when(other) {
            null -> false
            !is Move -> false
            else -> equalsMove(other)
        }
    }

    fun copy() {

    }

    /**
     * Beispiele:
     * "0 -> 17"
     * " 0->17"
     */
    fun fromAscii(ascii: String) {
        val noWhitesp = ascii.replace(FILTER_REGEX, "")
        val positions = noWhitesp.split("->")
        if(positions.size != 2) {
            throw IllegalArgumentException("Wrong syntax. Correct example: 0 -> 17")
        }
        try {
            val newFrom = positions[0].toInt()
            val newTo = positions[1].toInt()
            // change only, if no exception occured
            from = newFrom
            to = newTo
        } catch(e: NumberFormatException) {
            throw IllegalArgumentException("Position not a number.")
        }
    }

    /**
     * Beispiel:
     * "0->17"
     */
    fun toAscii(): String {
        return "${from}->${to}"
    }

    companion object {
        private val FILTER_REGEX = Regex("""\s""")
    }
}