package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlin.collections.List
import kotlin.random.Random

/**
 * Represents the State of the game.
 * All balls are placed in tubes.
 */
class GameState(val numberOfColors: Int, var numberOfExtraTubes: Int, val tubeHeight: Int) {

    var numberOfTubes = numberOfColors + numberOfExtraTubes
    var tubes = MutableList(numberOfTubes) { Tube(tubeHeight) }
    var moveLog = mutableListOf<Move>()
    private var jury: NeutralJury? = null

    init {
        Log.i(
            TAG,
            "init: numberOfColors: ${numberOfColors}, numberOfExtraTubes: ${numberOfExtraTubes}, tubeHeight: $tubeHeight"
        )
    }

    /**
    fun toJson(): String {
    Json
    return Gson().toJson(this)
    }
     */

    /**
     * Füllt die Röhren mit Bällen, wie wenn Spiel fertig ist.
     * Beispiel:
     * <pre>
     * 1 2 3 0 0
     * 1 2 3 0 0
     * 1 2 3 0 0
     * </pre>
     */
    fun initTubes() {
        Log.i(TAG, "initTubes()")
        // tubes filled with balls of same color
        for (i in 0 until numberOfColors) {
            val initialColor = i + 1
            tubes[i] = Tube(tubeHeight)
            tubes[i].fillWithOneColor(initialColor)
        }
        for (i in numberOfColors until numberOfTubes) {
            tubes[i] = Tube(tubeHeight)
            // empty tube, no color (is default after Array creation).
        }
    }

    fun newGame() {
        Log.i(TAG, "newGame()")
        initTubes()
        setJury(AnotherJury(this))
        //setJury(AdvancedJury(this))
        //setJury(NeutralJury(this))
        randomizeBalls()
        shuffleTubes()
        // clear undo log
        moveLog = mutableListOf()
    }

    fun clone(): GameState {
        Log.i(TAG, "clone()")
        val miniMe = GameState(numberOfColors, numberOfExtraTubes, tubeHeight)
        for (i in 0 until numberOfTubes) {
            miniMe.tubes[i] = tubes[i].clone()
        }
        // Die einzelnen Elemente sind die gleichen, wie beim Original.
        // Da Einträge in einem Log aber normalerweise nicht nachträglich geändert werden, ist das kein Problem.
        miniMe.moveLog.addAll(moveLog)
        return miniMe
    }

    /**
     * Setter-Injection
     */
    fun setJury(j: NeutralJury) {
        jury = j
    }

    /**
     * adds an extra tube,
     * which makes solving the puzzle much easier
     */
    fun cheat() {
        tubes.add(Tube(tubeHeight))
        numberOfTubes++
        numberOfExtraTubes++
    }

    /**
     * liefert true, wenn das Spiel beendet / das Puzzle gelöst ist,
     * also jede Röhren entweder leer oder gelöst ist.
     */
    fun isSolved(): Boolean {
        for (i in 0 until numberOfTubes) {
            //console.debug('i=' + i)
            if (!(tubes[i].isEmpty() || tubes[i].isSolved())) {
                //console.debug('tube ' + i + ' is not empty or solved')
                return false
            }
            //console.debug('tube ' + i + ' is not solved');
        }
        //console.debug('puzzle is solved');
        return true
    }

    /**
     * vertauscht Röhren untereinander zufällig
     */
    private fun shuffleTubes() {
        Log.i(TAG, "mixTubes()")
        for (c in 0 until (numberOfTubes * 3)) {
            val i = Random.nextInt(numberOfTubes)
            val j = Random.nextInt(numberOfTubes)
            swapTubes(i, j)
        }
    }

    /**
     * tauscht 2 Röhren
     */
    fun swapTubes(index1: Int, index2: Int) {
        val tmp = tubes[index1]
        tubes[index1] = tubes[index2]
        tubes[index2] = tmp
    }

    /**
     * moves a ball from one tube to another
     * (the tubes may be the same, but that doesn't make much sense)
     */
    fun moveBall(move: Move) {
        //Log.i("moveBall(${Gson().toJson(move)}")
        val color = tubes[move.from].removeBall()
        tubes[move.to].addBall(color)
    }

