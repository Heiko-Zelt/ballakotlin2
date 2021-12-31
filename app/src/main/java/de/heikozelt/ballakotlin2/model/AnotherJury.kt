package de.heikozelt.ballakotlin2.model

import android.util.Log

class AnotherJury(gs: GameState): NeutralJury(gs) {

    /**
     * Bewertet / vergiebt Punkte >= 1 fuer einen Rückwärts-Zug.
     * @return niedrige Zahl: schlecht, hoche Zahl: gut
     */
    override fun rateBackwardMove(move: Move): Int {
        Log.d(TAG,"rateBackwardMove(${move.from} --> ${move.to})")
        var rating = 1
        if(gs.tubes[move.to].isEmpty()) {
            Log.d(TAG,"Zug in leere Röhre ist sehr gut")
            rating = gs.tubeHeight * FACTOR_EMPTY
        } else if((gs.tubes[move.to].fillLevel >= 1) && gs.isSameColor(move.from, move.to)) {
            Log.d(TAG,"Zug auf einen Ball in gleicher Farbe von einer Säule in gleicher Farbe ist auch sehr gut")
            rating = gs.tubes[move.to].countTopBallsWithSameColor() * FACTOR_SAME + gs.tubes[move.from].countTopBallsWithSameColor() * FACTOR_SAME

            // kein Eintraege in Move-Log!
            if(gs.moveLog.isNotEmpty()) {
                if (gs.moveLog.last() == move) {
                    Log.d(TAG, "Twice-Booster")
                    rating += gs.tubeHeight * FACTOR_TWICE
                }
            }
        } else if(gs.tubes[move.to].countTopBallsWithSameColor() == 1) {
            Log.d(TAG,"oberer Ball ist einzelner")
            if(gs.tubes[move.to].colorOfTopmostBall() != gs.tubes[move.from].colorOfTopmostBall()) {
                Log.d(TAG,"Zug auf einzelnen Ball einer anderen Farbe ist auch sehr gut")
                // je höher der Stapel gleichfarbiger Bälle in der Quell-Röhre desto besser
                // je niedriger der Füllstand der Ziel-Röhre, desto besser
                rating = gs.tubes[move.from].countTopBallsWithSameColor() * FACTOR_ONE + gs.tubes[move.to].freeCells() * FACTOR_ONE
            } else {
                Log.e(TAG,"kann nicht vorkommen")
            }
        }  else {
            // Es gibt schlechte und sehr schlechte Züge
            rating = gs.tubeHeight - gs.tubes[move.to].countTopBallsWithSameColor()
            Log.d(TAG, "schlechter Zug")
        }
        Log.d(TAG,"rating=${rating}")
        return rating
    }

    companion object {
        private const val TAG = "balla.AnotherJury"

        const val FACTOR_SAME = 4
        const val FACTOR_TWICE = 2
        const val FACTOR_ONE = 2
        const val FACTOR_EMPTY = 4
        /*
        const val FACTOR_SAME = 12
        const val FACTOR_ONE = 4
        const val FACTOR_EMPTY = 10
        */

        /*
        const val FACTOR_SAME = 1200
        const val FACTOR_ONE = 400
        const val FACTOR_EMPTY = 1000
        */
    }
}