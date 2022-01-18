package de.heikozelt.ballakotlin2.model

import android.util.Log
import kotlinx.coroutines.yield
import kotlin.collections.List
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
    var tubes = mutableListOf<Tube>()

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
     * erzeugt ein leeres Spielfeld in den angegebenen Dimensionen
     */
    fun resize(_numberOfColors: Int, _numberOfExtraTubes: Int, _tubeHeight: Int) {
        Log.i(TAG, "GameState secondary constructor")
        numberOfColors = _numberOfColors
        numberOfExtraTubes = _numberOfExtraTubes
        numberOfTubes = _numberOfColors + _numberOfExtraTubes
        tubeHeight = _tubeHeight
        tubes = MutableList(numberOfTubes) { Tube(tubeHeight) }
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
            moveLog.push(move)
            //Log.i("moveLog: ${Gson.toJson(moveLog)}")
        }
    }

    /**
     * undoes last move, according to log
     */
    fun undoLastMove(): Move {
        //val forwardMove = moveLog.removeLast()
        val forwardMove = moveLog.pop()
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
     * - letzten Zug rückwärts (einfacher hin- und her-Zyklus)
     * - Zug von einfarbiger Röhre in leere Röhre
     * - bei 2 einfarbige Röhren, Zug von Röhre mit niedrigerem Füllstand in Röhre mit höherem Füllstand
     * todo: bei 2 Röhren, die unten gleich sind, aber oben unterschiedlich viele Bälle der gleichen Farbe haben, von niedrigerem zu höherem Füllstand.
     */
    fun isMoveUseful(from: Int, to: Int): Boolean {
        //Log.i(TAG, "isMoveUseful(from=${from}, to=${to})")

        // kann keinen Ball aus leerer Röhre nehmen
        if (tubes[from].isEmpty()) {
            return false
        }
        // Ziel-Tube ist voll
        if (tubes[to].isFull()) {
            return false
        }
        // selbe Röhre, sinnlos
        if (to == from) {
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

        // hin- und her, einfachster Zyklus
        if (moveLog.isNotEmpty()) {
            if (Move(from, to).backwards() == moveLog.last()) {
                return false
            }
        }

        // oberster Ball hat selbe Farbe oder Ziel-Röhre ist leer
        if (tubes[to].isEmpty() || isSameColor(from, to)) {
            return true
        }
        return false
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
     * Röhren 4 und 5 sind Duplikate.
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

    /**
     * Liefert eine Menge an Röhren (Indexe), die geeignet sind.
     * - ohne Dupletten
     * - ohne leere Röhren
     * - ohne gelöste Röhren
     */
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
        // ersten Zug unbedingt hinzufügen (wenn Liste nicht leer ist)
        //if(moves.size >= 1) {
        //    result.add(moves[1])
        //}

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
     * Liefert eine Liste mit Zügen, die als nächster Zug sinnvoll erscheinen.
     * Das sind alle möglichen Züge außer:
     * - inhaltsgleiche Züge
     * - Zug von einfarbiger Röhre in leere Röhre
     */
    fun allUsefulMoves(): List<Move> {
        val sourceTubes = usefulSourceTubes()
        val targetTubes = usefulTargetTubes()
        val moves = mutableListOf<Move>()
        for (from in sourceTubes) {
            for (to in targetTubes) {
                if (isMoveUseful(from, to)) {
                    val m = Move(from, to)
                    //Log.d(TAG, "add ${m.toAscii()}")
                    moves.add(m)
                }
            }
        }
        return contentDistinctMoves(moves)
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

    /**
     * Liefert die Lösung als Liste von Zügen oder null, falls keine Lösung gefunden wurde.
     * - Falls es mehrere Lösungen gibt, wird die Lösung mit den wenigsten Zügen geliefert.
     * - Falls es mehrere Lösungen mit gleich vielen Zügen gibt, wird zufällig eine ausewählt.
     * - Falls das Puzzle bereits gelöst ist, wird eine leere Liste zuruckegeliefert.
     * vorläufige einfache Implementierung:
     * - Die Rekursionstiefe bei der Suche in begrenzt.
     * - Zyklen werden nicht erkannt.
     * - Keine Unterscheidung zwischen sicher unlösbar und keine Lösung gefunden wegen Rekursionstiefenbegrenzung
     */
    suspend fun findSolution(): SearchResult {
        Log.d(TAG, "find Solution for\n${toAscii()}")
        val gs2 = this.cloneWithoutLog()
        var result = SearchResult()
        // erst einfache Lösung suchen, dann Rekursionstiefe erhöhen
        // 1. Abbruchkriterium: Maximale Rekursionstiefe erreicht
        for (recursionDepth in 0..MAX_RECURSION) {
            val startTime = System.nanoTime()
            result = gs2.findSolutionNoBackAndForth(recursionDepth)
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
                        "findSolutionNoBackAndForth(maxRecursionDepth=$recursionDepth) -> elapsed=$elapsed msec, open"
                    )
                }
                else -> {
                    Log.e(
                        TAG,
                        "findSolutionNoBackAndForth(maxRecursionDepth=$recursionDepth) -> elapsed=$elapsed msec, ???????"
                    )
                    break
                }
            }
            // 3. Abbruchkriterium: Zeit-Überschreitung
            // todo: von was haengt die Anzahl der Verzweigungen ab?
            if (elapsed * numberOfTubes >= MAX_ESTIMATED_DURATION) {
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
    suspend fun findSolutionNoBackAndForth(maxRecursionDepth: Int): SearchResult {
        // Job canceled?
        yield()

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
            //Log.d(TAG, "Rekursion")
            moveBallAndLog(move)
            val result = findSolutionNoBackAndForth(maxRecursion)
            result.move = move

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

    /**
     * @returns true, if all SearchResults are UNSOLVABLE
     * false, if any of the SearchResult is OPEN or FOUND_SOLUTION
     */
    fun allUnsolvable(results: MutableList<SearchResult>): Boolean {
        var unsolvable = true
        for (r in results) {
            if (r.status != SearchResult.STATUS_UNSOLVABLE) {
                unsolvable = false
                break
            }
        }
        return unsolvable
    }

    /**
     * exportiert Spielstatus als ASCII-Grafik
     */
    fun toAscii(): String {
        val ascii = StringBuilder()
        for (row in (tubeHeight - 1) downTo 0) {
            for (column in 0 until numberOfTubes) {
                val color = tubes[column].cells[row]
                val char = colorToChar(color)
                if (column != 0) {
                    ascii.append(' ')
                }
                ascii.append(char)
            }
            // letzte Zeile (Reihe 0) nicht mit newline character abschließen
            if (row != 0) {
                ascii.append("\n")
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

        val allColors = mutableSetOf<Int>()
        //Log.d(TAG, "allColors begin, trimmedLines.size=${trimmedLines.size}")
        for (line in trimmedLines) {
            for (char in line) {
                val color = charToColor(char)
                //Log.d(TAG, "char=$char, color=$color)")
                if ((color != 0) && !allColors.contains(color)) {
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

        tubes = MutableList(numberOfTubes) { Tube(tubeHeight) }
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
                if (color == 0) {
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
    fun fromAscii(ascii: String) {
        Log.d(TAG, "fromAscii(ascii=\n$ascii)")
        val lines = ascii.split("\n")
        fromAsciiLines(lines.toTypedArray())
    }

    companion object {
        private const val TAG = "balla.GameState"
        private const val MAX_RECURSION = 40

        /**
         * maximale geschätzte Dauer
         */
        private const val MAX_ESTIMATED_DURATION = 10000
    }
}