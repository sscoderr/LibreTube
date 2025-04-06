package com.bimilyoncu.sscoderr.libretubess.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ColorPickerDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ColorPickerDialog.Companion.COLOR_PICKER_REQUEST_KEY

class ColorPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private lateinit var circleView: View
    private var currentColor: Int? = null

    init {
        layoutResource = R.layout.color_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<TextView>(android.R.id.title)?.text = title
        circleView = holder.itemView.findViewById(R.id.circle)
        updateColorView()

        holder.itemView.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun setColor(color: Int) {
        currentColor = color
        persistInt(color)
        updateColorView()
    }

    override fun onGetDefaultValue(ta: TypedArray, index: Int): Any {
        return Color.parseColor(ta.getString(index))
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val color = if (defaultValue is Int) {
            getPersistedInt(defaultValue)
        } else {
            getPersistedInt(Color.WHITE)
        }
        currentColor = color
        persistInt(color)
    }

    private fun updateColorView() {
        (if (currentColor is Int) currentColor else Color.WHITE)?.let {
            circleView.setBackgroundColor(it)
        }
    }

    private fun showColorPickerDialog() {
        (if (currentColor is Int) currentColor else Color.BLACK)?.let {
            val bundle = bundleOf(IntentData.color to it)
            val dialog = ColorPickerDialog()
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            fragmentManager.setFragmentResultListener(
                COLOR_PICKER_REQUEST_KEY,
                context as AppCompatActivity
            ) { _, resultBundle ->
                val newColor = resultBundle.getInt(IntentData.color)
                setColor(newColor)
            }
            dialog.arguments = bundle
            dialog.show(fragmentManager, this::class.java.name)
        }
    }

    override fun getTitle(): CharSequence? {
        return "${super.getTitle()}:"
    }
}
