package de.heikozelt.ballakotlin2.model

open class NeutralJury(val gs: GameState) {

    /**
     * Bewertet / vergiebt Punkte >= 1 fuer einen Rückwärts-Zug.
     * @return niedrige Zahl: schlecht, hoche Zahl: gut     *
     * Im einfachsten Fall / Jury ist desinteressiert immer 1
     */
    open fun rateBackwardMove(move: Move): Int {
        return 1
    }
}