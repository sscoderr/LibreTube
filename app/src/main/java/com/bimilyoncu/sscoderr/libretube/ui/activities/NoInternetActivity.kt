package com.bimilyoncu.sscoderr.libretube.ui.activities

import android.content.Intent
import android.os.Bundle
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.databinding.ActivityNointernetBinding
import com.bimilyoncu.sscoderr.libretube.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretube.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretube.ui.fragments.AudioPlayerFragment

class NoInternetActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNointernetBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.getBooleanExtra(IntentData.openAudioPlayer, false)) {
            // Check if AudioPlayerFragment is already in the fragment container
            val existingAudioPlayer = supportFragmentManager.findFragmentById(R.id.container) as? AudioPlayerFragment
            if (existingAudioPlayer == null) {
                NavigationHelper.openAudioPlayerFragment(this, offlinePlayer = true)
            } else {
                // Fragment exists, just bring it to the foreground by restoring its state
                existingAudioPlayer.maximizePlayer()
            }
        }
    }
}
