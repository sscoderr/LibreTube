package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.LocaleHelper
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.RequireRestartDialog

class GeneralSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.general

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_settings, rootKey)

        val language = findPreference<ListPreference>("language")
        language?.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val region = findPreference<ListPreference>("region")
        region?.let { setupRegionPref(it) }

        val autoRotation = findPreference<ListPreference>(PreferenceKeys.ORIENTATION)
        autoRotation?.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }
    }

    private fun setupRegionPref(preference: ListPreference) {
        val countries = LocaleHelper.getAvailableCountries()
        val countryNames = countries.map { it.name }
            .toMutableList()
        countryNames.add(0, requireContext().getString(R.string.systemLanguage))

        val countryCodes = countries.map { it.code }
            .toMutableList()
        countryCodes.add(0, "sys")

        preference.entries = countryNames.toTypedArray()
        preference.entryValues = countryCodes.toTypedArray()
        preference.summaryProvider = Preference.SummaryProvider<ListPreference> {
            it.entry
        }
    }
}
