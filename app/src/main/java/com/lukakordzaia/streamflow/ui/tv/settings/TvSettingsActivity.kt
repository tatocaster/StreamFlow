package com.lukakordzaia.streamflow.ui.tv.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.lukakordzaia.streamflow.databinding.ActivityTvSettingsBinding
import com.lukakordzaia.streamflow.interfaces.OnSettingsSelected
import com.lukakordzaia.streamflow.sharedpreferences.AuthSharedPreferences
import com.lukakordzaia.streamflow.ui.phone.profile.ProfileViewModel
import com.lukakordzaia.streamflow.utils.setGone
import com.lukakordzaia.streamflow.utils.setVisible
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TvSettingsActivity: FragmentActivity(), OnSettingsSelected {
    private val profileViewModel: ProfileViewModel by viewModel()
    private val authSharedPreferences: AuthSharedPreferences by inject()
    private lateinit var binding: ActivityTvSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTvSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileViewModel.getUserData()
    }

    override fun getSettingsType(type: Int) {
        when (type) {
            0 -> {
                binding.traktContainer.setVisible()
                hideViews(listOf(binding.infoContainer, binding.deleteContainer, binding.signOutContainer))
            }
            1 -> {
                binding.infoContainer.setVisible()
                hideViews(listOf(binding.traktContainer, binding.deleteContainer, binding.signOutContainer))
            }
            2 -> {
                binding.deleteContainer.setVisible()
                hideViews(listOf(binding.traktContainer, binding.infoContainer, binding.signOutContainer))
            }
            3 -> {
                binding.signOutContainer.setVisible()
                hideViews(listOf(binding.traktContainer, binding.infoContainer, binding.deleteContainer))

                if (authSharedPreferences.getLoginToken() != "") {
                    profileViewModel.userData.observe(this, {
                        binding.profileName.text = "შესული ხართ როგორც ${it.displayName}"
                        Glide.with(this).load(it.avatar.large).into(binding.profilePhoto)
                    })
                }

            }
        }
    }

    private fun hideViews(views: List<View>) {
        views.forEach {
            it.setGone()
        }
    }
}