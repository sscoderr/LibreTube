package com.bimilyoncu.sscoderr.libretubess.ui.sheets

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.DialogAddChannelToGroupBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.AddChannelToGroupAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddChannelToGroupSheet : ExpandedBottomSheet(R.layout.dialog_add_channel_to_group) {
    private lateinit var channelId: String

    private val addToGroupAdapter by lazy(LazyThreadSafetyMode.NONE) {
        AddChannelToGroupAdapter(channelId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelId = arguments?.getString(IntentData.channelId)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogAddChannelToGroupBinding.bind(view)

        binding.groupsRV.adapter = addToGroupAdapter

        binding.cancel.setOnClickListener {
            requireDialog().dismiss()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val subGroupsDao = DatabaseHolder.Database.subscriptionGroupsDao()
            val subscriptionGroups = subGroupsDao.getAll().sortedBy { it.index }.toMutableList()

            withContext(Dispatchers.Main) {
                addToGroupAdapter.submitList(subscriptionGroups)

                binding.okay.setOnClickListener {
                    requireDialog().hide()

                    lifecycleScope.launch(Dispatchers.IO) {
                        subGroupsDao.updateAll(subscriptionGroups)

                        withContext(Dispatchers.Main) {
                            dialog?.dismiss()
                        }
                    }
                }
            }
        }
    }
}
