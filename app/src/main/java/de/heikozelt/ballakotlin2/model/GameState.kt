package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlin.collections.List
import kotlin.random.Random

/**
 * Represents the State of the game.
 * All balls are placed in tubes.
 */
class GameState(val numberOfColors: Int, var numberOfExtraTubes: Int, val tubeHeight: Int ) {

    var numberOfTubes = numberOfColors + numberOfExtraTubes
    var tubes = MutableList(numberOfTubes){Tube(tubeHeight)}
    var moveLog = mutableListOf<Move>()
    private var jury: NeutralJury? = null

    init {
        Log.i(TAG, "init")
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
     * 1 2 3 0 0
     * 1 2 3 0 0
     * 1 2 3 0 0
     */
    fun initTubes() {
        Log.i(TAG, "initTubes()")
        // tubes filled with balls of same color
        for(i in 0 until numberOfColors) {
            val initialColor = i + 1
            tubes[i] = Tube(tubeHeight)
            tubes[i].fillWithOneColor(initialColor)
        }
        for(i in numberOfColors until numberOfTubes) {
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
        mixTubes()
        // clear undo log
        moveLog = mutableListOf<Move>()
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
    fun mixTubes() {
        Log.i(TAG, "mixTubes()")
        for(c in 0 until (numberOfTubes * 3)) {
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
        if(move.to != move.from) {
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

    // kompliziertes Regelwerk
    fun isMoveAllowed(from: Int, to: Int): Boolean {
        Log.i(TAG, "isMoveAllowd(from=${from}, to=${to})")

        // kann keinen Ball aus leerer Röhre nehmen
        if(tubes[from].isEmpty()) {
            return false
        }
        // sonst geht's immer, wenn Quelle und Ziel gleich sind
        if(to == from) {
            return true
        }
        // Ziel-Tube ist voll
        if(tubes[to].isFull()) {
            return false
        }
        // oberster Ball hat selbe Farbe oder Ziel-Röhre ist leer
        if(tubes[to].isEmpty() || isSameColor(from, to)) {
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
     */
    fun allPossibleBackwardMoves(lastMove: Move?): List<Move> {
        val allMoves = mutableListOf<Move>()
        for(from in 0 until numberOfTubes) {
            if(tubes[from].isReverseDonorCandidate()) {
                for(to in 0 until numberOfTubes) {
                    if(tubes[to].isReverseReceiverCandidate()) {
                        if(from != to) {
                            val move = Move(from, to)
                            if(move.backwards() != lastMove) {
                                allMoves.add(move)
                            }
                        }
                    }
                }
            }
        }
        return allMoves
    }

    fun multiPush(a: MutableList<Move>, m: Move, number: Int) {
        for (i in 0 until number) {
            a.add(m)
        }
    }

    /**
     * Bewertet alle Rückwärts-Züge und gibt entsprechende Anzahl Lose in die Urne.
     */
    fun lottery(allMoves: List<Move>): List<Move> {
        val lots = mutableListOf<Move>()
        val j = jury
        if(j != null) {
            for (move in allMoves) {
                val rate = j.rateBackwardMove(move)
                //Log.i("rate: ${rate}")
                multiPush(lots, move, rate)
            }
        }
        return lots
    }

    /**
     * plays game backwards with many moves
     */
    fun randomizeBalls() {
        var lastMove: Move? = null
        val maxMoves = numberOfTubes * tubeHeight * 3
        var i = 0
        do {
            val possibleMoves = allPossibleBackwardMoves(lastMove)
            //Log.i(_TAG, "i: ${i}, possibleMoves: ${Gson().toJson(possibleMoves)}")
            if (possibleMoves.isEmpty()) {
                break
            }
            val lotty = lottery(possibleMoves)
            if (lotty.isEmpty()) {
                break
            }
            //Log.i(_TAG, "{i}: , lottery: ${Gson().toJson(lotty)}")
            val move = lotty.random()
            Log.i(TAG,"=======> selected move: ${move.from} --> ${move.to}")
            moveBall(move)
            lastMove = move
            i++
        } while(i < maxMoves)
        Log.i(TAG, "randomize finished with number of backward moves: ${i}")
    }

    companion object {
        private const val TAG = "balla.GameState"
    }
}