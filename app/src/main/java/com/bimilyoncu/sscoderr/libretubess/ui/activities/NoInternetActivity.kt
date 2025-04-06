package com.bimilyoncu.sscoderr.libretubess.ui.activities

import android.content.Intent
import android.os.Bundle
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.ActivityNointernetBinding
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.fragments.AudioPlayerFragment

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
