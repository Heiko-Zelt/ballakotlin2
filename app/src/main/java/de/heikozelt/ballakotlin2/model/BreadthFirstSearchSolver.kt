package de.heikozelt.ballakotlin2.model

import android.util.Log

/**
 * backtracking with interative breadth-first search
 * todo Bug: according to logcat there are multiple threads running at the same time
 */
class BreadthFirstSearchSolver : Solver {

    private var cancel = false

    fun cancelJob() {
        cancel = true
    }

    private fun equalsFunction(a: Array<Byte>, b: Array<Byte>): Boolean {
        return a.contentEquals(b)
    }

    private fun hashCodeFunction(a: Array<Byte>): Int {
        return a.contentHashCode()
    }

    override fun findSolution(gs: GameState): SearchResult {
        // If this method runs multiple times at the same time,
        // too much memory an CPU is consumed.
        // Synchronize prevents this.
        synchronized(this) {
            cancel = false
            jobCounter++
            val jobNum = jobCounter
            jobsHistory += "start $jobNum, "
            Log.d(TAG, "Job #$jobNum: find Solution for\n${gs.toAscii()}")
            Log.d(TAG, "Jobs history: $jobsHistory")
            // make sure gs is not modified and existing move log is ignored
            val gs2 = gs.cloneWithoutLog()
            val result = SearchResult()
            val firstMoves = gs2.allUsefulMovesIntegrated()
            val previousGameStates =
                FifoHashSet(
                    ::hashCodeFunction,
                    ::equalsFunction,
                    MAX_CAPACITY_PREVIOUS,
                    INITIAL_CAPACITY,
                    LOAD_FACTOR
                )
            val latestGameStates = mutableListOf<EfficientList<Array<Byte>>>()
            if (gs.isSolved()) {
                result.status = SearchResult.STATUS_ALREADY_SOLVED
                jobsHistory += "finished already solved $jobNum, "
                return result
            }
            try {
                firstMoves.forEach { move ->
                    //Log.d(TAG, "\n${move.toAscii()}")
                    gs2.moveBall(move)
                    if (gs2.isSolved()) {
                        result.status = SearchResult.STATUS_FOUND_SOLUTION
                        result.move = move
                        jobsHistory += "finished solved $jobNum, "
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
                    if (latestGameStates.all { it.getSize() == 0 }) {
                        // wenn nicht, dann ist es unlösbar
                        result.status = SearchResult.STATUS_UNSOLVABLE
                        jobsHistory += "finished unsolvable $jobNum, "
                        return result
                    }

                    for (branch in latestGameStates.indices) {
                        Log.d(
                            TAG,
                            "latestGameStates[branch].size: ${latestGameStates[branch].getSize()}"
                        )
                        val usedCapacity = latestGameStates.sumOf { it.getCapacity() }
                        val remainingCapacity = MAX_CAPACITY_LATEST - usedCapacity
                        Log.d(
                            TAG,
                            "Game States: latest used capacity: $usedCapacity, remaining capacity: $remainingCapacity, previous size: ${previousGameStates.size()}"
                        )
                        val newList = EfficientList<Array<Byte>>(remainingCapacity, CHUNK_SIZE)
                        for (bytes in latestGameStates[branch]) {
                            if (cancel) {
                                result.status = SearchResult.STATUS_CANCELED
                                jobsHistory += "finished canceled job $jobNum, "
                                return result
                            }
                            gs2.fromBytes(bytes)
                            val moves = gs2.allUsefulMovesIntegrated()
                            moves.forEach { move ->
                                //Log.d(TAG, "\n${move.toAscii()}")
                                gs2.moveBall(move)
                                //Log.d(TAG, "\n${gs2.toAscii()}")
                                if (gs2.isSolved()) {
                                    result.status = SearchResult.STATUS_FOUND_SOLUTION
                                    result.move = firstMoves[branch]
                                    jobsHistory += "finished found $jobNum, "
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
                }
            } catch (ex: EfficientList.CapacityLimitExceededException) {
                Log.d(TAG, "latest game states: capacity limit exceeded")
            }
            // Es gibt noch offene Pfade und Lösung wurde (noch) nicht gefunden
            result.status = SearchResult.STATUS_OPEN
            jobsHistory += "finished open $jobNum, "
            return result
        }
    }


    companion object {
        private var jobCounter = 0
        private var jobsHistory = ""

        private const val TAG = "balla.BreathFirstSearchSolver"
        private const val MAX_LEVEL = 100
        /**
         * parameters for the hash set to store previous game states:
         * less buckets -> more elements in same bucket, less memory usage but
         * more collisions, more time needed for search
         */
        private const val LOAD_FACTOR = 1.0f
        private const val INITIAL_CAPACITY = 1111
        private const val MAX_CAPACITY_PREVIOUS = 100_000

        /**
         * parameters for the lists to store the latest game states
         */
        private const val CHUNK_SIZE = 64
        private const val MAX_CAPACITY_LATEST = 50_000
    }
}