    /**
     * moves a ball and logs for possible undo operation
     */
    fun moveBallAndLog(move: Move) {
        // Es ist kein echter Spielzug,
        // wenn Quelle und Ziel gleich sind.
        if (move.to != move.from) {
            moveBall(move)
            moveLog.add(move)
            //Log.i("moveLog: ${Gson.toJson(moveLog)}")
        }
    }

    /**
     * undoes last move, according to log
     */
    fun undoLastMove(): Move {
        //val forwardMove = moveLog.removeLast()
        val forwardMove = moveLog.removeAt(moveLog.size - 1)
        val backwardMove = forwardMove.backwards()
        moveBall(backwardMove)
        return backwardMove
    }

    /**
     * gets last move. trivial.

    fun getLastMove(): Move? {
    return moveLog.last()
    }
     */

    /**
     * Testet, ob ein Spielzug erlaubt ist.
     * kompliziertes Regelwerk
     */
    fun isMoveAllowed(from: Int, to: Int): Boolean {
        Log.i(TAG, "isMoveAllowd(from=${from}, to=${to})")

        // kann keinen Ball aus leerer Röhre nehmen
        if (tubes[from].isEmpty()) {
            return false
        }
        // wenn Quelle und Ziel gleich sind, geht's immer.
        // sinnloser Zug.
        if (to == from) {
            return true
        }
        // Ziel-Tube ist voll
        if (tubes[to].isFull()) {
            return false
        }
        // oberster Ball hat selbe Farbe oder Ziel-Röhre ist leer
        if (tubes[to].isEmpty() || isSameColor(from, to)) {
            return true
        }
        return false
    }

    /**
     * Testet, ob die beiden oberen Kugeln, die gleiche Farbe haben.
     */
    fun isSameColor(index1: Int, index2: Int): Boolean {
        val color1 = tubes[index1].colorOfTopmostBall()
        val color2 = tubes[index2].colorOfTopmostBall()
        return color1 == color2
    }

    /**
     * liefert eine Liste mit allen möglichen Zügen,
     * wenn das Spiel rückwärts gespielt wird.
     * ausgenommen ist der letzte Zug rückwärts (hin und her macht wenig Sinn)
     * und ausgenommen ist vom Boden einer Röhre zum Boden einer anderen Röhre
     */
    //fun allPossibleBackwardMoves(lastMove: Move?): List<Move> {
    fun allPossibleBackwardMoves(): List<Move> {
        val allMoves = mutableListOf<Move>()
        for (from in 0 until numberOfTubes) {
            if (tubes[from].isReverseDonorCandidate()) {
                for (to in 0 until numberOfTubes) {
                    if (tubes[to].isReverseReceiverCandidate()) {
                        if (from != to) {
                            val move = Move(from, to)
                            // nicht hin-und-her und
                            val isBackAndForth = if (moveLog.isEmpty()) {
                                false
                            } else {
                                move.backwards() == moveLog.last()
                            }
                            // nicht von Boden zu Boden
                            val isGroundMove = (tubes[from].fillLevel == 1) and tubes[to].isEmpty()
                            if (!(isBackAndForth || isGroundMove)) {
                                allMoves.add(move)
                            }
                        }
                    }
                }
            }
        }
        return allMoves
    }

    /**
     * "static" method
     * Ein Move wird mehrmals einer Liste hinzugefügt.
     * @param a Liste
     * @param m Move
     * @param number Anzahl der Referenzen, die der Liste hinzugefügt werden.
     */
    private fun multiPush(a: MutableList<Move>, m: Move, number: Int) {
        for (i in 0 until number) {
            a.add(m)
        }
    }

    /*
     * Bewertet alle Rückwärts-Züge und gibt entsprechende Anzahl Lose in die Urne.
    fun lottery(allMoves: List<Move>): List<Move> {
        val goodLots = mutableListOf<Move>()
        val badLots = mutableListOf<Move>()
        val j = jury
        if (j != null) {
            for (move in allMoves) {
                val rate = j.rateBackwardMove(move)
                //Log.i("rate: ${rate}")
                if (rate == 0) {
                    multiPush(badLots, move, 1)
                } else {
                    multiPush(goodLots, move, rate)
                }
            }
        }
        return lots
    }
     */

    /*
     * Berechnet den Durchschitt einer Liste ganzer Zahlen
     * @return Durchschnitt
     * Methode gibt es schon
    fun calculateAverage(ints: List<Int>): Float {
        var sum = 0
        for(rate in ints) {
            sum += rate
        }
        return sum / ints.size.toFloat()
    }
     */

