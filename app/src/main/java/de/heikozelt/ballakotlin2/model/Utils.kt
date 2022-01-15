package de.heikozelt.ballakotlin2.model

/**
 * wandelt 0 in '_', 1 in '1', 2 in '2', ... 10 in 'a' ... 15 in 'f' ... 35 in 'z'
 */
fun colorToChar(color: Int): Char {
    return if (color == 0) {
        '_'
    } else if (color in 1..9) {
        '0' + color
    } else if (color in 10..35) {
        'a' - 10 + color
    } else {
        throw IllegalArgumentException("only numbers between 0 and 35 are allowed")
    }
}

/**
 * Wandelt ASCII-Zeichen in Farbnummer um.
 * '_' -> 0
 * '1'..'9' -> 1..9
 * 'a'..'z' -> 10..35
 * Vorsicht: GUI unterstützt aktuell maximal 15 Farben
 */
fun charToColor(char: Char): Int {
    //Log.d(TAG, "char=>>$char<<")
    val digit = if (char == '_') {
        0
    } else if (char in '1'..'9') {
        char - '0'
    } else if (char in 'a'..'z') {
        char - 'a' + 10
    } else {
        throw IllegalArgumentException("character must be '_', decimal digit or English lower case letter")
    }
    //Log.d(TAG, "digit=$digit")
    return digit
}