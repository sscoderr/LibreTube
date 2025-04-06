package com.bimilyoncu.sscoderr.libretubess.obj

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectableOption(
    val isSelected: Boolean,
    val name: String
) : Parcelable
