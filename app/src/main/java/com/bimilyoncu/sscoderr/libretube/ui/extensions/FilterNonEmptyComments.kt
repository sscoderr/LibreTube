package com.bimilyoncu.sscoderr.libretube.ui.extensions

import com.bimilyoncu.sscoderr.libretube.api.obj.Comment

fun List<Comment>.filterNonEmptyComments(): List<Comment> {
    return filter { !it.commentText.isNullOrEmpty() }
}
