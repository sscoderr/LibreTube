package com.bimilyoncu.sscoderr.libretube.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.bimilyoncu.sscoderr.libretube.databinding.CustomInstanceRowBinding
import com.bimilyoncu.sscoderr.libretube.db.obj.CustomInstance
import com.bimilyoncu.sscoderr.libretube.ui.adapters.callbacks.DiffUtilItemCallback
import com.bimilyoncu.sscoderr.libretube.ui.viewholders.CustomInstancesViewHolder

class CustomInstancesAdapter(
    private val onClickInstance: (CustomInstance) -> Unit,
    private val onDeleteInstance: (CustomInstance) -> Unit
) : ListAdapter<CustomInstance, CustomInstancesViewHolder>(
    DiffUtilItemCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomInstancesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CustomInstanceRowBinding.inflate(layoutInflater, parent, false)
        return CustomInstancesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomInstancesViewHolder, position: Int) {
        val instance = getItem(position)!!

        with (holder.binding) {
            instanceName.text = instance.name

            root.setOnClickListener {
                onClickInstance(instance)
            }

            deleteInstance.setOnClickListener {
                onDeleteInstance.invoke(instance)
            }
        }
    }
}