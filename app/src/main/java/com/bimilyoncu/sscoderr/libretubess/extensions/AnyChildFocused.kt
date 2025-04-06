package com.bimilyoncu.sscoderr.libretubess.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

fun View.anyChildFocused(): Boolean {
    if (isFocused) return true

    if (this is ViewGroup) {
        return children.any { it.anyChildFocused() }
    }

    return false
}