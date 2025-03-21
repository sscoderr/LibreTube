package com.bimilyoncu.sscoderr.libretube.ui.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretube.helpers.LocaleHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretube.helpers.ThemeHelper
import com.bimilyoncu.sscoderr.libretube.helpers.WindowHelper
import java.util.Locale

/**
 * Activity that applies the LibreTube theme and the in-app language
 */
open class BaseActivity : AppCompatActivity() {
    open val isDialogActivity: Boolean = false

    val screenOrientationPref by lazy {
        val orientationPref = PreferenceHelper.getString(
            PreferenceKeys.ORIENTATION,
            resources.getString(R.string.config_default_orientation_pref)
        )
        when (orientationPref) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            "landscape" -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
            "auto" -> ActivityInfo.SCREEN_ORIENTATION_USER
            else -> throw IllegalArgumentException()
        }
    }

    /**
     * Whether the phone of the user has a cutout like a notch or not
     */
    var hasCutout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // set the app theme (e.g. Material You)
        ThemeHelper.updateTheme(this)
        if (isDialogActivity) ThemeHelper.applyDialogActivityTheme(this)

        requestOrientationChange()

        // wait for the window decor view to be drawn before detecting display cutouts
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            hasCutout = WindowHelper.hasCutout(view)
            window.decorView.onApplyWindowInsets(insets)
        }

        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        // change the locale according to the user's preference (or system language as fallback)
        val locale = LocaleHelper.getAppLocale()
        Locale.setDefault(locale)

        val configuration = Configuration().apply {
            setLocale(locale)
        }

        applyOverrideConfiguration(configuration)
    }

    /**
     * Rotate the screen according to the app orientation preference
     */
    open fun requestOrientationChange() {
        requestedOrientation = screenOrientationPref
    }
}
