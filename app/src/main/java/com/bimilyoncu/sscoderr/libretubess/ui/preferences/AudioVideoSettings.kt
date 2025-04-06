package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment

class AudioVideoSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.audio_video

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.audio_video_settings, rootKey)
    }
}
