package com.bimilyoncu.sscoderr.libretubess.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.ui.activities.UpdateRequiredActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Helper class for app version control functionality
 */
object VersionControlHelper {
    
    private const val VERSION_CHECK_URL = "https://sscoderr.com/versionController/LibreTube/version-controller.txt"
    private const val DOWNLOAD_LINK_URL = "https://sscoderr.com/versionController/LibreTube/url.txt"
    private const val TAG = "VersionControlHelper"
    
    // Store server feature control version to determine feature visibility
    private var serverFeatureControlVersion: Int? = null
    
    // Cache for control features visibility to avoid blocking calls
    private var controlFeaturesVisibilityCache: Boolean? = null
    
    /**
     * Gets the cached feature control status
     * @return cached status (true = visible, false = hidden) or null if not cached yet
     */
    fun getCachedControlFeaturesStatus(): Boolean? {
        return controlFeaturesVisibilityCache
    }

    /**
     * Central method to determine if controlled features should be shown
     * @param context Application context
     * @return true if features should be visible, false otherwise
     */
    suspend fun shouldShowControls(context: Context): Boolean {
        if (controlFeaturesVisibilityCache != null) {
            return controlFeaturesVisibilityCache!!
        }
        
        if (serverFeatureControlVersion == null) {
            // If we haven't checked server version yet, read it
            val versions = readVersionInfo()
            serverFeatureControlVersion = versions.getOrNull(1)?.toIntOrNull()
        }
        
        // Get current app version code
        val currentVersionCode = getCurrentAppVersionCode(context)
        
        // If server version is null or doesn't match current version, show features
        val shouldShow = serverFeatureControlVersion == null || currentVersionCode != serverFeatureControlVersion
        
        // Cache the result
        controlFeaturesVisibilityCache = shouldShow
        
        return shouldShow
    }

    /**
     * Gets the current app version code
     * @param context Application context
     * @return Current app version code as int, or 0 if can't be determined
     */
    private fun getCurrentAppVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            
            // In newer Android versions, use longVersionCode and cast to Int
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Error getting app version code", e)
            0
        }
    }

    
    /**
     * Reads the version information from server
     * @return List of version strings, where first is required version and second is control version
     */
    private suspend fun readVersionInfo(): List<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        
        try {
            val url = URL(VERSION_CHECK_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = BufferedReader(InputStreamReader(connection.inputStream))
                val versions = mutableListOf<String>()
                
                // Read line by line, similar to the Java implementation
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line != null) {
                        versions.add(line!!)
                    }
                }
                
                Log.d(TAG, "Server versions: $versions")
                versions
            } else {
                Log.e(TAG, "Error reading version, HTTP response code: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking version", e)
            emptyList()
        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }
    
    /**
     * Gets download link from server
     */
    private suspend fun getDownloadLink(): String = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        
        try {
            val url = URL(DOWNLOAD_LINK_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = BufferedReader(InputStreamReader(connection.inputStream))
                val downloadUrl = reader.readLine() ?: ""
                Log.d(TAG, "Download URL: $downloadUrl")
                downloadUrl
            } else {
                Log.e(TAG, "Error reading download link, HTTP response code: $responseCode")
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting download link", e)
            ""
        } finally {
            reader?.close()
            connection?.disconnect()
        }
    }
    
    /**
     * Sealed class representing the result of an update check
     */
    sealed class UpdateCheckResult {
        /**
         * No update is required, the app is up to date
         */
        object UpToDate : UpdateCheckResult()
        
        /**
         * An update is available, but not mandatory
         * @param downloadUrl URL to download the update
         * @param requiredVersion String representation of the required version
         */
        data class UpdateAvailable(
            val downloadUrl: String,
            val requiredVersion: String
        ) : UpdateCheckResult()
        
        /**
         * A mandatory update is required, the app should block usage
         * @param downloadUrl URL to download the update
         * @param requiredVersion String representation of the required version
         */
        data class MandatoryUpdateRequired(
            val downloadUrl: String,
            val requiredVersion: String
        ) : UpdateCheckResult()
        
        /**
         * An error occurred during the update check
         * @param error The exception that occurred
         */
        data class Error(val error: Exception) : UpdateCheckResult()
    }

    /**
     * Checks if app needs an update with improved structure
     * @param activity Activity context
     * @param isMandatoryCheck If true, will return MandatoryUpdateRequired result for required updates
     * @return UpdateCheckResult indicating the status of the update check
     */
    suspend fun checkForUpdates(
        activity: FragmentActivity,
        isMandatoryCheck: Boolean = false
    ): UpdateCheckResult {
        try {
            val versions = readVersionInfo()
            
            // First value in versions is the required app version
            val requiredVersionCode = versions.firstOrNull()?.toIntOrNull() ?: 0
            
            // Second value is the feature control version
            serverFeatureControlVersion = versions.getOrNull(1)?.toIntOrNull()
            
            // Get current app version
            val currentVersionCode = getCurrentAppVersionCode(activity)
            
            if (requiredVersionCode > 0 && currentVersionCode < requiredVersionCode) {
                // App needs an update
                val downloadUrl = getDownloadLink()
                
                if (downloadUrl.isEmpty()) {
                    return UpdateCheckResult.Error(Exception("Failed to retrieve download URL"))
                }
                
                val requiredVersionString = "v$requiredVersionCode"
                
                return if (isMandatoryCheck) {
                    UpdateCheckResult.MandatoryUpdateRequired(downloadUrl, requiredVersionString)
                } else {
                    UpdateCheckResult.UpdateAvailable(downloadUrl, requiredVersionString)
                }
            } else {
                // Preload features visibility to cache
                shouldShowControls(activity)
                return UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in update check", e)
            return UpdateCheckResult.Error(e)
        }
    }

    /**
     * Shows the blocking update required screen
     * @param activity The activity context
     * @param downloadUrl The URL to download the update from
     * @param requiredVersion The required version string to display
     */
    fun showMandatoryUpdateScreen(
        activity: FragmentActivity,
        downloadUrl: String,
        requiredVersion: String
    ) {
        val intent = Intent(activity, UpdateRequiredActivity::class.java).apply {
            putExtra(IntentData.downloadUrl, downloadUrl)
            putExtra(IntentData.requiredVersion, requiredVersion)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        activity.startActivity(intent)
        activity.finish()
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use the new checkForUpdates method that returns UpdateCheckResult
     */
    @Deprecated("Use the new checkForUpdates method that returns UpdateCheckResult")
    suspend fun checkForUpdates(
        activity: FragmentActivity,
        onUpdateAvailable: (String) -> Unit = {},
        onUpdateNotNeeded: () -> Unit = {},
        onError: () -> Unit = {},
        isBlockingUpdateCheck: Boolean = false
    ) {
        when (val result = checkForUpdates(activity, isBlockingUpdateCheck)) {
            is UpdateCheckResult.UpToDate -> {
                onUpdateNotNeeded()
            }
            is UpdateCheckResult.UpdateAvailable -> {
                onUpdateAvailable(result.downloadUrl)
            }
            is UpdateCheckResult.MandatoryUpdateRequired -> {
                showMandatoryUpdateScreen(activity, result.downloadUrl, result.requiredVersion)
            }
            is UpdateCheckResult.Error -> {
                onError()
            }
        }
    }

} 