package com.bimilyoncu.sscoderr.libretubess.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.InstanceRepository
import com.bimilyoncu.sscoderr.libretubess.api.PipedMediaServiceRepository
import com.bimilyoncu.sscoderr.libretubess.api.RetrofitInstance
import com.bimilyoncu.sscoderr.libretubess.api.obj.PipedInstance
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.databinding.SimpleOptionsRecyclerBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder.Database
import com.bimilyoncu.sscoderr.libretubess.extensions.toastFromMainDispatcher
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.InstancesAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.base.BasePreferenceFragment
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.CreateCustomInstanceDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.CustomInstancesListDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.DeleteAccountDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.LoginDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.LogoutDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl

class InstanceSettings : BasePreferenceFragment() {
    override val titleResourceId: Int = R.string.instance
    private val token get() = PreferenceHelper.getToken()
    private var instances = mutableListOf<PipedInstance>()
    private val authInstanceToggle get() = findPreference<SwitchPreferenceCompat>(
        PreferenceKeys.AUTH_INSTANCE_TOGGLE
    )!!

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.instance_settings, rootKey)

        val instancePref = findPreference<ListPreference>(PreferenceKeys.FETCH_INSTANCE)!!
        val authInstanceToggle = findPreference<SwitchPreferenceCompat>(
            PreferenceKeys.AUTH_INSTANCE_TOGGLE
        )!!
        val authInstance = findPreference<ListPreference>(PreferenceKeys.AUTH_INSTANCE)!!
        val instancePrefs = listOf(instancePref, authInstance)
        val appContext = requireContext().applicationContext

        lifecycleScope.launch {
            // update the instances to also show custom ones
            initInstancesPref(instancePrefs, InstanceRepository(appContext).getInstancesFallback())

            // try to fetch the public list of instances async
            val instanceRepo = InstanceRepository(appContext)
            val instances = instanceRepo.getInstances()
                .onFailure {
                    appContext.toastFromMainDispatcher(it.message.orEmpty())
                }
            initInstancesPref(
                instancePrefs,
                instances.getOrDefault(instanceRepo.getInstancesFallback())
            )
        }

        authInstance.setOnPreferenceChangeListener { _, _ ->
            RetrofitInstance.apiLazyMgr.reset()
            logoutAndUpdateUI()
            true
        }

        authInstanceToggle.setOnPreferenceChangeListener { _, _ ->
            RetrofitInstance.apiLazyMgr.reset()
            logoutAndUpdateUI()
            true
        }

        val customInstance = findPreference<Preference>(PreferenceKeys.CUSTOM_INSTANCE)
        customInstance?.setOnPreferenceClickListener {
            CustomInstancesListDialog()
                .show(childFragmentManager, CreateCustomInstanceDialog::class.java.name)
            true
        }

        val login = findPreference<Preference>(PreferenceKeys.LOGIN_REGISTER)
        val logout = findPreference<Preference>(PreferenceKeys.LOGOUT)
        val deleteAccount = findPreference<Preference>(PreferenceKeys.DELETE_ACCOUNT)

        login?.isVisible = token.isEmpty()
        logout?.isVisible = token.isNotEmpty()
        deleteAccount?.isEnabled = token.isNotEmpty()

        childFragmentManager.setFragmentResultListener(
            INSTANCE_DIALOG_REQUEST_KEY,
            this
        ) { _, resultBundle ->
            val isLoggedIn = resultBundle.getBoolean(IntentData.loginTask)
            val isLoggedOut = resultBundle.getBoolean(IntentData.logoutTask)
            if (isLoggedIn) {
                login?.isVisible = false
                logout?.isVisible = true
                deleteAccount?.isEnabled = true
            } else if (isLoggedOut) {
                logoutAndUpdateUI()
            }
        }

        login?.setOnPreferenceClickListener {
            LoginDialog().show(childFragmentManager, LoginDialog::class.java.name)
            true
        }

        logout?.setOnPreferenceClickListener {
            LogoutDialog().show(childFragmentManager, LogoutDialog::class.java.name)
            true
        }

        deleteAccount?.setOnPreferenceClickListener {
            DeleteAccountDialog()
                .show(childFragmentManager, DeleteAccountDialog::class.java.name)
            true
        }
    }

    private suspend fun initInstancesPref(
        instancePrefs: List<ListPreference>,
        publicInstances: List<PipedInstance>
    ) = runCatching {
        val customInstances = withContext(Dispatchers.IO) {
            Database.customInstanceDao().getAll()
        }.map { PipedInstance(it.name, it.apiUrl) }

        instances = publicInstances.plus(customInstances).toMutableList()

        // add the currently used instances to the list if they're currently down / not part
        // of the public instances list
        for (apiUrl in listOf(PipedMediaServiceRepository.apiUrl, RetrofitInstance.authUrl)) {
            if (instances.none { it.apiUrl == apiUrl }) {
                val origin = apiUrl.toHttpUrl().host
                instances.add(PipedInstance(origin, apiUrl, isCurrentlyDown = true))
            }
        }

        instances.sortBy { it.name }

        // If any preference dialog is visible in this fragment, it's one of the instance selection
        // dialogs. In order to prevent UX issues, we don't update the instances list then.
        if (isDialogVisible) return@runCatching

        for (instancePref in instancePrefs) {
            // add custom instances to the list preference
            instancePref.entries = instances.map { it.name }.toTypedArray()
            instancePref.entryValues = instances.map { it.apiUrl }.toTypedArray()
            instancePref.summaryProvider =
                Preference.SummaryProvider<ListPreference> { preference ->
                    preference.entry
                }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference.key in arrayOf(PreferenceKeys.FETCH_INSTANCE, PreferenceKeys.AUTH_INSTANCE)) {
            showInstanceSelectionDialog(preference as ListPreference)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun showInstanceSelectionDialog(preference: ListPreference) {
        var selectedInstance = preference.value
        val selectedIndex = instances.indexOfFirst { it.apiUrl == selectedInstance }

        val layoutInflater = LayoutInflater.from(context)
        val binding = SimpleOptionsRecyclerBinding.inflate(layoutInflater)
        binding.optionsRecycler.layoutManager = LinearLayoutManager(context)

        val instances = ImmutableList.copyOf(this.instances)
        binding.optionsRecycler.adapter = InstancesAdapter(selectedIndex) {
            selectedInstance = instances[it].apiUrl
        }.also { it.submitList(instances) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.title)
            .setView(binding.root)
            .setPositiveButton(R.string.okay) { _, _ ->
                preference.value = selectedInstance
                resetForNewInstance()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun logoutAndUpdateUI() {
        PreferenceHelper.setToken("")
        Toast.makeText(context, getString(R.string.loggedout), Toast.LENGTH_SHORT).show()
        findPreference<Preference>(PreferenceKeys.LOGIN_REGISTER)?.isVisible = true
        findPreference<Preference>(PreferenceKeys.LOGOUT)?.isVisible = false
        findPreference<Preference>(PreferenceKeys.DELETE_ACCOUNT)?.isEnabled = false
    }

    private fun resetForNewInstance() {
        if (!authInstanceToggle.isChecked) {
            logoutAndUpdateUI()
        }
        RetrofitInstance.apiLazyMgr.reset()
        ActivityCompat.recreate(requireActivity())
    }

    companion object {
        const val INSTANCE_DIALOG_REQUEST_KEY = "instance_dialog_request_key"
    }
}
