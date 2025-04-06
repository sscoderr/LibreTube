package com.bimilyoncu.sscoderr.libretubess.helpers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.databinding.CustomDialogRateAppBinding

/**
 * Helper class to manage the rate app dialog functionality
 */
object RateDialogHelper {
    private const val PREF_KEY_RATE_DIALOG = "rate_dialog_preferences"
    private const val PREF_KEY_DONT_SHOW_AGAIN = "dont_show_rate_dialog_again"
    private const val PREF_KEY_APP_RATED = "app_rated"

    /**
     * Check if the rate dialog should be shown
     * @param context Context to access SharedPreferences
     * @return true if the dialog should be shown, false otherwise
     */
    fun shouldShowRateDialog(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_KEY_RATE_DIALOG, Context.MODE_PRIVATE)
        val dontShowAgain = prefs.getBoolean(PREF_KEY_DONT_SHOW_AGAIN, false)
        val appRated = prefs.getBoolean(PREF_KEY_APP_RATED, false)
        
        return !dontShowAgain && !appRated
    }

    /**
     * Mark the app as rated
     * @param context Context to access SharedPreferences
     */
    private fun setAppRated(context: Context) {
        val prefs = context.getSharedPreferences(PREF_KEY_RATE_DIALOG, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_KEY_APP_RATED, true).apply()
    }

    /**
     * Set the don't show again preference
     * @param context Context to access SharedPreferences
     * @param dontShowAgain Value to set
     */
    private fun setDontShowAgain(context: Context, dontShowAgain: Boolean) {
        val prefs = context.getSharedPreferences(PREF_KEY_RATE_DIALOG, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PREF_KEY_DONT_SHOW_AGAIN, dontShowAgain).apply()
    }

    /**
     * Show the rate app dialog with exit confirmation
     * @param activity Activity context
     * @param onExit Callback when user chooses to exit
     * @return The created AlertDialog
     */
    fun showRateDialog(activity: Activity, onExit: () -> Unit): AlertDialog {
        // Use View Binding for the dialog layout
        val binding = CustomDialogRateAppBinding.inflate(LayoutInflater.from(activity))
        
        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(false)
            .create()
        
        // Setup click listeners
        binding.rateDialogRateButton.setOnClickListener {
            openPlayStore(activity)
            setAppRated(activity)
            dialog.dismiss()
        }
        
        binding.rateDialogExitButton.setOnClickListener {
            // Check if "Don't show again" is checked
            val dontShowAgain = binding.rateDialogDontShowAgain.isChecked
            if (dontShowAgain) {
                setDontShowAgain(activity, true)
            }
            
            dialog.dismiss()
            onExit()
        }
        
        dialog.show()
        return dialog
    }

    /**
     * Open the Google Play Store to rate the app
     * @param context Context to start the intent
     */
    private fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            // Try to open the Play Store app directly
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            )
        } catch (e: ActivityNotFoundException) {
            // If Play Store app is not available, open the website
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
} 