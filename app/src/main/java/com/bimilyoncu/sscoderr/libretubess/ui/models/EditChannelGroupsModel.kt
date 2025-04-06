package com.bimilyoncu.sscoderr.libretubess.ui.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bimilyoncu.sscoderr.libretubess.db.obj.SubscriptionGroup

class EditChannelGroupsModel : ViewModel() {
    val groups = MutableLiveData<List<SubscriptionGroup>>()
    var groupToEdit: SubscriptionGroup? = null
}
