package com.bimilyoncu.sscoderr.libretube.ui.preferences

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.bimilyoncu.sscoderr.libretube.BuildConfig
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretube.ui.base.BasePreferenceFragment
import com.bimilyoncu.sscoderr.libretube.ui.dialogs.ErrorDialog
import com.bimilyoncu.sscoderr.libretube.util.UpdateChecker
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
