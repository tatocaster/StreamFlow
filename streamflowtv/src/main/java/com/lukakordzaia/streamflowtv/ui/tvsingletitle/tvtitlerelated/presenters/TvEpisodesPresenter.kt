package com.lukakordzaia.streamflowtv.ui.tvsingletitle.tvtitlerelated.presenters

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.leanback.widget.Presenter
import com.lukakordzaia.core.datamodels.TitleEpisodes
import com.lukakordzaia.streamflowtv.R
import com.lukakordzaia.streamflowtv.customviews.TvEpisodesCardView

class TvEpisodesPresenter(private val context: Context) : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = TvEpisodesCardView(parent.context, null)

        cardView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.background_episodes_card_tv, null)

        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        cardView.layoutParams = ViewGroup.LayoutParams(width, height)

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val episode = item as TitleEpisodes
        val cardView = viewHolder.view as TvEpisodesCardView

//        if (isSeason) {
//            cardView.currentIndicatorVisibility(episode.episodeNum == currentEpisode)
//        }

        cardView.setNumber("ეპიზოდი ${episode.episodeNum}")
        cardView.setName(episode.episodeName)
        cardView.setPoster(episode.episodePoster)

        cardView.setSeekbarVisibility(episode.titleDuration?.toInt() != 0)
        if (episode.titleDuration?.toInt() != 0) {
            cardView.binding.itemSeekBar.max = episode.titleDuration!!.toInt()
            cardView.binding.itemSeekBar.progress = episode.watchDuration!!.toInt()
        }

        cardView.setOnFocusChangeListener { _, hasFocus ->
//            if (episode.episodeNum == currentEpisode) {
//                cardView.setIndicatorDrawable(
//                    ResourcesCompat.getDrawable(
//                        context.resources,
//                        if (hasFocus) R.drawable.indicator_current_episode_dark else R.drawable.indicator_current_episode_yellow,
//                        null
//                    )
//                )
//            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}

}