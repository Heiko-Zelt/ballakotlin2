package de.heikozelt.ballakotlin2.model

import android.util.Log

/**
 * interative breadth-first search
 */
class BreadthFirstSearchSolver : Solver {
    override suspend fun findSolution(gs: GameState): SearchResult {
        Log.d(TAG, "find Solution for\n${gs.toAscii()}")
        // make sure gs is not modified and existing move log is ignored
        val gs2 = gs.cloneWithoutLog()
        val result = SearchResult()
        val firstMoves = gs2.allUsefulMovesIntegrated()
        val previousGameStates = LimitedSet<SpecialArray>(MAX_CAPACITY_PREVIOUS)
        val latestGameStates = mutableListOf<HashSet<SpecialArray>>()
        if(gs.isSolved()) {
            result.status = SearchResult.STATUS_ALREADY_SOLVED
            return result
        }
        firstMoves.forEach { move ->
            //Log.d(TAG, "\n${move.toAscii()}")
            gs2.moveBall(move)
            if(gs2.isSolved()) {
                result.status = SearchResult.STATUS_FOUND_SOLUTION
                result.move = move
                return result
            }
            //Log.d(TAG, "\n${gs2.toAscii()}")
            latestGameStates.add(hashSetOf(SpecialArray(gs2.toBytesNormalized())))
            gs2.moveBall(move.backwards())
        }
        for (level in 0 until MAX_LEVEL) {
            Log.d(TAG, "level #$level")
            // gibt es noch offene Pfade?
            if (latestGameStates.all { it.size == 0 }) {
                // wenn nicht, dann ist es unlösbar
                result.status = SearchResult.STATUS_UNSOLVABLE
                return result
            }

            for (branch in latestGameStates.indices) {
                Log.d(TAG, "latestGameStates[branch].size: ${latestGameStates[branch].size}")
                val newSet = hashSetOf<SpecialArray>()
                for (wrapper in latestGameStates[branch]) {
                    gs2.fromBytes(wrapper.bytes)
                    val moves = gs2.allUsefulMovesIntegrated()
                    moves.forEach { move ->
                        //Log.d(TAG, "\n${move.toAscii()}")
                        gs2.moveBall(move)
                        //Log.d(TAG, "\n${gs2.toAscii()}")
                        if (gs2.isSolved()) {
                            result.status = SearchResult.STATUS_FOUND_SOLUTION
                            result.move = firstMoves[branch]
                            return result
                        } else {
                            val wrapped = SpecialArray(gs2.toBytesNormalized())
                            if (wrapped !in previousGameStates) {
                                newSet.add(wrapped)
                                previousGameStates.add(wrapped)
                            }
                        }
                        gs2.moveBall(move.backwards())
                    }
                }
                latestGameStates[branch] = newSet
            }

            //val sizeLatest = latestGameStates.fold(0) { sum, element -> sum + element.size }
            val sizeLatest = latestGameStates.sumOf{ it.size }
            Log.d(TAG, "previous size: ${previousGameStates.size()}, latest size: $sizeLatest")
            if(sizeLatest >= MAX_CAPACITY_LATEST) {
                break
            }
        }
        // Es gibt noch offene Pfade und Lösung wurde (noch) nicht gefunden
        result.status = SearchResult.STATUS_OPEN
        return result
    }


    companion object {
        private const val TAG = "balla.BreathFirstSolver"

        private const val MAX_LEVEL = 100

        private const val MAX_CAPACITY_PREVIOUS = 30_000

        private const val MAX_CAPACITY_LATEST = 30_000
    }
}