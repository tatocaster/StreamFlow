package com.lukakordzaia.streamflow.ui.tv.tvvideoplayer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.exoplayer2.util.Util
import com.lukakordzaia.streamflow.R
import com.lukakordzaia.streamflow.datamodels.VideoPlayerData
import com.lukakordzaia.streamflow.ui.baseclasses.BaseVideoPlayerFragmentNew
import kotlinx.android.synthetic.main.tv_exoplayer_controller_layout.*
import kotlinx.android.synthetic.main.tv_video_player_fragment.*


open class TvVideoPlayerFragment : BaseVideoPlayerFragmentNew(R.layout.tv_video_player_fragment) {
    private lateinit var videoPlayerData: VideoPlayerData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setExoPlayer(tv_title_player)

        tv_exo_back.setOnClickListener {
            requireActivity().onBackPressed()
            requireActivity().onBackPressed()
        }

        val titleId = activity?.intent?.getSerializableExtra("titleId") as Int
        val chosenLanguage = activity?.intent?.getSerializableExtra("chosenLanguage") as String
        val chosenSeason = activity?.intent?.getSerializableExtra("chosenSeason") as Int
        val isTvShow = activity?.intent?.getSerializableExtra("isTvShow") as Boolean
        val chosenEpisode = activity?.intent?.getSerializableExtra("chosenEpisode") as Int
        val watchedTime = activity?.intent?.getSerializableExtra("watchedTime") as Long
        val trailerUrl: String? = activity?.intent?.getSerializableExtra("trailerUrl") as String?

        videoPlayerData = VideoPlayerData(
            titleId,
            isTvShow,
            chosenSeason,
            chosenLanguage,
            chosenEpisode,
            watchedTime,
            trailerUrl
        )

        if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getPlayListFiles(videoPlayerData.titleId, videoPlayerData.chosenSeason, videoPlayerData.chosenEpisode, videoPlayerData.chosenLanguage, videoPlayerData.isTvShow)
        }


    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer(videoPlayerData.watchedTime, videoPlayerData.trailerUrl)
            Log.d("videoplaying", "started")
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24) {
            initPlayer(videoPlayerData.watchedTime, videoPlayerData.trailerUrl)
            Log.d("videoplaying", "started")
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
            Log.d("videoplaying", "paused")
        }
    }

    override fun onStop() {
        if (Util.SDK_INT >= 24) {
            releasePlayer()
            if (!tv_title_player.isControllerVisible) {
                requireActivity().onBackPressed()
            } else {
                requireActivity().onBackPressed()
                requireActivity().onBackPressed()
            }
            super.onStop()
            Log.d("videoplaying", "stopped")
        }
    }
}