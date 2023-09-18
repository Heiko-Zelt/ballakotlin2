package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlinx.coroutines.yield

/**
 * recursive depth-first search
 */
class StackSolver: Solver {
    override fun findSolution(gs: GameState): SearchResult {
        Log.d(TAG, "find Solution for\n${gs.toAscii()}")
        val gs2 = gs.cloneWithoutLog()
        val result = SearchResult()
        // erst einfache Lösung suchen, dann Rekursionstiefe erhöhen
        // 1. Abbruchkriterium: Maximale Rekursionstiefe erreicht
        for (recursionDepth in 0..MAX_RECURSION) {
            val startTime = System.nanoTime()
            val previousGameStates = HashSet<SpecialArray>()
            findSolutionNoBackAndForth(gs2, recursionDepth, result, previousGameStates)
            val endTime = System.nanoTime()
            val elapsed = (endTime - startTime) / 1000000
            // 2. Abbruchkriterium: Lösung gefunden
            when (result.status) {
                SearchResult.STATUS_FOUND_SOLUTION -> {
                    Log.d(
                        TAG,
                        "findSolutionNoBackAndForth(maxRecursionDepth=$recursionDepth) -> elapsed=$elapsed msec, found ${result.move?.toAscii()}"
                    )
                    break
                }
                SearchResult.STATUS_UNSOLVABLE -> {
                    Log.d(
                        TAG,
                        "findSolutionNoBackAndForth(maxRecursionDepth=$recursionDepth) -> elapsed=$elapsed msec, unsolvable! :-("
                    )
                    break
                }
                SearchResult.STATUS_OPEN -> {
                    Log.d(
                        TAG,
                        "findSolutionNoBackAndForth(maxRecursionDepth=$recursionDepth) -> elapsed=$elapsed msec, open ends"
                    )
                    // : ${result.openEnds}
                }
                else -> {
                    Log.e(
                        TAG,
                        "findSolutionNoBackAndForth(maxRecursionDepth=$recursionDepth) -> elapsed=$elapsed msec, ???????"
                    )
                    break
                }
            }
            Log.d(TAG, "number of different game states: ${previousGameStates.size}")
            // 3. Abbruchkriterium: Zeit-Überschreitung
            // todo: von was haengt die Anzahl der Verzweigungen ab?
            if (elapsed > MAX_DURATION) {
                break
            }
        }
        return result
    }

    /**
     * Rekursion
     * @param maxRecursionDepth gibt an, wieviele Züge maximal ausprobiert werden.
     * todo: mehr Infos im Ergebnis. Wie tief wurde gesucht? Wie viele offene Pfade gibt es?
     * todo: Zyklen erkennen
     * todo: Spezialfall: Wenn eine Röhre mit wenigen Zügen(?) gefüllt werden kann, dann diesen Zug bevorzugen. Wo liegt die Grenze?
     */

    /*,
       previousGameStates: MutableList<Array<Byte>> = mutableListOf(Array(numberOfTubes * tubeHeight) { 0.toByte() }) */
    fun findSolutionNoBackAndForth(
        gs: GameState,
        maxRecursionDepth: Int,
        result: SearchResult,
        previousGameStates: HashSet<SpecialArray>
    ) {
        // Job canceled? nicht zu oft Kontrolle abgeben.
        //if (maxRecursionDepth > 7) yield()

        if (gs.isSolved()) {
            //Log.d(TAG,"1. Abbruchkriterium: Lösung gefunden")
            result.status = SearchResult.STATUS_FOUND_SOLUTION
            return
        } else if (maxRecursionDepth == 0) {
            //Log.d(TAG, "2. Abbruchkriterium: Maximale Rekursionstiefe erreicht")
            //result.openEnds = 1
            result.status = SearchResult.STATUS_OPEN
            return
        }

        val maxRecursion = maxRecursionDepth - 1
        val moves = gs.allUsefulMovesIntegrated()

        if (moves.isEmpty()) {
            //Log.d(TAG,"3. Abbruchkriterium: keine Züge mehr möglich")
            result.status = SearchResult.STATUS_UNSOLVABLE
            return
        }

        val gsBytes = SpecialArray(gs.toBytesNormalized())
        if(gsBytes in previousGameStates) {
            //Log.d(TAG, "4. Abbruchkriterium: Spielstatus wurde vorher schon Mal erreicht")
            result.status = SearchResult.STATUS_UNSOLVABLE
            return
        } else {
            previousGameStates.add(gsBytes)
        }

        var countOpenBranches = 0
        //var countOpenEnds = 0
        for (move in moves) {
            //Log.d(TAG, "Rekursion")
            gs.moveBallAndLog(move)
            //val newGameState = Array(numberOfTubes * tubeHeight) { 0.toByte() }
            //toBytes(newGameState)
            // Zyklus gefunden, nicht tiefer suchen
            //if (!listContainsArray(previousGameStates, newGameState)) {
            // kein Zyklus, also Rekursion
            //previousGameStates.add(newGameState)
            findSolutionNoBackAndForth(gs, maxRecursion, result, previousGameStates)
            //previousGameStates.removeLast()
            when (result.status) {
                SearchResult.STATUS_FOUND_SOLUTION -> {
                    result.move = move
                    return
                }
                SearchResult.STATUS_OPEN -> {
                    countOpenBranches++
                    //countOpenEnds += result.openEnds
                }
            }
            //} else {
            //    Log.e(TAG,"ZYCLE !!!!")
            //}
            gs.undoLastMove()
        }

        // Wenn eine Lösung gefunden wurde, dann wird das Ergebnis sofort zurückgegeben,
        // sonst müssen alle möglichen Züge ausgewertete werden.
        // wenn alles Sackgassen sind, dann unlösbar
        // wenn ein einzig offener Pfad existier dann Lösung offen.
        // Also müssen entweder die Sackgassen oder die offenen Pfade gezöhlt werden.
        if (countOpenBranches != 0) {
            //result.openEnds = countOpenEnds
            result.status = SearchResult.STATUS_OPEN
        }
        // alles Sackgassen, also auch letzter Pfad
        return
    }

    companion object {
        private const val TAG = "balla.StackSolver"

        private const val MAX_RECURSION = 50

        /**
         * Bei Überschreitung dieser Dauer wird abgebrochen.
         * Das kann etwa 3 Mal so lange sein.
         */
        private const val MAX_DURATION = 8_000
    }
}