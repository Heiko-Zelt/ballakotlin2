package de.heikozelt.ballakotlin2.model

/**
 * wandelt 0 in '_', 1 in '1', 2 in '2', ... 10 in 'a' ... 15 in 'f' ... 35 in 'z'
 */
fun colorToChar(color: Byte): Char {
    return when (color) {
        0.toByte() -> '_'
        in 1..9 -> '0' + color.toInt()
        in 10..35 -> 'a' - 10 + color.toInt()
        else -> throw IllegalArgumentException("only numbers between 0 and 35 are allowed")
    }
}

/**
 * Wandelt ASCII-Zeichen in Farbnummer um.
 * '_' -> 0
 * '1'..'9' -> 1..9
 * 'a'..'z' -> 10..35
 * Vorsicht: GUI unterstützt aktuell maximal 15 Farben
 */
fun charToColor(char: Char): Byte {
    //Log.d(TAG, "char=>>$char<<")
    val digit = when (char) {
        '_' -> 0.toByte()
        in '1'..'9' -> (char - '0').toByte()
        in 'a'..'z' -> (char - 'a' + 10).toByte()
        else -> throw IllegalArgumentException("character must be '_', decimal digit or English lower case letter")
    }
    //Log.d(TAG, "digit=$digit")
    return digit
}

/**
 * @returns true, if all SearchResults are UNSOLVABLE
 * false, if any of the SearchResult is OPEN or FOUND_SOLUTION
 */
fun allUnsolvable(results: List<SearchResult>): Boolean {
    // 1. linear search for a list entry with status OPEN or FOUND_SOLUTION.
    // 2. if not found return true.
    return results.indexOfFirst { it.status != SearchResult.STATUS_UNSOLVABLE } == -1
    /*
    var unsolvable = true
    for (r in results) {
        if (r.status != SearchResult.STATUS_UNSOLVABLE) {
            unsolvable = false
            break
        }
    }
    return unsolvable
    */
}

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
 * Prueft, ob eine Liste einen Array enthält.
 * Es wird inhaltlich geprüft, nicht die Referenz.
 */
fun listContainsArray(list: List<Array<Byte>>, a: Array<Byte>): Boolean {
    return list.indexOfFirst { it contentEquals a } != -1
}