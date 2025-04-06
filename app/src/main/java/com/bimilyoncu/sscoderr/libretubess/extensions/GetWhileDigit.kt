package com.bimilyoncu.sscoderr.libretubess.extensions

/**
 * Read a string as long as the char is a digit and return the number value as int
 */
fun String?.getWhileDigit(): Int? {
    return orEmpty().takeWhile { char ->
        char.isDigit()
    }.toIntOrNull()
}
