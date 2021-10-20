package com.lukakordzaia.streamflow.ui.phone.videoplayer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import com.lukakordzaia.streamflow.databinding.FragmentPhoneVideoPlayerBinding
import com.lukakordzaia.streamflow.databinding.PhoneExoplayerControllerLayoutBinding
import com.lukakordzaia.streamflow.datamodels.TitleMediaItemsUri
import com.lukakordzaia.streamflow.datamodels.VideoPlayerInfo
import com.lukakordzaia.streamflow.ui.baseclasses.fragments.BaseVideoPlayerFragment
import com.lukakordzaia.streamflow.utils.setGone

class VideoPlayerFragment : BaseVideoPlayerFragment<FragmentPhoneVideoPlayerBinding>() {
    private lateinit var playerBinding: PhoneExoplayerControllerLayoutBinding

    private val autoBackPress = autoBackPress {
        requireActivity().onBackPressed()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPhoneVideoPlayerBinding
        get() = FragmentPhoneVideoPlayerBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerBinding = PhoneExoplayerControllerLayoutBinding.bind(binding.root)

        mediaPlayer.setPlayerListener(PlayerListeners())

        playerBinding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        videoPlayerViewModel.videoPlayerInfo.observe(viewLifecycleOwner, {
            videoPlayerInfo = it
            nextButtonClickListener(playerBinding.nextEpisode, binding.phoneTitlePlayer)
            prevButtonClickListener()

            setTitleName(playerBinding.playerTitle)
        })

        if (videoPlayerData.trailerUrl != null) {
            playerBinding.prevEpisode.setGone()
            playerBinding.nextEpisode.setGone()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer(videoPlayerData.watchedTime, videoPlayerData.trailerUrl)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24) {
            initPlayer(videoPlayerData.watchedTime, videoPlayerData.trailerUrl)
        }
    }

    inner class PlayerListeners : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            if (isPlaying) autoBackPress.cancel() else autoBackPress.start()
        }

        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)

            when (state) {
                Player.STATE_READY -> {
                    episodeHasEnded = true
                    showContinueWatchingDialog(binding.continueWatching)
                }
                Player.STATE_ENDED -> {
                    stateHasEnded(playerBinding.nextEpisode)
                }
            }

            binding.phoneTitlePlayer.keepScreenOn = !(state == Player.STATE_IDLE || state == Player.STATE_ENDED)
        }
    }

    private fun prevButtonClickListener() {
        val prevButton = playerBinding.prevEpisode

        if (videoPlayerInfo.isTvShow) {
            if (videoPlayerInfo.chosenEpisode == 1) {
                prevButton.setOnClickListener {
                    mediaItemsPlayed = 0
                    playerBinding.exoPrev.callOnClick()
                }
            } else {
                prevButton.setOnClickListener {
                    mediaItemsPlayed = 0
                    player.clearMediaItems()
                    videoPlayerViewModel.getTitleFiles(VideoPlayerInfo(
                        videoPlayerInfo.titleId,
                        videoPlayerInfo.isTvShow,
                        videoPlayerInfo.chosenSeason,
                        videoPlayerInfo.chosenEpisode-1,
                        videoPlayerInfo.chosenLanguage
                    ))
                    mediaPlayer.initPlayer(binding.phoneTitlePlayer, 0, 0L)
                }
            }
        } else {
            prevButton.setGone()
        }
    }

    private fun initPlayer(watchedTime: Long, trailerUrl: String?) {
        if (trailerUrl != null) {
            mediaPlayer.setMediaItems(listOf(MediaItem.fromUri(Uri.parse(trailerUrl))))
            mediaPlayer.setPlayerMediaSource(buildMediaSource.movieMediaSource(
                TitleMediaItemsUri(MediaItem.fromUri(
                    Uri.parse(trailerUrl)), "0")
            ))
            subtitleFunctions(false)
        } else {
            videoPlayerViewModel.mediaAndSubtitle.observe(viewLifecycleOwner, {
                mediaPlayer.setPlayerMediaSource(buildMediaSource.movieMediaSource(it))

                subtitleFunctions(it.titleSubUri.isNotEmpty() && it.titleSubUri != "0")
            })
        }
        mediaPlayer.initPlayer(binding.phoneTitlePlayer, 0, watchedTime)
    }

    private fun subtitleFunctions(hasSubs: Boolean) {
        subtitleFunctions(binding.phoneTitlePlayer.subtitleView!!, playerBinding.subtitleToggle, hasSubs)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        autoBackPress.cancel()
    }
}