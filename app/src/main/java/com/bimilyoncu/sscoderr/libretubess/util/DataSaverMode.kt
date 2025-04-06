package com.bimilyoncu.sscoderr.libretubess.util

import android.content.Context
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.NetworkHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper

object DataSaverMode {
    fun isEnabled(context: Context): Boolean {
        val pref = PreferenceHelper.getString(PreferenceKeys.DATA_SAVER_MODE, "disabled")
        return when (pref) {
            "enabled" -> true
            "disabled" -> false
            "metered" -> NetworkHelper.isNetworkMetered(context)
            else -> throw IllegalArgumentException()
        }
    }
}
