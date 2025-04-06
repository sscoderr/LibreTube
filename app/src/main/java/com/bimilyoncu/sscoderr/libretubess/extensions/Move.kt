package com.bimilyoncu.sscoderr.libretubess.extensions

fun <T> MutableList<T>.move(oldPosition: Int, newPosition: Int) {
    val item = this.get(oldPosition)
    this.removeAt(oldPosition)
    this.add(newPosition, item)
}
