package com.bimilyoncu.sscoderr.libretubess.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper

abstract class DynamicLayoutManagerFragment(@LayoutRes layoutResId: Int) : Fragment(layoutResId) {
    abstract fun setLayoutManagers(gridItems: Int)

    private fun getGridItemsCount(orientation: Int): Int {
        val (prefKey, defaultValueRes) = when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PreferenceKeys.GRID_COLUMNS_PORTRAIT to R.integer.grid_items
            else -> PreferenceKeys.GRID_COLUMNS_LANDSCAPE to R.integer.grid_items_landscape
        }

        val defaultValue = resources.getInteger(defaultValueRes).toString()
        return PreferenceHelper.getString(prefKey, defaultValue).toInt()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        setLayoutManagers(getGridItemsCount(newConfig.orientation))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLayoutManagers(getGridItemsCount(resources.configuration.orientation))
    }
}
