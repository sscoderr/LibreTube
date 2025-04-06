package com.bimilyoncu.sscoderr.libretubess.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.databinding.SimpleOptionsRecyclerBinding
import com.bimilyoncu.sscoderr.libretubess.extensions.setOnDraggedListener
import com.bimilyoncu.sscoderr.libretubess.helpers.NavBarHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.NavBarOptionsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NavBarOptionsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = SimpleOptionsRecyclerBinding.inflate(layoutInflater)
        val options = NavBarHelper.getNavBarItems(requireContext())
        val adapter = NavBarOptionsAdapter(
            options.toMutableList(),
            NavBarHelper.getStartFragmentId(requireContext())
        )

        binding.optionsRecycler.layoutManager = LinearLayoutManager(context)
        binding.optionsRecycler.adapter = adapter
        binding.optionsRecycler.setOnDraggedListener { from, to ->
            val itemToMove = adapter.items[from]
            adapter.items.remove(itemToMove)
            adapter.items.add(to, itemToMove)

            adapter.notifyItemMoved(from, to)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.navigation_bar)
            .setView(binding.root)
            .setPositiveButton(R.string.okay) { _, _ ->
                NavBarHelper.setNavBarItems(adapter.items, requireContext())
                NavBarHelper.setStartFragment(requireContext(), adapter.selectedHomeTabId)
                RequireRestartDialog()
                    .show(requireParentFragment().childFragmentManager, null)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
