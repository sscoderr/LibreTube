package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.IconsSheetAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.NavBarOptionsDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.RequireRestartDialog
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.IconsBottomSheet
import com.google.android.material.color.DynamicColors

class AppearanceSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.appearance
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.appearance_settings, rootKey)

        val themeToggle = findPreference<ListPreference>(PreferenceKeys.THEME_MODE)
        themeToggle?.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val pureTheme = findPreference<SwitchPreferenceCompat>(PreferenceKeys.PURE_THEME)
        pureTheme?.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val accentColor = findPreference<ListPreference>(PreferenceKeys.ACCENT_COLOR)
        updateAccentColorValues(accentColor!!)
        accentColor.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val changeIcon = findPreference<Preference>(PreferenceKeys.APP_ICON)
        val iconPref = PreferenceHelper.getString(
            PreferenceKeys.APP_ICON,
            IconsSheetAdapter.Companion.AppIcon.Default.activityAlias
        )
        IconsSheetAdapter.availableIcons.firstOrNull { it.activityAlias == iconPref }?.let {
            changeIcon?.summary = getString(it.nameResource)
        }
        changeIcon?.setOnPreferenceClickListener {
            IconsBottomSheet().show(childFragmentManager)
            true
        }

        val labelVisibilityMode = findPreference<ListPreference>(PreferenceKeys.LABEL_VISIBILITY)
        labelVisibilityMode?.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val navBarOptions = findPreference<Preference>(PreferenceKeys.NAVBAR_ITEMS)
        navBarOptions?.setOnPreferenceClickListener {
            NavBarOptionsDialog().show(childFragmentManager, null)
            true
        }

        val legacySubscriptionView =
            findPreference<SwitchPreferenceCompat>(PreferenceKeys.LEGACY_SUBSCRIPTIONS)
        val legacySubscriptionColumns =
            findPreference<ListPreference>(PreferenceKeys.LEGACY_SUBSCRIPTIONS_COLUMNS)
        legacySubscriptionColumns?.isVisible = legacySubscriptionView?.isChecked!!
        legacySubscriptionView.setOnPreferenceChangeListener { _, newValue ->
            legacySubscriptionColumns?.isVisible = newValue as Boolean
            true
        }
    }

    /**
     * Remove material you from accent color option if not available
     */
    private fun updateAccentColorValues(pref: ListPreference) {
        if (!DynamicColors.isDynamicColorAvailable()) {
            pref.entries = pref.entries.toList().subList(1, pref.entries.size).toTypedArray()
            pref.entryValues = pref.entryValues.toList().subList(1, pref.entryValues.size).toTypedArray()
        }
    }
}
