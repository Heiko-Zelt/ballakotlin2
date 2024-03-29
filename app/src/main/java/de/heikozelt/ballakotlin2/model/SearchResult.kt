package de.heikozelt.ballakotlin2.model

/**
 * Ergebnis von GameState.searchSolution()
 */
class SearchResult {
    /**
     * type of result
     */
    var status: Byte = STATUS_UNDEFINED

    /**
     * number of branches followed to find solution
     */
    //var brachesFollowed = 0

    /**
     * number of branches not followed because maximum recursion depth was exceeded
     */
    //var openEnds = 0

    /**
     * next move to solution, only if solution found and not already solved.
     */
    var move: Move? = null

    companion object {
        /**
         * not initialized yet
         */
        const val STATUS_UNDEFINED = 0.toByte()

        /**
         * don't know if solvable
         */
        const val STATUS_OPEN = 1.toByte()

        /**
         * solution found
         */
        const val STATUS_FOUND_SOLUTION = 2.toByte()

        /**
         * not solvable, dead end/cul-de-sac, press undo or reset!
         */
        const val STATUS_UNSOLVABLE = 3.toByte()

        /**
         * already solved, no more move possible
         */
        const val STATUS_ALREADY_SOLVED = 4.toByte()

        /**
         * Job was canceled
         */
        const val STATUS_CANCELED = 5.toByte()
    }
}