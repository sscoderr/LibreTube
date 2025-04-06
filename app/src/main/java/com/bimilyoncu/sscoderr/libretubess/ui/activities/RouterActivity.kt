package com.bimilyoncu.sscoderr.libretubess.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.bimilyoncu.sscoderr.libretubess.extensions.TAG
import com.bimilyoncu.sscoderr.libretubess.helpers.IntentHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity

class RouterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri() ?: intent.data
        if (uri != null) {
            // Start processing the given text, if available. Otherwise use the link shared as text
            // to the app.
            handleSendText(uri)
        } else {
            // start app as normal if unknown action, shouldn't be reachable
            NavigationHelper.restartMainActivity(this)
        }
    }

    private fun handleSendText(uri: Uri) {
        Log.i(TAG(), uri.toString())

        val intent = packageManager.getLaunchIntentForPackage(packageName)!!.let { intent ->
            IntentHelper.resolveType(intent, uri)
        }
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAndRemoveTask()
    }
}
