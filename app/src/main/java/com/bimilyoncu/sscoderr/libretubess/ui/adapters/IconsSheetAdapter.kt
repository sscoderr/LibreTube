package com.bimilyoncu.sscoderr.libretubess.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.databinding.AppIconItemBinding
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.ThemeHelper
import com.bimilyoncu.sscoderr.libretubess.ui.viewholders.IconsSheetViewHolder

class IconsSheetAdapter : RecyclerView.Adapter<IconsSheetViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconsSheetViewHolder {
        val binding = AppIconItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconsSheetViewHolder(binding)
    }

    override fun getItemCount() = availableIcons.size

    override fun onBindViewHolder(holder: IconsSheetViewHolder, position: Int) {
        val appIcon = availableIcons[position]
        holder.binding.apply {
            iconIV.setImageResource(appIcon.iconResource)
            iconName.text = root.context.getString(appIcon.nameResource)
            root.setOnClickListener {
                PreferenceHelper.putString(PreferenceKeys.APP_ICON, appIcon.activityAlias)
                ThemeHelper.changeIcon(root.context, appIcon.activityAlias)
            }
        }
    }

    companion object {
        sealed class AppIcon(
            @StringRes val nameResource: Int,
            @DrawableRes val iconResource: Int,
            val activityAlias: String
        ) {
            object Default :
                AppIcon(R.string.defaultIcon, R.mipmap.ic_launcher, "Default")

            object DefaultLight :
                AppIcon(R.string.defaultIconLight, R.mipmap.ic_launcher_light, "DefaultLight")

            object Legacy : AppIcon(R.string.legacyIcon, R.mipmap.ic_legacy, "IconLegacy")
            object Gradient :
                AppIcon(R.string.gradientIcon, R.mipmap.ic_gradient, "IconGradient")

            object Fire : AppIcon(R.string.fireIcon, R.mipmap.ic_fire, "IconFire")
            object Torch : AppIcon(R.string.torchIcon, R.mipmap.ic_torch, "IconTorch")
            object Shaped : AppIcon(R.string.shapedIcon, R.mipmap.ic_shaped, "IconShaped")
            object Flame : AppIcon(R.string.flameIcon, R.mipmap.ic_flame, "IconFlame")
            object Bird : AppIcon(R.string.birdIcon, R.mipmap.ic_bird, "IconBird")
        }

        val availableIcons = listOf(
            AppIcon.Default,
            AppIcon.DefaultLight,
            AppIcon.Legacy,
            AppIcon.Gradient,
            AppIcon.Fire,
            AppIcon.Torch,
            AppIcon.Shaped,
            AppIcon.Flame,
            AppIcon.Bird
        )
    }
}
