package com.bimilyoncu.sscoderr.libretube.extensions

import android.icu.text.CompactDecimalFormat
import com.bimilyoncu.sscoderr.libretube.helpers.LocaleHelper

fun Long?.formatShort(): String = CompactDecimalFormat
    .getInstance(LocaleHelper.getAppLocale(), CompactDecimalFormat.CompactStyle.SHORT)
    .format(this ?: 0)
