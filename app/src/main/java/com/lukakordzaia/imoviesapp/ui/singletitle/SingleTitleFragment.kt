package com.lukakordzaia.imoviesapp.ui.singletitle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.lukakordzaia.imoviesapp.R
import com.lukakordzaia.imoviesapp.network.models.TitleDetails
import com.lukakordzaia.imoviesapp.utils.EventObserver
import com.lukakordzaia.imoviesapp.utils.navController
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_single_title.*

class SingleTitleFragment : Fragment(R.layout.fragment_single_title) {
    private lateinit var viewModel: SingleTitleViewModel
    private val args: SingleTitleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SingleTitleViewModel::class.java)
        viewModel.getSingleMovieFiles(args.titleId)

        viewModel.singleTitleFiles.observe(viewLifecycleOwner, Observer {
            tv_single_title_name_geo.text = it.primaryName
            tv_single_title_name_eng.text = it.secondaryName
            if (it.rating?.imdb?.score != null) {
                tv_single_movie_imdb_score.text = it.rating.imdb.score.toString()
            }
            if (!it.covers?.data?.x1050.isNullOrEmpty()) {
                Picasso.get().load(it.covers?.data?.x1050).into(iv_single_title_play)
            }
            tv_single_title_desc.text = it.plot?.data?.description

            tv_single_title_year.text = it.year.toString()
            tv_single_title_duration.text = "${it.duration} წ."
            if (!it.countries.data.isNullOrEmpty()) {
                tv_single_title_country.text = it.countries.data[0].secondaryName
            }

        })

        viewModel.titleDetails.observe(viewLifecycleOwner, Observer {
            iv_post_video_icon.setOnClickListener { _ ->
                viewModel.onPlayPressed(args.titleId, TitleDetails(it.numOfSeasons, it.isTvShow))
            }
        })

        viewModel.navigateScreen.observe(viewLifecycleOwner, EventObserver {
            navController(it)
        })

    }
}