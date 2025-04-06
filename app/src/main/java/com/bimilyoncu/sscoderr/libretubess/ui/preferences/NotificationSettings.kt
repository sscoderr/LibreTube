package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import androidx.work.ExistingPeriodicWorkPolicy
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.NotificationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment

class NotificationSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.notifications

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notification_settings, rootKey)

        val notificationsEnabled =
            findPreference<SwitchPreferenceCompat>(PreferenceKeys.NOTIFICATION_ENABLED)
        val checkingFrequency = findPreference<ListPreference>(PreferenceKeys.CHECKING_FREQUENCY)
        val requiredNetwork = findPreference<ListPreference>(PreferenceKeys.REQUIRED_NETWORK)

        notificationsEnabled?.setOnPreferenceChangeListener { _, _ ->
            updateNotificationPrefs()
            true
        }

        checkingFrequency?.setOnPreferenceChangeListener { _, _ ->
            updateNotificationPrefs()
            true
        }

        requiredNetwork?.setOnPreferenceChangeListener { _, _ ->
            updateNotificationPrefs()
            true
        }
    }

    private fun updateNotificationPrefs() {
        // replace the previous queued work request
        NotificationHelper
            .enqueueWork(
                context = requireContext(),
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE
            )
    }
}
