package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.bimilyoncu.sscoderr.libretubess.BuildConfig
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ErrorDialog
import com.bimilyoncu.sscoderr.libretubess.util.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.settings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val update = findPreference<Preference>("update")
        update?.summary = "v${BuildConfig.VERSION_NAME}"

        // check app update manually
        update?.setOnPreferenceClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                UpdateChecker(requireContext()).checkUpdate(true)
            }

            true
        }

        val crashlog = findPreference<Preference>("crashlog")
        crashlog?.isVisible = PreferenceHelper.getErrorLog().isNotEmpty() && BuildConfig.DEBUG
        crashlog?.setOnPreferenceClickListener {
            ErrorDialog().show(childFragmentManager, null)
            crashlog.isVisible = false
            true
        }
    }
}
