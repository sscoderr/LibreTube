package com.bimilyoncu.sscoderr.libretube.extensions

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.updateIfChanged(newValue: T) {
    if (value != newValue) value = newValue
}
