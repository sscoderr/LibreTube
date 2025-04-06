package com.bimilyoncu.sscoderr.libretubess.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.bimilyoncu.sscoderr.libretubess.R
import com.google.android.material.button.MaterialButton

/**
 * Custom view binding for dialog_rate_app.xml layout
 */
class CustomDialogRateAppBinding private constructor(
    val root: View,
    val rateDialogTitle: TextView,
    val rateDialogMessage: TextView,
    val rateDialogDontShowAgain: CheckBox,
    val rateDialogExitButton: MaterialButton,
    val rateDialogRateButton: MaterialButton
) {
    companion object {
        /**
         * Inflate binding from layout inflater
         */
        fun inflate(inflater: LayoutInflater): CustomDialogRateAppBinding {
            val root = inflater.inflate(R.layout.dialog_rate_app, null, false)
            return bind(root)
        }

        /**
         * Inflate binding from parent view group
         */
        fun inflate(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): CustomDialogRateAppBinding {
            val root = inflater.inflate(R.layout.dialog_rate_app, parent, attachToParent)
            return bind(root)
        }

        /**
         * Bind layout views to binding
         */
        fun bind(view: View): CustomDialogRateAppBinding {
            val rateDialogTitle = view.findViewById<TextView>(R.id.rate_dialog_title)
            val rateDialogMessage = view.findViewById<TextView>(R.id.rate_dialog_message)
            val rateDialogDontShowAgain = view.findViewById<CheckBox>(R.id.rate_dialog_dont_show_again)
            val rateDialogExitButton = view.findViewById<MaterialButton>(R.id.rate_dialog_exit_button)
            val rateDialogRateButton = view.findViewById<MaterialButton>(R.id.rate_dialog_rate_button)

            return CustomDialogRateAppBinding(
                view,
                rateDialogTitle,
                rateDialogMessage,
                rateDialogDontShowAgain,
                rateDialogExitButton,
                rateDialogRateButton
            )
        }
    }
} 