package com.bimilyoncu.sscoderr.libretubess.extensions

import android.icu.text.CompactDecimalFormat
import com.bimilyoncu.sscoderr.libretubess.helpers.LocaleHelper

fun Long?.formatShort(): String = CompactDecimalFormat
    .getInstance(LocaleHelper.getAppLocale(), CompactDecimalFormat.CompactStyle.SHORT)
    .format(this ?: 0)
