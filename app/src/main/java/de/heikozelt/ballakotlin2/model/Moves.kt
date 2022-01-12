package de.heikozelt.ballakotlin2.model

import android.util.Log
import java.lang.IllegalArgumentException

/**
 * Die Klasse repräsentiert eine Abfolge von Spielzügen.
 * Sie wird als Undo-Log verwendet.
 *
 * Ideen, wie ein ASCII-Notation aussehen könnte
 * "(1>2) (4>4) (2>5) (35>1)"
 * "1>2 4>4 2>5 35>1" kompakt aber haesslich
 * "1>2  4>4  2>5  z>1"
 * "12 44 25 z1"
 * "124425z1" schlecht lesbar
 * "1->2  4->4  2->5  35->1" Problem: sonstige Whitespaces
 * "1>2, 4>4, 2>5, 35>1"
 * "1->2,4->4,2->5,35->1" auch erlaubt
 * "1->2, 4->4, 2->5, 35->1" <<<< schönste, beste Lösung
 *
 * Spielzüge sind durch Komma getrennt. Quell- und Ziel-Röhre durch einen Pfeil.
 * Zusätzliche Whitespaces sind erlaubt.
 */
class Moves {
    private val movesList = mutableListOf<Move>()

    fun clear() {
        movesList.clear()
    }

    fun clone(): Moves {
        val miniMe = Moves()
        miniMe.pushAll(this)
        return miniMe
    }

    fun addAll(moves: Moves) {
        movesList.addAll(moves.movesList)
    }

    fun pushAll(moves: Moves) {
        movesList.addAll(moves.movesList)
    }

    fun push(move: Move) {
        movesList.add(move)
    }

    fun pop(): Move {
        return movesList.removeAt(movesList.size - 1)
    }

    fun isEmpty(): Boolean {
        return movesList.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return movesList.isNotEmpty()
    }

    fun last(): Move {
        return movesList.last()
    }

    fun size(): Int {
        return movesList.size
    }

    /**
     * Beispiel:
     * "4->3, 0->17"
     */
    fun toAscii(): String {
        val sb = StringBuilder()
        for(i in movesList.indices) {
            if(i != 0) {
                sb.append(", ")
            }
            sb.append(movesList[i].toAscii())
        }
        return sb.toString()
    }

    /**
     * Beispiele:
     * 1. "0->1, 3->4"
     * 2. "10->0, 3->15"
     * Röhren sind durchnummeriert: 0..9 und a..z
     */
    fun fromAscii(ascii: String) {
        val newMovesList = mutableListOf<Move>()
        val trimmed = ascii.trim()
        if(trimmed.isNotEmpty()) {
            val moves = trimmed.split(',')
            for (str in moves) {
                //Log.d(TAG, "str=>>>$str<<<")
                val move = Move()
                move.fromAscii(str)
                newMovesList.add(move)
            }
        }
        movesList.clear()
        movesList.addAll(newMovesList)
    }

    /**
     * export
     */
    fun toArray(): Array<Move> {
        return movesList.toTypedArray()
    }

    /**
     * export
     */
    fun asMutableList(): MutableList<Move> {
        return movesList
    }

    /**
     * import
     */
    fun fromList(ml: List<Move>) {
        movesList.clear()
        movesList.addAll(ml)
    }

    /**
     * inhaltlich gleich, wenn keine Unterschiede gefunden
     */
    fun contentEquals(other: Moves): Boolean {
        val otherList = other.asMutableList()
        if(movesList.size != other.size()) {
            return false
        }
        for(i in movesList.indices) {
            if(!movesList[i].equalsMove(otherList[i])) {
                return false
            }
        }
        return true
    }

    companion object {
        private const val TAG = "balla.Moves"
    }
}