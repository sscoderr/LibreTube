package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment

class SponsorBlockSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.sponsorblock

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sponsorblock_settings, rootKey)
    }
}
