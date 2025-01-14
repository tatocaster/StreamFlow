package com.lukakordzaia.core.baseclasses

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.ui.CaptionStyleCompat
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.lukakordzaia.core.utils.AppConstants
import com.lukakordzaia.core.R
import com.lukakordzaia.core.databinding.ContinueWatchingDialogBinding
import com.lukakordzaia.core.datamodels.TitleMediaItemsUri
import com.lukakordzaia.core.datamodels.VideoPlayerData
import com.lukakordzaia.core.utils.*
import com.lukakordzaia.core.videoplayer.BuildMediaSource
import com.lukakordzaia.core.videoplayer.MediaPlayerClass
import com.lukakordzaia.core.videoplayer.VideoPlayerViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseVideoPlayerFragment<VB: ViewBinding> : BaseFragmentVM<VB, VideoPlayerViewModel>() {
    override val viewModel by sharedViewModel<VideoPlayerViewModel>()
    private val buildMediaSource: BuildMediaSource by inject()

    protected lateinit var videoPlayerData: VideoPlayerData

    protected lateinit var mediaPlayer: MediaPlayerClass
    protected lateinit var player: SimpleExoPlayer

    protected var mediaItemsPlayed = 0
    var episodeHasEnded = false

    private var numOfSeasons: Int = 0

    protected abstract val autoBackPress: AutoBackPress
    private var tracker: ProgressTracker? = null

    abstract val playerView: PlayerView?
    abstract val subtitleButton: ImageButton
    abstract val playerTitle: TextView
    abstract val nextButton: ImageButton
    abstract val exoDuration: TextView
    abstract val continueWatchingDialog: ContinueWatchingDialogBinding

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24) {
            initPlayer()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoPlayerData = requireActivity().intent!!.getParcelableExtra<VideoPlayerData>(AppConstants.VIDEO_PLAYER_DATA) as VideoPlayerData
        viewModel.setVideoPlayerData(videoPlayerData)

        setUpPlayer()
        initObservers()

        playerHasStarted()
        mediaPlayer.setPlayerListener(MediaTransitionListener())
    }

    private fun setUpPlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        mediaPlayer = MediaPlayerClass(player)
    }

    private fun playerHasStarted() {
        sharedPreferences.saveRefreshContinueWatching(videoPlayerData.trailerUrl == null)
    }

    private fun initObservers() {
        viewModel.videoPlayerData.observe(viewLifecycleOwner, {
            videoPlayerData = it
        })

        viewModel.totalEpisodesInSeason.observe(viewLifecycleOwner, {
            updateNextButton(it)
        })

        viewModel.setTitleName.observe(viewLifecycleOwner, {
            setTitleName(it)
        })
    }

    private fun setTitleName(name: String) {
        playerTitle.setVisible()
        if (videoPlayerData.trailerUrl != null) {
            playerTitle.text = getString(R.string.trailer)
        } else {
            if (videoPlayerData.isTvShow) {
                playerTitle.text = getString(R.string.episode_title, videoPlayerData.chosenSeason.toString(), videoPlayerData.chosenEpisode.toString(), name)
            } else {
                playerTitle.text = name
            }
        }
    }

    private fun initPlayer() {
        if (videoPlayerData.trailerUrl != null) {
            mediaPlayer.setPlayerMediaSource(buildMediaSource.mediaSource(
                TitleMediaItemsUri(videoPlayerData.trailerUrl, null)
            ))
            subtitleButton.setGone()
        } else {
            viewModel.mediaAndSubtitle.observe(viewLifecycleOwner, {
                mediaPlayer.setPlayerMediaSource(buildMediaSource.mediaSource(it))
            })
        }

        subtitleFunctions()
        mediaPlayer.initPlayer(playerView, 0, videoPlayerData.watchedTime)
    }

    private fun subtitleFunctions() {
        val style = CaptionStyleCompat(Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.BLACK, Typeface.DEFAULT_BOLD)
        playerView?.subtitleView!!.apply {
            setPadding(0, 0, 0, 20)
            setApplyEmbeddedFontSizes(false)
            setApplyEmbeddedStyles(false)
            setFixedTextSize(2, 25F)
            setStyle(style)
        }
    }

    fun switchAudioLanguage(language: String) {
        episodeHasEnded = false
        viewModel.setVideoPlayerData(videoPlayerData.copy(chosenLanguage = language, watchedTime = player.currentPosition))
        player.clearMediaItems()
        mediaPlayer.initPlayer(playerView, 0, videoPlayerData.watchedTime)
    }

    fun switchSubtitleLanguage(language: String) {
        if (language == getString(R.string.turn_off)) {
            playerView?.subtitleView?.setGone()
            videoPlayerData = videoPlayerData.copy(chosenSubtitle = language)
            player.play()
        } else {
            episodeHasEnded = false
            playerView?.subtitleView?.setVisible()
            viewModel.setVideoPlayerData(videoPlayerData.copy(chosenSubtitle = language, watchedTime = player.currentPosition))
            player.clearMediaItems()
            mediaPlayer.initPlayer(playerView, 0, videoPlayerData.watchedTime)
        }
    }

    private fun updateNextButton(lastEpisode: Int) {
        //quick fix before viewState is introduced
        viewModel.numOfSeasons.observe(viewLifecycleOwner, { numOfSeasons ->
            nextButton.setVisibleOrGone(
                !(videoPlayerData.chosenSeason == numOfSeasons && videoPlayerData.chosenEpisode == lastEpisode) &&
                        videoPlayerData.isTvShow && videoPlayerData.trailerUrl == null
            )

            nextButton.setOnClickListener {
                nextButtonFunction(videoPlayerData.chosenEpisode == lastEpisode, numOfSeasons)
            }
        })
    }

    private fun nextButtonFunction(isLastEpisode: Boolean, numOfSeasons: Int) {
        saveCurrentProgress()

        if (videoPlayerData.isTvShow) {
            if (!(videoPlayerData.chosenSeason == numOfSeasons && isLastEpisode)) {
                episodeHasEnded = false
                player.clearMediaItems()
                viewModel.setVideoPlayerData(VideoPlayerData(
                    videoPlayerData.titleId,
                    videoPlayerData.isTvShow,
                    if (isLastEpisode) videoPlayerData.chosenSeason+1 else videoPlayerData.chosenSeason,
                    videoPlayerData.chosenLanguage,
                    if (isLastEpisode) 1 else videoPlayerData.chosenEpisode+1,
                    0L,
                    null,
                    videoPlayerData.chosenSubtitle,
                ))
                mediaPlayer.initPlayer(playerView, 0, 0L)
                viewModel.episodeHasStarted()
            } else {
                requireActivity().onBackPressed()
            }
        } else {
            requireActivity().onBackPressed()
        }
    }

    fun saveCurrentProgress() {
        if (videoPlayerData.trailerUrl == null) {
            viewModel.addContinueWatching(player.currentPosition, player.duration)
        }
    }

    fun releasePlayer() {
        mediaPlayer.releasePlayer {
            if (videoPlayerData.trailerUrl == null) {
                viewModel.addContinueWatching(it.playbackPosition, it.titleDuration)
            }
        }
    }

    private fun showContinueWatchingDialog() {
        if (mediaItemsPlayed == MAX_MEDIA_PLAYED) {
            viewModel.addContinueWatching(player.currentPosition, player.duration)
            player.pause()

            continueWatchingDialog.root.setVisible()
            continueWatchingDialog.confirmButton.setOnClickListener {
                continueWatchingDialog.root.setGone()
                player.play()
            }

            continueWatchingDialog.goBackButton.setOnClickListener {
                requireActivity().onBackPressed()
            }

            mediaItemsPlayed = 0
        }
    }

    override fun onPause() {
        if (Util.SDK_INT >= 24) {
            saveCurrentProgress()
            player.pause()
            viewModel.setVideoPlayerData(videoPlayerData.copy(watchedTime = player.currentPosition))
        }
        super.onPause()
    }

    override fun onDetach() {
        tracker?.purgeHandler()
        autoBackPress.cancel()

        super.onDetach()
    }

    inner class MediaTransitionListener: Player.Listener {
        override fun onCues(cues: MutableList<Cue>) {
            super.onCues(cues)
            playerView?.subtitleView!!.setCues(cues)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

            if (isPlaying) autoBackPress.cancel() else autoBackPress.start()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (mediaItem != null) {
                    if (videoPlayerData.trailerUrl == null) {
                        viewModel.addContinueWatching(player.currentPosition, player.duration)
                    }
                }
            }, 2000)
        }

        override fun onPlaybackStateChanged(state: Int) {
            super.onPlaybackStateChanged(state)
            playerView?.keepScreenOn = !(state == Player.STATE_IDLE || state == Player.STATE_ENDED)

            when (state) {
                Player.STATE_READY -> {
                    tracker = ProgressTracker(player, exoDuration)

                    episodeHasEnded = true
                    showContinueWatchingDialog()
                }
                Player.STATE_ENDED -> {
                    nextButton.setInvisible()
                    if (episodeHasEnded) {
                        mediaItemsPlayed++
                        nextButton.callOnClick()
                    }
                }
            }
        }
    }

    inner class AutoBackPress(private val backPress: () -> Unit): CountDownTimer(AUTO_BACK_COUNTER_TIME.toLong(), AUTO_BACK_COUNTER_INTERVAL.toLong()) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            backPress.invoke()
        }
    }

    inner class ProgressTracker(private val player: Player, private val duration: TextView) : Runnable {
        private val handler: Handler = Handler(Looper.myLooper()!!)
        override fun run() {
            val position = if (player.duration <= 0) 0 else player.duration - player.currentPosition
            duration.text = position.videoPlayerPosition()
            handler.postDelayed(this, PLAYER_TIME_INTERVAL.toLong())
        }

        fun purgeHandler() {
            handler.removeCallbacks(this)
        }

        init {
            handler.post(this)
        }
    }

    companion object {
        const val MAX_MEDIA_PLAYED = 3
        const val AUTO_BACK_COUNTER_TIME = 500000
        const val AUTO_BACK_COUNTER_INTERVAL = 1000
        const val PLAYER_TIME_INTERVAL = 1000
    }
}