package com.bimilyoncu.sscoderr.libretube.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.databinding.DialogCustomIntancesListBinding
import com.bimilyoncu.sscoderr.libretube.ui.adapters.CustomInstancesAdapter
import com.bimilyoncu.sscoderr.libretube.ui.models.CustomInstancesModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomInstancesListDialog: DialogFragment() {
    val viewModel: CustomInstancesModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogCustomIntancesListBinding.inflate(layoutInflater)
        val adapter = CustomInstancesAdapter(
            onClickInstance = {
                CreateCustomInstanceDialog()
                    .apply {
                        arguments = bundleOf(IntentData.customInstance to it)
                    }
                    .show(childFragmentManager, null)
            },
            onDeleteInstance = {
                viewModel.deleteCustomInstance(it)
            }
        )
        binding.customInstancesRecycler.adapter = adapter

        lifecycleScope.launch {
            viewModel.instances.collectLatest {
                adapter.submitList(it)
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.customInstance))
            .setView(binding.root)
            .setPositiveButton(getString(R.string.okay), null)
            .setNegativeButton(getString(R.string.addInstance), null)
            .show()
            .apply {
                getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
                    CreateCustomInstanceDialog()
                        .show(childFragmentManager, null)
                }
            }
    }
}