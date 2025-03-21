package com.bimilyoncu.sscoderr.libretube.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.helpers.ClipboardHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ErrorDialog : DialogFragment() {
    @SuppressLint("PrivateResource")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val errorLog = PreferenceHelper.getErrorLog()
        // reset the error log
        PreferenceHelper.saveErrorLog("")

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.error_occurred)
            .setMessage(errorLog)
            .setNegativeButton(R.string.okay, null)
            .setPositiveButton(androidx.preference.R.string.copy) { _, _ ->
                ClipboardHelper.save(requireContext(), text = errorLog, notify = true)
            }
            .show()
    }
}
