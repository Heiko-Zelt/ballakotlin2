package de.heikozelt.ballakotlin2.model

class AdvancedJury(gs: GameState): NeutralJury(gs) {

    /**
     * Bewertet / vergiebt Punkte >= 1 fuer einen Rückwärts-Zug.
     * @return niedrige Zahl: schlecht, hoche Zahl: gut
     * Kategorie a: Zug auf einfarbige Säule anderer Farbe (weitere Unterteilung nach Höhe der Säule)
     * Kategorie b: Zug auf andersfarbigen Ball, der darunter keinen gleichfarbigen Ball hat
     * Kategorie c: Zug in leere Röhre
     * Kategorie d: Zug auf gleichfarbigen Ball
     * Neu: Bonus-Punkte für Zug, der dem letzten identisch ist
     * todo: Kategorie e: Zug mit andersfarbigem Ball, auf einen Ball der darunter einen gleichfarbigen hat,
     *   aber nicht alle in der Ziel-Röhre gleichfarbig sind (kompliziert)
     */
    override fun rateBackwardMove(move: Move): Int {
        //console.debug('move ' + JSON.stringify(move))

        var rating = 0

        // Zug indentisch letzem Zug / mehrere gleichfarbige Bälle umschichten
        if(gs.moveLog.isNotEmpty()) {
            var last = gs.moveLog.last()
            // vergleicht Daten (nicht Referenzen wie in Java)
            if(move == last) {
                rating += 20
            }
        }

        // Zug in leere Röhre
        if(gs.tubes[move.to].isEmpty()) {
            //console.debug('Zug in leere Röhre')
            return rating + 50
        }

        // Zug auf gleichfarbigen Ball
        val ballColor = gs.tubes[move.from].colorOfTopmostBall()
        val targetColor = gs.tubes[move.to].colorOfTopmostBall()
        // console.debug('ballColor ' + ballColor + '== targetColor ' + targetColor + '?')
        if(targetColor == ballColor) {
            //console.debug('Zug auf gleichfarbigen Ball')
            return rating + 50
        }

        // Zug auf einfarbige Säule anderer Farbe (weitere Unterteilung nach Höhe der Säule)
        val n = gs.tubes[move.to].unicolor()
        //console.debug('unicolor: ' + n)
        if(n > 1) {
            val points = gs.tubeHeight - n - 1
            //console.debug('Zug auf einfarbige Säule anderer Farbe :-( Punkte: ' + points);
            return rating + points
        }

        // Zug auf andersfarbigen Ball, der darunter keinen gleichfarbigen Ball hat
        //console.debug('Zug auf andersfarbigen Ball, der darunter keinen gleichfarbigen Ball hat');
        return rating + 40
    }
}