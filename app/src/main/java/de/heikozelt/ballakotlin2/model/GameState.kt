package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlin.collections.List
import kotlin.experimental.and
import kotlin.random.Random

/**
 * Represents the state of the game.
 * All balls are placed in tubes.
 * The standard contructor creates a zero x zero matrix.
 * Other constructors(?) & methods create a useful game state.
 */
class GameState {
    var numberOfColors = 0
    var numberOfTubes = 0
    var numberOfExtraTubes = 0
    var tubeHeight = 0

    //var tubes = MutableList(numberOfTubes) { Tube(0) }
    var tubes = arrayOf<Tube>()

    /**
     * Operationen:
     * constructor / initialisieren
     * leeren
     * klonen / addAll()
     * push / add()
     * pop / removeLast() / removeAt(moveLog.size - 1)
     * isNotEmpty()
     * last()
     * size()
     */
    val moveLog = Moves()
    private var jury: NeutralJury? = null

    init {
        Log.i(TAG, "GameState primary constructor")
    }

    /**
     * Führt eine Liste von Zügen aus.
     * Einsatz zum Beispiel als redo-Operation.
     */
    fun applyMoves(moves: Moves) {
        for(m in moves) {
            if(m.from == 99) {
                cheat()
            } else {
                moveBall(m)
            }
        }
        moveLog.fromList(moves.asMutableList())
    }

    fun applyMoves(ascii: String) {
        val moves = Moves()
        moves.fromAscii(ascii)
        applyMoves(moves)
    }

