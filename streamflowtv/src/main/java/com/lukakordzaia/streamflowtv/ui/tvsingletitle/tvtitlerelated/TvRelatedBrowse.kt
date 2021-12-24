package com.lukakordzaia.streamflowtv.ui.tvsingletitle.tvtitlerelated

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.leanback.widget.*
import androidx.leanback.widget.BrowseFrameLayout.OnFocusSearchListener
import androidx.recyclerview.widget.GridLayoutManager
import com.lukakordzaia.core.utils.AppConstants
import com.lukakordzaia.core.adapters.ChooseLanguageAdapter
import com.lukakordzaia.core.databinding.DialogChooseLanguageBinding
import com.lukakordzaia.core.datamodels.SingleTitleModel
import com.lukakordzaia.core.datamodels.TitleEpisodes
import com.lukakordzaia.core.datamodels.VideoPlayerData
import com.lukakordzaia.core.network.models.imovies.response.singletitle.GetSingleTitleCastResponse
import com.lukakordzaia.streamflowtv.R
import com.lukakordzaia.streamflowtv.baseclasses.BaseBrowseSupportFragment
import com.lukakordzaia.streamflowtv.ui.main.presenters.TvMainPresenter
import com.lukakordzaia.streamflowtv.ui.tvsingletitle.TvSingleTitleActivity
import com.lukakordzaia.streamflowtv.ui.tvsingletitle.tvtitlerelated.presenters.TvCastPresenter
import com.lukakordzaia.streamflowtv.ui.tvsingletitle.tvtitlerelated.presenters.TvEpisodesPresenter
import com.lukakordzaia.streamflowtv.ui.tvsingletitle.tvtitlerelated.presenters.TvSeasonsPresenter
import com.lukakordzaia.streamflowtv.ui.tvsingletitle.tvtitlerelated.presenters.TvSimilarPresenter
import com.lukakordzaia.streamflowtv.ui.tvvideoplayer.TvVideoPlayerActivity
import com.lukakordzaia.streamflowtv.ui.tvwatchlist.TvWatchlistActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class TvRelatedBrowse : BaseBrowseSupportFragment<TvRelatedViewModel>() {
    var titleId: Int? = 0
    var isTvShow: Boolean? = false

    override val viewModel by viewModel<TvRelatedViewModel>()
    override val reload: () -> Unit = {
        setSeasonsAndEpisodes(titleId!!, isTvShow!!)
        setTitleCast(titleId!!, isTvShow!!)
        setTitleRelated(titleId!!, isTvShow!!)
    }
    private lateinit var chooseLanguageAdapter: ChooseLanguageAdapter

    private var hasFocus = false
    private var focusedSeason = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        workaroundFocus()
    }

    override fun onStart() {
        super.onStart()

        setSeasonsAndEpisodes(titleId!!, isTvShow!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleIdFromDetails = activity?.intent?.getSerializableExtra(AppConstants.TITLE_ID) as? Int
        val isTvShowFromDetails = activity?.intent?.getSerializableExtra(AppConstants.IS_TV_SHOW) as? Boolean
        val videoPlayerData = activity?.intent?.getParcelableExtra(AppConstants.VIDEO_PLAYER_DATA) as? VideoPlayerData

        titleId = titleIdFromDetails ?: videoPlayerData?.titleId
        isTvShow = isTvShowFromDetails ?: videoPlayerData?.isTvShow

        initRowsAdapter(isTvShow!!)
        setTitleCast(titleId!!, isTvShow!!)
        setTitleRelated(titleId!!, isTvShow!!)

        setupEventListeners(ItemViewClickedListener(), ItemViewSelectedListener(titleId!!))
    }

    private fun initRowsAdapter(isTvShow: Boolean) {
        if (isTvShow) {
            val secondHeaderItem = ListRow(HeaderItem(0, "სეზონები"), ArrayObjectAdapter(TvSeasonsPresenter()))
            val firstHeaderItem = ListRow(HeaderItem(1, "ეპიზოდები"), ArrayObjectAdapter(TvMainPresenter()))
            val fourthItem = ListRow(HeaderItem(2, getString(R.string.actors)), ArrayObjectAdapter(TvCastPresenter()))
            val fifthItem = ListRow(HeaderItem(3, "მსგავსი"), ArrayObjectAdapter(TvSimilarPresenter()))
            val initListRows = mutableListOf(firstHeaderItem, secondHeaderItem, fourthItem, fifthItem)
            rowsAdapter.addAll(0, initListRows)
        } else {
            val fourthItem = ListRow(HeaderItem(0, getString(R.string.actors)), ArrayObjectAdapter(TvCastPresenter()))
            val fifthItem = ListRow(HeaderItem(1, "მსგავსი"), ArrayObjectAdapter(TvSimilarPresenter()))
            val initListRows = mutableListOf(fourthItem, fifthItem)
            rowsAdapter.addAll(0, initListRows)
        }
    }

    private fun setSeasonsAndEpisodes(titleId: Int, isTvShow: Boolean) {
        if (isTvShow) {
            viewModel.getSingleTitleData(titleId)

            viewModel.numOfSeasons.observe(viewLifecycleOwner, {
                val seasonCount = Array(it!!) { i -> (i * 1) + 1 }.toList()
                seasonsRowsAdapter(seasonCount)
            })

            episodesRowsAdapter(null)
        }
    }

    private fun seasonsRowsAdapter(seasonList: List<Int>) {
        val listRowAdapter = ArrayObjectAdapter(TvSeasonsPresenter()).apply {
            seasonList.forEach {
                add(it)
            }
        }
        HeaderItem(0, "სეზონები"). also {
            rowsAdapter.replace(0, ListRow(it, listRowAdapter))
        }

        viewModel.continueWatchingDetails.observe(viewLifecycleOwner, {
            if (it != null) {
                setPosition(0, it.season-1)
                focusedSeason = it.season
                episodesRowsAdapter(it.episode, it.season)
            }
        })
    }

    private fun episodesRowsAdapter(currentEpisode: Int?, currentSeason: Int? = null) {
        var isFirst = true

//        viewModel.episodeNames.observe(viewLifecycleOwner, { episodeList ->
//            val listRowAdapter = ArrayObjectAdapter(TvEpisodesPresenter(requireContext(), currentEpisode, currentSeason == focusedSeason)).apply {
//                episodeList.forEach {
//                    add(it)
//                }
//            }
//
//            HeaderItem(1, "ეპიზოდები"). also {
//                rowsAdapter.replace(1, ListRow(it, listRowAdapter))
//            }
//
//            if (currentEpisode != null && isFirst) {
//                setPosition(1, currentEpisode-1)
//                isFirst = false
//            }
//        })
    }

    private fun castRowsAdapter(castResponseListGetSingle: List<GetSingleTitleCastResponse.Data>, isTvShow: Boolean) {
        val listRowAdapter = ArrayObjectAdapter(TvCastPresenter()).apply {
            castResponseListGetSingle.forEach {
                add(it)
            }
        }
        HeaderItem(if (isTvShow) 2 else 0, "მსახიობები"). also {
            rowsAdapter.replace(if (isTvShow) 2 else 0, ListRow(it, listRowAdapter))
        }
    }

    private fun relatedRowsAdapter(relatedList: List<SingleTitleModel>, isTvShow: Boolean) {
        val listRowAdapter = ArrayObjectAdapter(TvSimilarPresenter()).apply {
            addAll(0, relatedList)
        }
        HeaderItem(if (isTvShow) 3 else 1, "მსგავსი"). also {
            rowsAdapter.replace(if (isTvShow) 3 else 1, ListRow(it, listRowAdapter))
        }
    }

    private fun setTitleCast(titleId: Int, isTvShow: Boolean) {
        viewModel.getSingleTitleCast(titleId)

        viewModel.castResponseDataGetSingle.observe(viewLifecycleOwner, {
            castRowsAdapter(it, isTvShow)
        })
    }

    private fun setTitleRelated(titleId: Int, isTvShow: Boolean) {
        viewModel.getSingleTitleRelated(titleId)

        viewModel.singleTitleRelated.observe(viewLifecycleOwner, {
            relatedRowsAdapter(it, isTvShow)
        })
    }

    private fun setPosition(row: Int, item: Int) {
        val isSmoothScroll = false
        val task = ListRowPresenter.SelectItemViewHolderTask(item)
        task.isSmoothScroll = isSmoothScroll

        rowsSupportFragment.setSelectedPosition(row, isSmoothScroll, task)
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder,
                item: Any,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row
        ) {
            when (item) {
                is TitleEpisodes -> {
                    viewModel.getEpisodeLanguages(item.titleId, item.episodeNum)
                    languagePickerDialog(item.episodeNum)
                }
                is SingleTitleModel -> {
                    val intent = Intent(context, TvSingleTitleActivity::class.java).apply {
                        putExtra(AppConstants.TITLE_ID, item.id)
                        putExtra(AppConstants.IS_TV_SHOW, item.isTvShow)
                    }
                    requireActivity().startActivity(intent)
                    if (requireActivity() is TvVideoPlayerActivity) {
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private inner class ItemViewSelectedListener(val titleId: Int) : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder?, item: Any?, rowViewHolder: RowPresenter.ViewHolder?, row: Row?) {
            if (item is Int) {
                viewModel.getSeasonFiles(titleId, item)
                focusedSeason = item
            }
        }
    }

    private fun languagePickerDialog(episode: Int) {
        val binding = DialogChooseLanguageBinding.inflate(LayoutInflater.from(requireContext()))
        val chooseLanguageDialog = Dialog(requireContext())
        chooseLanguageDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        chooseLanguageDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        chooseLanguageDialog.setContentView(binding.root)
        chooseLanguageDialog.show()

        val chooseLanguageLayout = GridLayoutManager(requireActivity(), 1, GridLayoutManager.HORIZONTAL, false)
        chooseLanguageAdapter = ChooseLanguageAdapter(requireContext()) {
            chooseLanguageDialog.dismiss()
            playEpisode(titleId!!, episode, it)
        }
        binding.rvChooseLanguage.layoutManager = chooseLanguageLayout
        binding.rvChooseLanguage.adapter = chooseLanguageAdapter

        viewModel.availableLanguages.observe(viewLifecycleOwner, {
            val languages = it.reversed()
            chooseLanguageAdapter.setLanguageList(languages)
            binding.rvChooseLanguage.requestFocus()
        })
    }

    private fun playEpisode(titleId: Int, episode: Int, chosenLanguage: String) {
        val intent = Intent(context, TvVideoPlayerActivity::class.java).apply {
            putExtra(
                AppConstants.VIDEO_PLAYER_DATA, VideoPlayerData(
                titleId,
                true,
                viewModel.chosenSeason.value!!,
                chosenLanguage,
                episode,
                0L,
                null
            ))
        }
        requireActivity().startActivity(intent)
        if (requireActivity() is TvVideoPlayerActivity) {
            (requireActivity() as TvVideoPlayerActivity).setCurrentFragmentState(TvVideoPlayerActivity.NEW_EPISODE)
            requireActivity().finish()
        }
    }

    private fun workaroundFocus() {
        if (view != null) {
            val viewToFocus = requireActivity().findViewById<View>(R.id.go_top_text)
            val browseFrameLayout: BrowseFrameLayout = requireView().findViewById(androidx.leanback.R.id.browse_frame)
            browseFrameLayout.onFocusSearchListener = OnFocusSearchListener setOnFocusSearchListener@{ _: View?, direction: Int ->
                if (direction == View.FOCUS_UP) {
                    this.hasFocus = true
                    if (requireActivity() is TvWatchlistActivity) {
                        (requireActivity() as TvWatchlistActivity).buttonFocusability(true)
                    }
                    return@setOnFocusSearchListener viewToFocus
                } else {
                    return@setOnFocusSearchListener null
                }
            }
        }
    }
}