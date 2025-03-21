package com.bimilyoncu.sscoderr.libretube.util

import android.content.Context
import com.bimilyoncu.sscoderr.libretube.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretube.helpers.NetworkHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper

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
