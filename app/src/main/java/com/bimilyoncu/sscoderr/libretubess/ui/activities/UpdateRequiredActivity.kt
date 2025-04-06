package com.bimilyoncu.sscoderr.libretubess.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bimilyoncu.sscoderr.libretubess.BuildConfig
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.ActivityUpdateRequiredBinding
import kotlin.system.exitProcess

/**
 * Activity shown when the app requires an update to continue.
 * This is a blocking activity that prevents the user from using the app until they update.
 */
class UpdateRequiredActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUpdateRequiredBinding
    private var downloadUrl: String = ""
    private var requiredVersion: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateRequiredBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get passed download URL and required version
        downloadUrl = intent.getStringExtra(IntentData.downloadUrl) ?: ""
        requiredVersion = intent.getStringExtra(IntentData.requiredVersion) ?: ""
        
        setupVersionInfo()
        setupButtons()
    }
    
    private fun setupVersionInfo() {
        // Show the current app version
        val currentVersion = BuildConfig.VERSION_NAME
        binding.tvCurrentVersion.text = getString(R.string.current_version, currentVersion)
        
        // Show the required version if available
        if (requiredVersion.isNotEmpty()) {
            binding.tvRequiredVersion.text = getString(R.string.required_version, requiredVersion)
        } else {
            binding.tvRequiredVersion.text = getString(R.string.required_version, "?")
        }
    }
    
    private fun setupButtons() {
        // Set up update button
        binding.btnUpdateNow.setOnClickListener {
            openUpdateUrl()
        }
        
        // Set up exit button
        binding.btnExitApp.setOnClickListener {
            finishAndRemoveTask()
            exitProcess(0)
        }
    }
    
    private fun openUpdateUrl() {
        try {
            // If we have a specific download URL, use it
            if (downloadUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                startActivity(intent)
            } else {
                // Otherwise, open the app's page on the Play Store or GitHub
                val appPackageName = packageName
                try {
                    // Try to open in Play Store app first
                    startActivity(Intent(Intent.ACTION_VIEW, 
                        Uri.parse("market://details?id=$appPackageName")))
                } catch (e: android.content.ActivityNotFoundException) {
                    // Fall back to browser if Play Store app is not available
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/libre-tube/LibreTube/releases/latest")))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Prevents going back to avoid bypassing the update
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing to prevent going back
    }
} 