    /**
     * Prueft, ob alle ganze Zahlen in einer Liste gleich sind.
     * In Java waere es eine static-Methode.
     * In Kotlin koennte man sie als Package-Methode oder als Companion-Objekt-Methode implementieren.
     * Beides erzeugt im Byte-Code zusaetzliche Java-Klassen.
     * @return true, wenn alle gleich sind
     */
    fun areEqual(ints: List<Int>): Boolean {
        val firstRate = ints[0]
        for (rate in ints) {
            if (rate != firstRate) {
                return false
            }
        }
        return true
    }

    /**
     * plays game backwards with many moves
     */
    private fun randomizeBalls() {
        //var lastMove: Move? = null
        val maxMoves = numberOfTubes * tubeHeight * 3
        var executedMoveIndex = 0
        do {
            dump()
            //val possibleMoves = allPossibleBackwardMoves(lastMove)
            val possibleMoves = allPossibleBackwardMoves()
            //Log.i(_TAG, "i: ${i}, possibleMoves: ${Gson().toJson(possibleMoves)}")

            // Kein Zug mehr möglich ist ein Abbruch-Kriterium
            if (possibleMoves.isEmpty()) {
                break
            }

            // Alle möglichen Züge bewerten lassen und Bewertungen merken
            val ratings = mutableListOf<Int>()
            val j = jury
            if (j != null) {
                for (move in possibleMoves) {
                    ratings.add(j.rateBackwardMove(move))
                }
            }

            var candidateMoves: List<Move>
            var candidateRates: List<Int>
            // wurden alle möglichen Züge gleich bewertet?
            if (areEqual(ratings)) { // ja, alle gleich, z.B. am Anfang
                // alle gleich berücksichtigen
                candidateMoves = possibleMoves
                candidateRates = ratings
            } else { // nein, unterschiedlich
                val average = ratings.average()
                Log.d(TAG, "average rating=%2.2f".format(average))
                // Nur die Züge auswählen, die überdurchschnittlich sind
                candidateMoves = mutableListOf()
                candidateRates = mutableListOf()
                for (i in possibleMoves.indices) {
                    if (ratings[i] > average) {
                        //Log.d(TAG, "#${i} ${possibleMoves[i].from}-->${possibleMoves[i].to} rating=${ratings[i]} add")
                        candidateMoves.add(possibleMoves[i])
                        candidateRates.add(ratings[i])
                        //} else {
                        //    Log.d(TAG, "#${i} ${possibleMoves[i].from}-->${possibleMoves[i].to} rating=${ratings[i]} ignore")
                    }
                }
            }

            // Die Anzahl Lose richtet sich nach der jeweiligen Bewertung
            val lots = mutableListOf<Move>()
            for (i in candidateMoves.indices) {
                multiPush(lots, candidateMoves[i], candidateRates[i])
            }

            val selectedMove = lots.random()
            Log.i(
                TAG,
                "round #%3d: num candidates=%d ==> selected move %2d -> %2d".format(
                    executedMoveIndex,
                    candidateMoves.size,
                    selectedMove.from,
                    selectedMove.to
                )
            )
            moveBallAndLog(selectedMove)
            //lastMove = selectedMove
            executedMoveIndex++
        } while (executedMoveIndex < maxMoves)
        // log wieder leeren, bevor das Spiel beginnt
        moveLog = mutableListOf()
        Log.i(TAG, "randomize finished with number of backward moves: $executedMoveIndex")
    }

    /**
     * logt aktuellen Spielstatus
     * Beispiel:
     * 1 2 3 _ _
     * 1 2 3 _ _
     * 1 2 3 _ _
     */
    fun dump() {
        for (row in (tubeHeight - 1) downTo 0) {
            var line = colorToString(tubes[0].cells[row])
            for (col in 1 until numberOfTubes) {
                val color = tubes[col].cells[row]
                line += " ${colorToString(color)}"
            }
            Log.d(TAG, line)
        }
    }

    /**
     * wandelt 0 in " _", 1 in " 1", 2 in " 2", ... 15 in "15"
     */
    private fun colorToString(color: Int): String {
        return if (color == 0) {
            " _"
        } else {
            "%2d".format(color)
        }
    }

    companion object {
        private const val TAG = "balla.GameState"
    }
}