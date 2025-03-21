package com.bimilyoncu.sscoderr.libretube.ui.preferences

import android.os.Bundle
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.ui.base.BasePreferenceFragment

class SponsorBlockSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.sponsorblock

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sponsorblock_settings, rootKey)
    }
}
