package com.bimilyoncu.sscoderr.libretube.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.databinding.FragmentNointernetBinding
import com.bimilyoncu.sscoderr.libretube.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretube.helpers.NetworkHelper
import com.bimilyoncu.sscoderr.libretube.ui.activities.SettingsActivity
import com.google.android.material.snackbar.Snackbar

class NoInternetFragment: Fragment(R.layout.fragment_nointernet) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNointernetBinding.bind(view)
        binding.retryButton.setOnClickListener {
            if (NetworkHelper.isNetworkAvailable(requireContext())) {
                NavigationHelper.restartMainActivity(requireContext())
            } else {
                Snackbar.make(binding.root, R.string.turnInternetOn, Snackbar.LENGTH_LONG).show()
            }
        }
        binding.noInternetSettingsImageView.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.downloads.setOnClickListener {
            findNavController().navigate(R.id.downloadsFragment)
        }
    }
}