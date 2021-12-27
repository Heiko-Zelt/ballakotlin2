package de.heikozelt.ballakotlin2.model

import android.util.Log

class AnotherJury(gs: GameState): NeutralJury(gs) {

    /**
     * Bewertet / vergiebt Punkte >= 1 fuer einen Rückwärts-Zug.
     * @return niedrige Zahl: schlecht, hoche Zahl: gut
     */
    override fun rateBackwardMove(move: Move): Int {
        // Log.d('move ' + JSON.stringify(move))

        var rating = 1

        Log.d(TAG,"possible move: ${move.from} --> ${move.to}")

        if(gs.tubes[move.to].isEmpty()) {
            Log.d(TAG,"Zug in leere Röhre ist sehr gut")
            rating = 41
        } else if((gs.tubes[move.to].fillLevel >= 1) && gs.isSameColor(move.from, move.to)) {
            Log.d(TAG,"Zug auf einen Ball in gleicher Farbe von einer Säule in gleicher Farbe ist auch sehr gut")
            rating = gs.tubes[move.to].countTopBallsWithSameColor() * 12 + gs.tubes[move.from].countTopBallsWithSameColor() * 12
        } else if(gs.tubes[move.to].countTopBallsWithSameColor() == 1) {
            if(gs.tubes[move.to].cells[0] != gs.tubes[move.from].colorOfTopmostBall()) {
                Log.d(TAG,"Zug auf einzelnen Ball einer anderen Farbe ist auch sehr gut")
                // je höher der Stapel gleichfarbiger Bälle in der Quell-Röhre desto besser
                // je niedriger der Füllstand der Ziel-Röhre, desto besser
                rating = gs.tubes[move.from].countTopBallsWithSameColor() * 4 + gs.tubes[move.to].freeCells() * 4
            }
        }  else {
            Log.d(TAG, "langweiliger Zug")
        }
        Log.d(TAG,"rating=${rating}")
        return rating
    }

    companion object {
        private const val TAG = "balla.AnotherJury"
    }
}