package com.github.libretube.ui.views

import android.content.Context
import android.text.TextUtils
import android.text.util.Linkify
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.github.libretube.R
import com.github.libretube.helpers.ClipboardHelper

class ExpandableTextView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatTextView(context, attributeSet) {

    init {
        maxLines = DEFAULT_MAX_LINES
        ellipsize = TextUtils.TruncateAt.END
        setBackgroundResource(R.drawable.rounded_ripple)
        autoLinkMask = Linkify.WEB_URLS

        setOnClickListener {
            maxLines = if (maxLines == DEFAULT_MAX_LINES) Int.MAX_VALUE else DEFAULT_MAX_LINES
        }
        setOnLongClickListener {
            ClipboardHelper.save(context, text = text.toString())
            true
        }
    }

    companion object {
        private const val DEFAULT_MAX_LINES = 2
    }
}
