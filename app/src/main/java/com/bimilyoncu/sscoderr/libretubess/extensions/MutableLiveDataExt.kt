package com.bimilyoncu.sscoderr.libretubess.extensions

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.updateIfChanged(newValue: T) {
    if (value != newValue) value = newValue
}