    /**
     * erzeugt ein leeres Spielfeld in den angegebenen Dimensionen
     */
    fun resize(numberOfColors: Int, numberOfExtraTubes: Int, tubeHeight: Int) {
        Log.i(TAG, "GameState secondary constructor")
        this.numberOfColors = numberOfColors
        this.numberOfExtraTubes = numberOfExtraTubes
        this.numberOfTubes = numberOfColors + numberOfExtraTubes
        this.tubeHeight = tubeHeight
        tubes = Array(numberOfTubes) { Tube(tubeHeight) }
        moveLog.clear()
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
     * 1 2 3 _ _
     * 1 2 3 _ _
     * 1 2 3 _ _
     * </pre>
     */
    fun rainbow() {
        Log.i(TAG, "initTubes()")
        // tubes filled with balls of same color
        for (i in 0 until numberOfColors) {
            val initialColor = (i + 1).toByte()
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
        rainbow()
        setJury(AnotherJury(this))
        //setJury(AdvancedJury(this))
        //setJury(NeutralJury(this))
        randomizeBalls()
        shuffleTubes()
        // clear undo log
        moveLog.clear()
    }

    fun cloneWithoutLog(): GameState {
        Log.i(TAG, "cloneWithoutLog()")
        val miniMe = GameState()
        miniMe.resize(numberOfColors, numberOfExtraTubes, tubeHeight)
        for (i in 0 until numberOfTubes) {
            miniMe.tubes[i] = tubes[i].clone()
        }
        return miniMe
    }

    /*
     Method is never used
    fun cloneWithLog(): GameState {
        Log.i(TAG, "cloneWithLog()")
        val miniMe = cloneWithoutLog()
        // Die einzelnen Elemente sind die gleichen, wie beim Original. Kein Deep Clone.
        // Da Einträge in einem Log aber normalerweise nicht nachträglich geändert werden, ist das kein Problem.
        miniMe.moveLog.addAll(moveLog)
        return miniMe
    }
    */

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
        numberOfExtraTubes++
        numberOfTubes++
        val tmp = Array(numberOfTubes) { Tube(tubeHeight) }
        tubes.copyInto(tmp)
        tubes = tmp
        moveLog.push(Move(99,0))
    }

    /**
     * liefert true, wenn das Spiel beendet / das Puzzle gelöst ist,
     * also jede Röhren entweder leer oder gelöst ist.
     */
    fun isSolved(): Boolean {
        for (tube in tubes) {
            //console.debug('i=' + i)
            if (!(tube.isEmpty() || tube.isSolved())) {
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
     * moves a ball from one tube to another (without logging)
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
            moveLog.push(move)
            //Log.i("moveLog: ${Gson.toJson(moveLog)}")
        }
    }

    /**
     * undoes last move, according to log
     * todo: weniger heap/garbage collection overhead
     */
    fun undoLastMove(): Move {
        //val forwardMove = moveLog.removeLast()
        val forwardMove = moveLog.pop()
        val backwardMove = forwardMove.backwards()
        moveBall(backwardMove)
        return backwardMove
    }

    fun undoCheat() {
        if(tubes[numberOfTubes - 1].isEmpty()) {
            numberOfExtraTubes--
            numberOfTubes--
            //val tmp = Array(numberOfTubes) { Tube(tubeHeight) }
            tubes = tubes.copyOfRange(0, numberOfTubes)
        } else {
            Log.e(TAG,"Can't undo cheat. Last tube is not empty!")
        }
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
        //Log.i(TAG, "isMoveAllowed(from=${from}, to=${to})")

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
     * wie isMoveAllowed() jedoch ohne
     * - Quell-Röhre und Ziel-Röhre identisch
     * - letzten Zug rückwärts (einfacher hin- und her-Zyklus) = Sonderform des Kettenzuges
     * - Kettenzüge, Beispiel: 1 -> 2, 2 -> 3, sinnvoller ist direkt 1 -> 3
     * - Zug von einfarbiger Röhre in leere Röhre
     * - bei 2 einfarbige Röhren, Zug von Röhre mit niedrigerem Füllstand in Röhre mit höherem Füllstand
     * todo: alle oder keiner
     * todo: bei 2 Röhren, die unten gleich sind, aber oben unterschiedlich viele Bälle der gleichen Farbe haben, von niedrigerem zu höherem Füllstand.
     */
    fun isMoveUseful(from: Int, to: Int): Boolean {
        //Log.d(TAG, "isMoveUseful(from=${from}, to=${to})")

        // kann keinen Ball aus leerer Röhre nehmen
        if (tubes[from].isEmpty()) {
            return false
        }

        // Ziel-Tube ist voll
        if (tubes[to].isFull()) {
            return false
        }

        // selbe Röhre, Unsinn
        if (to == from) {
            return false
        }

        // oberster Ball hat nicht selbe Farbe und Ziel-Röhre ist nicht leer
        if (!tubes[to].isEmpty() && !isSameColor(from, to)) {
            return false
        }

        // von einer einfarbigen Röhre zu leerer Röhre
        // einfachstes Beispiel:
        // _ _    _ _
        // 1 _ => _ 1
        // komplexeres Beispiel:
        // _ _    _ _
        // 1 _    _ _
        // 1 _ => 1 1
        // (beinhaltet von einer gelösten Röhre in irgendeine andere (also leere) Röhre. tubes[from].isSolved())
        if ((tubes[from].unicolor() != 0) && tubes[to].isEmpty()) {
            return false
        }

        // nicht erlaubt, weil es zu unnötig vielen Zügen führt:
        // bei 2 einfarbige Röhren, Zug von Röhre mit höherem Füllstand in Röhre mit niedrigeren Füllstand.
        // _ _    _ _
        // 1 _ => _ 1
        // 1 1    1 1
        if (
            !tubes[from].isEmpty() && !tubes[to].isEmpty() &&
            (tubes[from].cells[0] == tubes[to].cells[0]) &&
            (tubes[from].unicolor() > tubes[to].unicolor())
        ) {
            return false
        }

        if (moveLog.isNotEmpty()) {
            // Kettenzug Beispiel: 1 -> 2, 2 -> 3, sinnvoller ist direkt 1 -> 3
            // (Spezialfall hin und zurück ist inbegriffen)
            if (from == moveLog.last().to) {
                return false
            }

            // Ball wegnehmen und statt dessen anderen gleichfarbigen Ball hinlegen
            // Beispiel: 2 -> 3 (rot), 1 -> 2 (rot)
            // (Spezialfall hin und zurück ist auch hier inbegriffen)
            if ((to == moveLog.last().from) && (tubes[moveLog.last().to].colorOfTopmostBall() == tubes[from].colorOfTopmostBall())) {
                return false
            }
        }

        // all or none
        /*
        Log.d(
            TAG,
            "empty=${tubes[to].isEmpty()}, topUnicolorRemovable=${topUnicolorRemovable(from)}"
        )
        */
        if ((!tubes[to].isEmpty()) && (!topUnicolorRemovable(from))) {
            return false
        }

        return true
    }

    /**
     * Ermittelt, ob die oberen Bälle, der gleichen Farbe auf andere (nicht leere) Röhren verteilt werden können.
     * @param from ist Index einer nicht leeren Röhre
     */
    fun topUnicolorRemovable(from: Int): Boolean {
        //Log.d(TAG, "topUnicolorRemovable(from=$from)")
        val color = tubes[from].colorOfTopmostBall()
        var available = 0
        val needed = tubes[from].countTopBallsWithSameColor()
        for (i in tubes.indices) {
            val tube = tubes[i]
            if ((i != from) && !tube.isEmpty() && (tube.colorOfTopmostBall() == color)) {
                //Log.d(TAG, "#$i: freeCells=${tube.freeCells()}")
                available += tube.freeCells()
            }
        }
        //Log.d(TAG, "available=$available, needed=$needed")
        return available >= needed
    }

    /* never used
    fun allPossibleMoves(): List<Move> {
        val moves = mutableListOf<Move>()
        for (from in 0 until numberOfTubes) {
            for (to in 0 until numberOfTubes) {
                if (isMoveAllowed(from, to)) {
                    moves.add(Move(from, to))
                }
            }
        }
        return moves
    }
    */

    /**
     * Liefert eine Menge an Röhren ohne Duplikate.
     * Aber nicht die Röhren selber, sondern Indexe.
     * Beispiel:
     * _ 2 _ _ _ _ 2 _    _ 2 _ _ 2 _
     * _ 3 2 _ _ _ 3 4 => _ 3 2 _ 3 4
     * 1 1 4 _ 1 _ 3 4    1 1 4 _ 3 4
     * ---------------    -----------
     * 0 1 2 3 4 5 6 7    0 1 2 3 6 7
     * Röhre 4 ist Duplikat von 0 und Röhre 5 ist Duplikat von 3.
     */
    fun tubesSet(): MutableList<Int> {
        val result = mutableListOf<Int>()
        for (candidate in 0 until tubes.size) {
            var dup = false
            for (other in 0 until candidate) {
                if (tubes[candidate].contentEquals(tubes[other])) {
                    dup = true
                }
            }
            if (!dup) {
                result.add(candidate)
            }
        }
        return result
    }

    /*
     * Liefert eine Menge an Röhren (Indexe), die geeignet sind.
     * - ohne Dupletten
     * - ohne leere Röhren
     * - ohne gelöste Röhren
    fun usefulSourceTubes(): List<Int> {
        val tSet = tubesSet()
        val result = mutableListOf<Int>()
        for (i in tSet) {
            if (!tubes[i].isEmpty() && !tubes[i].isSolved()) {
                result.add(i)
            }
        }
        return result
    }
    */

    /**
     * liefert eine Menge an Röhren (Indexe).
     * - mit Dupletten
     * - ohne volle Röhren
     * mit Dupletten, wegen diesem Fall:
     * 1 _ _
     * 1 2 2
     * Optimierungs-Potential, aber zu kompliziert:
     * - Tripletten koennten ausgeschlossen werden.
     * - Nur Dupletten zulassen, wenn nicht voll.
     */
    fun usefulTargetTubes(): List<Int> {
        val result = mutableListOf<Int>()
        for (i in tubes.indices) {
            if (!tubes[i].isFull()) {
                result.add(i)
            }
        }
        return result
    }

    /**
     * Beispiel 1: Quell-Röhren der beiden möglichen Züge sind Inhalts-gleich
     * 1 1 _
     * 2 2 _
     * input: 0->2, 1->2
     * output: 0->2
     *
     * Beispiel 2: Ziel-Röhren der beiden möglichen Züge sind Inhalts-gleich
     * 1 _ _
     * 1 _ _
     * input: 0->1, 0->2
     * output: 0->1
     */
    fun contentDistinctMoves(moves: MutableList<Move>): MutableList<Move> {
        // alle Züge miteinander vergleichen
        // also jeden Zug mit Zügen, die vorher in der Liste stehen
        val result = mutableListOf<Move>()

        for (candidateIndex in 0 until moves.size) {
            val candidate = moves[candidateIndex]
            var dup = false
            for (otherIndex in 0 until candidateIndex) {
                val other = moves[otherIndex]
                //Log.d(TAG,"compare $candidateIndex with $otherIndex, ${candidate.toAscii()} with ${other.toAscii()}")
                if (tubes[candidate.from].contentEquals(tubes[other.from]) && tubes[candidate.to].contentEquals(
                        tubes[other.to]
                    )
                ) {
                    //Log.d(TAG, "#$candidateIndex, ${candidate.toAscii()} is duplicate")
                    dup = true
                    break
                }
            }
            if (!dup) {
                //Log.d(TAG, "#$candidateIndex, ${candidate.toAscii()} is unique ")
                result.add(candidate)
            }
        }
        return result
    }

    /**
     * Beispiel 1: Quell-Röhren der beiden möglichen Züge sind Inhalts-gleich
     * 1 1 _
     * 2 2 _
     * input: 0->2, 1->2
     * output: 0->2
     *
     * Beispiel 2: Ziel-Röhren der beiden möglichen Züge sind Inhalts-gleich
     * 1 _ _
     * 1 _ _
     * input: 0->1, 0->2
     * output: 0->1
     */
    fun contentDistinctMovesBang(moves: MutableList<Move>) {
        // alle Züge miteinander vergleichen
        // also jeden Zug mit Zügen, die vorher in der Liste stehen

        var candidateIndex = 0
        outerLoop@ while (candidateIndex < moves.size) {
            val candidate = moves[candidateIndex]
            for (otherIndex in 0 until candidateIndex) {
                val other = moves[otherIndex]
                //Log.d(TAG,"compare $candidateIndex with $otherIndex, ${candidate.toAscii()} with ${other.toAscii()}")
                if (tubes[candidate.from].contentEquals(tubes[other.from]) && tubes[candidate.to].contentEquals(
                        tubes[other.to]
                    )
                ) {
                    moves.removeAt(candidateIndex)
                    continue@outerLoop
                }
            }
            candidateIndex++
        }
    }

    /**
     * @returns true wenn Ziel keine einfarbiger Stapel ist oder einfarbig und der höchste Stapel.
     * Bei gleichen Höhen wird auch true zurückgeliefert.
     */
    fun isDifferentColoredOrUnicolorAndHighest(column: Int, color: Byte): Boolean {
        val t = tubes[column]
        if(!t.isUnicolorOrEmpty()) {
            return true
        }
        val height = t.unicolor()
        for(other in 0 until numberOfTubes) {
            if(tubes[other].cells[0] == color) {
                if(tubes[other].unicolor() > height) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Liefert eine Liste mit Zügen, die als nächster Zug sinnvoll erscheinen.
     * Das sind alle möglichen Züge außer:
     * - inhaltsgleiche Züge
     * - Zug von einfarbiger Röhre in leere Röhre
     * todo: Kettenzüge, Beispiel: 1 -> 2, 2 -> 3, sinnvoller ist direkt 1 -> 3
     * todo: unabhängige Züge in verschiedenen Reihenfolgen, Beispiel: 1 -> 2, 3 -> 4 = 3 -> 4, 1 -> 2
     * todo: Kettenzüge mit unabhängigen Zügen dazwischen. Beispiel: 1 -> 2, 7 -> 8, 2 -> 3
     */
    fun allUsefulMoves(): List<Move> {
        val moves = mutableListOf<Move>()

        // Wenn ein Ball aus einer Röhre genommen wurde und es befinden sich gleichfarbige Bälle darunter,
        // dann erst Mal alle entnehmen, bevor mit ganz anderen Zügen fortgesetzt wird.
        if(!moveLog.isEmpty()) {
            val from = moveLog.last().from
            if (!tubes[from].isEmpty()) {
                val lastColor = tubes[moveLog.last().to].colorOfTopmostBall()
                // noch ein Ball mit gleicher Farbe in Quell-Röhre
                if (tubes[from].colorOfTopmostBall() == lastColor) {
                    // alle möglichen Ziele finden
                    for (to in tubes.indices) {
                        if ((to != from) && tubes[to].canTake(lastColor)) {
                            if(isDifferentColoredOrUnicolorAndHighest(to, lastColor)) {
                                moves.add(Move(from, to))
                            }
                        }
                    }
                    return moves
                }
            }
        }

        //val sourceTubes = usefulSourceTubes() unnötig
        val targetTubes = usefulTargetTubes()
        //for (from in sourceTubes) {
        for (from in tubes.indices) {
            for (to in targetTubes) {
                //for (to in tubes.indices) {
                if (isMoveUseful(from, to)) {
                    val m = Move(from, to)
                    //Log.d(TAG, "add ${m.toAscii()}")
                    if(isDifferentColoredOrUnicolorAndHighest(to, tubes[from].colorOfTopmostBall())) {
                        moves.add(m)
                    }
                }
            }
        }
        contentDistinctMovesBang(moves)
        return moves
    }

    /**
     * Liefert eine Liste mit Zügen, die als nächster Zug sinnvoll erscheinen.
     * Das sind alle möglichen Züge außer:
     * - inhaltsgleiche Züge
     * - Zug von einfarbiger Röhre in leere Röhre
     * todo: Kettenzüge, Beispiel: 1 -> 2, 2 -> 3, sinnvoller ist direkt 1 -> 3
     * todo: unabhängige Züge in verschiedenen Reihenfolgen, Beispiel: 1 -> 2, 3 -> 4 = 3 -> 4, 1 -> 2
     * todo: Kettenzüge mit unabhängigen Zügen dazwischen. Beispiel: 1 -> 2, 7 -> 8, 2 -> 3
     */
    fun allUsefulMovesIntegrated(): List<Move> {
        val moves = mutableListOf<Move>()

        // Wenn ein Ball aus einer Röhre genommen wurde und es befinden sich gleichfarbige Bälle darunter,
        // dann erst Mal alle entnehmen, bevor mit ganz anderen Zügen fortgesetzt wird.
        if(!moveLog.isEmpty()) {
            val from = moveLog.last().from
            if (!tubes[from].isEmpty()) {
                val lastColor = tubes[moveLog.last().to].colorOfTopmostBall()
                // noch ein Ball mit gleicher Farbe in Quell-Röhre
                if (tubes[from].colorOfTopmostBall() == lastColor) {
                    // alle möglichen Ziele finden
                    for (to in tubes.indices) {
                        if ((to != from) && tubes[to].canTake(lastColor)) {
                            if(isDifferentColoredOrUnicolorAndHighest(to, lastColor)) {
                                moves.add(Move(from, to))
                            }
                        }
                    }
                    return moves
                }
            }
        }

        //val sourceTubes = usefulSourceTubes() unnötig
        val targetTubes = usefulTargetTubes()
        //for (from in sourceTubes) {
        for (from in tubes.indices) {
            //for (to in tubes.indices) {
            toLoop@ for (to in targetTubes) {
                if (isMoveUseful(from, to)) {
                    //Log.d(TAG, "add ${m.toAscii()}")
                    for (other in moves) {
                        // inhaltsgleicher Zug?
                        if (tubes[from].contentEquals(tubes[other.from]) && tubes[to].contentEquals(
                                tubes[other.to]
                            )
                        ) {
                            // inhaltsgleicher Zug!
                            continue@toLoop
                        }
                    }
                    // kein inhaltsgleicher Zug
                    if(isDifferentColoredOrUnicolorAndHighest(to, tubes[from].colorOfTopmostBall())) {
                        moves.add(Move(from, to))
                    }
                }
            }
        }
        return moves
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
        moveLog.clear()
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
        Log.d(TAG, "\n${toAscii()}")
    }


    /*
     * Rekursion
     * @param maxRecursionDepth gibt an, wieviele Züge maximal ausprobiert werden.
     * todo: mehr Infos im Ergebnis. Wie tief wurde gesucht? Wie viele offene Pfade gibt es?
     * todo: Zyklen erkennen
     * todo: Spezialfall: Wenn eine Röhre mit wenigen Zügen(?) gefüllt werden kann, dann diesen Zug bevorzugen. Wo liegt die Grenze?

    suspend fun findSolutionNoBackAndForth_backup(
        maxRecursionDepth: Int,
        previousGameStates: MutableList<Array<Byte>> = mutableListOf(Array(numberOfTubes * tubeHeight) { 0.toByte() })
    ): SearchResult {
        // Job canceled?
        // nicht zu oft Kontrolle abgeben.
        if(maxRecursionDepth > 5) {
            yield()
        }

        if (isSolved()) {
            //Log.d(TAG,"1. Abbruchkriterium: Lösung gefunden")
            val resultFound = SearchResult()
            resultFound.status = SearchResult.STATUS_FOUND_SOLUTION
            return resultFound
        } else if (maxRecursionDepth == 0) {
            //Log.d(TAG, "2. Abbruchkriterium: Maximale Rekursionstiefe erreicht")
            val resultCancel = SearchResult()
            resultCancel.status = SearchResult.STATUS_OPEN
            return resultCancel
        }
        val maxRecursion = maxRecursionDepth - 1
        val moves = allUsefulMoves()

        if (moves.isEmpty()) {
            //Log.d(TAG,"3. Abbruchkriterium: keine Züge mehr möglich")
            val resultUnsolvable = SearchResult()
            resultUnsolvable.status = SearchResult.STATUS_UNSOLVABLE
            return resultUnsolvable
        }
        val results = mutableListOf<SearchResult>()
        for (move in moves) {
            var result: SearchResult?
            //Log.d(TAG, "Rekursion")
            moveBallAndLog(move)
            val newGameState = Array(numberOfTubes * tubeHeight) { 0.toByte() }
            toBytes(newGameState)
            if (listContainsArray(previousGameStates, newGameState)) {
                // Zyklus gefunden, nicht tiefer suchen
                result = SearchResult()
                result.status = SearchResult.STATUS_UNSOLVABLE
            } else {
                // kein Zyklus, also Rekursion
                previousGameStates.add(newGameState)
                result = findSolutionNoBackAndForth(maxRecursion, result, previousGameStates)
                previousGameStates.removeLast()
                result.move = move
            }
            if (result.status == SearchResult.STATUS_FOUND_SOLUTION) {
                //Log.d(TAG, "eine Lösung")
                return result
            } else {
                //Log.d(TAG, "keine Lösung")
                results.add(result)
            }
            undoLastMove()
        }

        if (allUnsolvable(results)) {
            // recycling eines Objekts
            results[0].move = null
            return results[0]
        }

        val resultOpen = SearchResult()
        resultOpen.status = SearchResult.STATUS_OPEN
        return resultOpen
    }
    */


    /**
     * Array of Bytes of correct size must be provided by caller
    fun toBytes(bytes: Array<Byte>) {
        for (i in tubes.indices) {
            val cells = tubes[i].cells
            cells.copyInto(bytes, i * cells.size)
        }
    }
    */

    /**
     * liefert einen Byte Array, mit dem Inhalt der Röhren,
     * wobei die Röhren sortiert wurden.
     * Das vereinfacht einen Vergleich zweier Spielstände
     * unter Missachtung der Reihenfolge der Röhren.
     * Um Arbeitsspeicher zu sparen, wird der Spielstand
     * möglichst kompakt gespeichert.
     * Pro Byte werden 2 Bälle gespeichert.
     * (upper and lower nibble)
     */
    fun toBytesNormalized(): Array<Byte> {
        // (3 * 3 + 1) / 2 = (9 + 1) / 2 = 10 / 2 = 5; 5 Bytes um 9 Nibbles zu speichern
        // (4 * 3 + 1) / 2 = (12 + 1) / 2 = 13 / 2 = 6; 6 Bytes um 12 Nibbles zu speichern
        // (5 * 3 + 1) / 2 = (15 + 1) / 2 = 16 / 2 = 8; 8 Bytes um 15 Nibbles zu speichern
        val s = (numberOfTubes * tubeHeight + 1) / 2
        val bytes = Array<Byte>(s) { 0 }
        val sortedTubes = tubes.copyOf()
        sortedTubes.sort()

        for(i in bytes.indices) {
            val lowerTubeIndex = i * 2 / tubeHeight
            val lowerBallIndex = i * 2 % tubeHeight
            val lowerNibble = sortedTubes[lowerTubeIndex].cells[lowerBallIndex]
            //Log.d(TAG, "lower [$lowerTubeIndex, $lowerBallIndex] = $lowerNibble")
            val upperTubeIndex = (i * 2 + 1) / tubeHeight
            // bei ungerader Anzahl Zellen bleibt das letzte Nibble leer
            val upperNibble = if(upperTubeIndex < numberOfTubes) {
                val upperBallIndex = (i * 2 + 1) % tubeHeight
                //Log.d(TAG, "upper [$upperTubeIndex, $upperBallIndex]")
                sortedTubes[upperTubeIndex].cells[upperBallIndex]
            } else {
                0
            }
            bytes[i] = (lowerNibble + (upperNibble * 16.toByte())).toByte()
        }

        /*
        for (i in sortedTubes.indices) {
            val cells = sortedTubes[i].cells
            cells.copyInto(bytes, i * tubeHeight)
        }
        */

        return bytes
    }

    /**
     * Umkehrfunktion zu toBytesNormalized()
     * Läd den Spielstand aus einem kompakten Array.
     */
    fun fromBytes(bytes: Array<Byte>) {
        for(i in bytes.indices) {
            val lowerTubeIndex = i * 2 / tubeHeight
            val lowerBallIndex = i * 2 % tubeHeight
            val lowerNibble = bytes[i] and 0b00001111.toByte()
            //Log.d(TAG, "lower [$lowerTubeIndex, $lowerBallIndex] = $lowerNibble")
            tubes[lowerTubeIndex].cells[lowerBallIndex] = lowerNibble
            val higherTubeIndex = (i * 2 + 1) / tubeHeight
            if(higherTubeIndex < numberOfTubes) {
                val higherBallIndex = (i * 2 + 1) % tubeHeight
                val higherNibble = (bytes[i].toInt() shr 4).toByte() and 0b00001111.toByte()
                //Log.d(TAG, "higher [$higherTubeIndex, $higherBallIndex] = $higherNibble")
                tubes[higherTubeIndex].cells[higherBallIndex] = higherNibble
            }
        }
        tubes.forEach { it.repairFillLevel() }
    }

    /**
     * exportiert Spielstatus als ASCII-Grafik
     */
    fun toAscii(lineDelimiter: String = "\n", columnDelimiter: String = " "): String {
        val ascii = StringBuilder()
        for (row in (tubeHeight - 1) downTo 0) {
            for (column in 0 until numberOfTubes) {
                val color = tubes[column].cells[row]
                val char = colorToChar(color)
                if (column != 0) {
                    ascii.append(columnDelimiter)
                }
                ascii.append(char)
            }
            // letzte Zeile (Reihe 0) nicht mit newline character abschließen
            if (row != 0) {
                ascii.append(lineDelimiter)
            }
            // Log.d(TAG, "toAscii() -> ${ascii.toString()}")
        }
        return ascii.toString()
    }

    /**
     * Beispiel:
     * <pre>
     * val lines = arrayOf<String>(
     *   "_ 5 5 _ _ 1 _ 3 6",
     *   "_ 4 7 _ _ 2 _ 6 5",
     *   "3 7 2 3 2 6 2 4 7",
     *   "1 4 3 4 5 6 7 1 1"
     * )
     * </pre>
     * todo: pruefen, ob es keine Lücken bei den Farben gibt 1, 2, 3, 7!
     * todo: Wenn Exception auftritt, ist der Zustand inkonsistent
     */
    fun fromAsciiLines(lines: Array<String>) {
        if (lines.size < 2) {
            throw IllegalArgumentException("height of game state is less than 2")
        }
        val trimmedLines = Array(lines.size) { "" }
        for (i in lines.indices) {
            trimmedLines[i] = lines[i].trim().replace(" ", "").replace("\t", "")
        }
        numberOfTubes = trimmedLines[0].length
        Log.d(TAG, "numberOfTubes=$numberOfTubes")
        for (i in 1 until trimmedLines.size) {
            if (trimmedLines[i].length != numberOfTubes) {
                throw IllegalArgumentException("different number of balls in lines of game state")
            }
        }
        if (numberOfTubes < 2) {
            throw IllegalArgumentException("less than 2 tubes in game state")
        }

        val allColors = mutableSetOf<Byte>()
        //Log.d(TAG, "allColors begin, trimmedLines.size=${trimmedLines.size}")
        for (line in trimmedLines) {
            for (char in line) {
                val color = charToColor(char)
                //Log.d(TAG, "char=$char, color=$color)")
                if ((color != 0.toByte()) && !allColors.contains(color)) {
                    //Log.d(TAG, "add")
                    allColors.add(color)
                }
            }
        }
        //Log.d(TAG, "allColors end allColors=$allColors")
        numberOfColors = allColors.size

        if (numberOfColors > numberOfTubes) {
            throw IllegalArgumentException("more colors than tubes")
        }

        numberOfExtraTubes = numberOfTubes - numberOfColors
        tubeHeight = trimmedLines.size

        tubes = Array(numberOfTubes) { Tube(tubeHeight) }
        moveLog.clear()

        // fillLevel wird automatisch berechnet
        // Schwebende Bälle werden ignoriert
        // Tube ist konsistent
        for (column in tubes.indices) {
            //Log.d(TAG, "column=$column")
            for (row in 0 until tubeHeight) {
                val lineNum = tubeHeight - row - 1
                //Log.d(TAG, "row=$row, lineNum=$lineNum")
                val line = trimmedLines[lineNum]
                val color = charToColor(line[column])
                if (color == 0.toByte()) {
                    break
                }
                tubes[column].addBall(color)
            }
        }
    }

    /**
     * importiert Spielstatus von ASCII-Grafik
     * Beispiel:
     * <pre>
     * _ 5 5 _ _ 1 _ 3 6\n
     * _ 4 7 _ _ 2 _ 6 5\n
     * 3 7 2 3 2 6 2 4 7\n
     * 1 4 3 4 5 6 7 1 7\n
     * </pre>
     * Leerzeichen und Tabs werden ignoriert.
     * todo: keine schwebenden Bälle erlauben
     */
    fun fromAscii(ascii: String, lineDelimiter: String = "\n") {
        Log.d(TAG, "fromAscii(ascii=\n$ascii)")
        val lines = ascii.split(lineDelimiter)
        fromAsciiLines(lines.toTypedArray())
    }

    fun clear() {
        tubes.forEach { it.clear() }
    }


    companion object {
        private const val TAG = "balla.GameState"
    }
}