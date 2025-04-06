package com.bimilyoncu.sscoderr.libretubess.ui.extensions

import com.bimilyoncu.sscoderr.libretubess.api.obj.Comment

fun List<Comment>.filterNonEmptyComments(): List<Comment> {
    return filter { !it.commentText.isNullOrEmpty() }
}
