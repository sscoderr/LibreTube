package com.bimilyoncu.sscoderr.libretube.ui.activities

import android.content.Intent
import android.os.Bundle
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.databinding.ActivityNointernetBinding
import com.bimilyoncu.sscoderr.libretube.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretube.ui.base.BaseActivity

class NoInternetActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNointernetBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.getBooleanExtra(IntentData.openAudioPlayer, false)) {
            NavigationHelper.openAudioPlayerFragment(this, offlinePlayer = true)
        }
    }
}
