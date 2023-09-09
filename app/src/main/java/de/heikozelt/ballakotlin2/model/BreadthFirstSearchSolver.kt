package de.heikozelt.ballakotlin2.model

import android.util.Log

/**
 * interative breadth-first search
 */
class BreadthFirstSearchSolver : Solver {

    fun equalsFunction(a: Array<Byte>, b: Array<Byte>): Boolean {
        return a.contentEquals(b)
    }

    fun hashCodeFunction(a: Array<Byte>): Int {
        return a.contentHashCode()
    }

    override suspend fun findSolution(gs: GameState): SearchResult {
        Log.d(TAG, "find Solution for\n${gs.toAscii()}")
        // make sure gs is not modified and existing move log is ignored
        val gs2 = gs.cloneWithoutLog()
        val result = SearchResult()
        val firstMoves = gs2.allUsefulMovesIntegrated()
        val previousGameStates = FifoHashSet(::hashCodeFunction, :: equalsFunction, MAX_CAPACITY_PREVIOUS)
        val latestGameStates = mutableListOf<EfficientList<Array<Byte>>>()
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
            val list = EfficientList<Array<Byte>>()
            list.add(gs2.toBytesNormalized())
            latestGameStates.add(list)
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
                val newList = EfficientList<Array<Byte>>()
                for (bytes in latestGameStates[branch]) {
                    gs2.fromBytes(bytes)
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
                            val newBytes = gs2.toBytesNormalized()
                            if (newBytes !in previousGameStates) {
                                newList.add(newBytes)
                                previousGameStates.put(newBytes)
                            }
                        }
                        gs2.moveBall(move.backwards())
                    }
                }
                latestGameStates[branch] = newList
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

        private const val MAX_CAPACITY_PREVIOUS = 50_000

        // TODO: gar keine Überschreitung zulassen
        private const val MAX_CAPACITY_LATEST = 40_000
    }
}