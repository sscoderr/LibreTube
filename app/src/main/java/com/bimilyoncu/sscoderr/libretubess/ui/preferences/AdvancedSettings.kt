package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.ImageHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdvancedSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.advanced

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.advanced_settings, rootKey)

        val maxImageCache = findPreference<ListPreference>(PreferenceKeys.MAX_IMAGE_CACHE)
        maxImageCache?.setOnPreferenceChangeListener { _, _ ->
            ImageHelper.initializeImageLoader(requireContext())
            true
        }

        val resetSettings = findPreference<Preference>(PreferenceKeys.RESET_SETTINGS)
        resetSettings?.setOnPreferenceClickListener {
            showResetDialog()
            true
        }
    }

    private fun showResetDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset)
            .setMessage(R.string.reset_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.reset) { _, _ ->
                // clear default preferences
                PreferenceHelper.clearPreferences()

                // clear login token
                PreferenceHelper.setToken("")

                ActivityCompat.recreate(requireActivity())
            }
            .show()
    }
}
