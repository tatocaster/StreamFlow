package com.lukakordzaia.streamflow.ui.phone.home

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.lukakordzaia.streamflow.R
import com.lukakordzaia.streamflow.network.LoadingState
import com.lukakordzaia.streamflow.ui.baseclasses.BaseFragment
import com.lukakordzaia.streamflow.ui.phone.home.homeadapters.HomeDbTitlesAdapter
import com.lukakordzaia.streamflow.ui.phone.home.homeadapters.HomeNewMovieAdapter
import com.lukakordzaia.streamflow.ui.phone.home.homeadapters.HomeTopMovieAdapter
import com.lukakordzaia.streamflow.ui.phone.home.homeadapters.HomeTvShowAdapter
import com.lukakordzaia.streamflow.utils.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.phone_home_framgent.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : BaseFragment(R.layout.phone_home_framgent) {
    private val viewModel by viewModel<HomeViewModel>()

    private lateinit var homeDbTitlesAdapter: HomeDbTitlesAdapter
    private lateinit var homeNewMovieAdapter: HomeNewMovieAdapter
    private lateinit var homeTopMovieAdapter: HomeTopMovieAdapter
    private lateinit var homeTvShowAdapter: HomeTvShowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.noInternet.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                requireContext().createToast(AppConstants.NO_INTERNET)
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.refreshContent(1)
                }, 5000)
            }
        })

        //Movie Day
        viewModel.movieDayLoader.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.RUNNING -> movieDay_progressBar.setVisible()
                LoadingState.Status.SUCCESS -> {
                    movieDay_progressBar.setGone()
                    main_movie_day_container.setVisible()
                }
            }
        })

        viewModel.getMovieDay()

        viewModel.movieDayData.observe(viewLifecycleOwner, { it ->
            main_movie_day_container.setOnClickListener { _ ->
                viewModel.onSingleTitlePressed(AppConstants.NAV_HOME_TO_SINGLE, it.id)
            }

            Picasso.get().load(it.covers?.data?.x1050).placeholder(R.drawable.movie_image_placeholder).error(R.drawable.movie_image_placeholder).into(movie_day_cover)

            if (it.primaryName.isNotEmpty()) {
                movie_day_name.text = it.primaryName
            } else {
                movie_day_name.text = it.secondaryName
            }
        })

        //Continue Watching Titles List
        val dbLayout = GridLayoutManager(requireActivity(), 1, GridLayoutManager.HORIZONTAL, false)
        homeDbTitlesAdapter = HomeDbTitlesAdapter(requireContext(),
                {
                    viewModel.onContinueWatchingPressed(it)
                },
                {
                    viewModel.onSingleTitlePressed(AppConstants.NAV_HOME_TO_SINGLE, it)
                },
                { titleId: Int, titleName: String ->
                    viewModel.onContinueWatchingInfoPressed(titleId, titleName)
//                    val popUp = PopupMenu(context, buttonView, Gravity.CENTER)
//                    popUp.menuInflater.inflate(R.menu.watched_menu, popUp.menu)
//
//                    popUp.setOnMenuItemClickListener {
//                        when (it.itemId) {
//                            R.id.delete_from_db -> {
//                                val clearDbDialog = Dialog(requireContext())
//                                clearDbDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                                clearDbDialog.setContentView(layoutInflater.inflate(R.layout.clear_db_alert_dialog, null))
//                                clearDbDialog.clear_db_alert_yes.setOnClickListener {
//                                    viewModel.deleteSingleContinueWatchingFromRoom(requireContext(), titleId)
//                                    viewModel.deleteSingleContinueWatchingFromFirestore(titleId)
//                                    clearDbDialog.dismiss()
//                                }
//                                clearDbDialog.clear_db_alert_no.setOnClickListener {
//                                    clearDbDialog.dismiss()
//                                }
//                                clearDbDialog.show()
//                                return@setOnMenuItemClickListener true
//                            }
//                            R.id.watched_check_info -> {
//                                viewModel.onSingleTitlePressed(AppConstants.NAV_HOME_TO_SINGLE, titleId)
//                                return@setOnMenuItemClickListener true
//                            }
//                            else -> {
//                                requireContext().createToast("nothing else")
//                                return@setOnMenuItemClickListener true
//                            }
//                        }
//                    }
//                    popUp.show()
                })
        rv_main_watched_titles.adapter = homeDbTitlesAdapter
        rv_main_watched_titles.layoutManager = dbLayout

        if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (auth.currentUser != null) {
                viewModel.clearContinueWatchingTitleList()
                viewModel.getContinueWatchingFromFirestorePHONE()
            } else {
                viewModel.getContinueWatchingFromRoom(requireContext()).observe(viewLifecycleOwner, {
                    viewModel.clearContinueWatchingTitleList()
                    if (!it.isNullOrEmpty()) {
                        main_watched_titles_none.setGone()

                        viewModel.getContinueWatchingTitlesFromApi(it)
                    } else {
                        main_watched_titles_none.setVisible()
                    }
                })
            }
        }

        viewModel.dbList.observe(viewLifecycleOwner, {
            if (it.isNullOrEmpty()) {
                main_watched_titles_none.setVisible()
                rv_main_watched_titles.setGone()
            } else {
                main_watched_titles_none.setGone()
                rv_main_watched_titles.setVisible()
                homeDbTitlesAdapter.setWatchedTitlesList(it)
            }
        })


        //New Movies List
        viewModel.newMovieLoader.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.RUNNING -> newMovie_progressBar.setVisible()
                LoadingState.Status.SUCCESS -> newMovie_progressBar.setGone()
            }
        })


        val newMovieLayout = GridLayoutManager(requireActivity(), 1, GridLayoutManager.HORIZONTAL, false)
        homeNewMovieAdapter = HomeNewMovieAdapter(requireContext()) {
            viewModel.onSingleTitlePressed(AppConstants.NAV_HOME_TO_SINGLE, it)
        }
        rv_main_new_movies.adapter = homeNewMovieAdapter
        rv_main_new_movies.layoutManager = newMovieLayout

        viewModel.getNewMovies(1)
        viewModel.newMovieList.observe(viewLifecycleOwner, {
            homeNewMovieAdapter.setMoviesList(it)
        })

        //Top Movies List
        viewModel.topMovieLoader.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.RUNNING -> topMovie_progressBar.setVisible()
                LoadingState.Status.SUCCESS -> topMovie_progressBar.setGone()
            }
        })

        val topMovieLayout = GridLayoutManager(requireActivity(), 1, GridLayoutManager.HORIZONTAL, false)
        homeTopMovieAdapter = HomeTopMovieAdapter(requireContext()) {
            viewModel.onSingleTitlePressed(AppConstants.NAV_HOME_TO_SINGLE, it)
        }
        rv_main_top_movies.adapter = homeTopMovieAdapter
        rv_main_top_movies.layoutManager = topMovieLayout

        viewModel.getTopMovies(1)

        viewModel.topMovieList.observe(viewLifecycleOwner, Observer {
            homeTopMovieAdapter.setMoviesList(it)
        })


        //Top TvShows List
        viewModel.topTvShowsLoader.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.RUNNING -> topTvShow_progressBar.setVisible()
                LoadingState.Status.SUCCESS -> topTvShow_progressBar.setGone()
            }
        })

        val tvShowLayout = GridLayoutManager(requireActivity(), 1, GridLayoutManager.HORIZONTAL, false)
        homeTvShowAdapter = HomeTvShowAdapter(requireContext()) {
            viewModel.onSingleTitlePressed(AppConstants.NAV_HOME_TO_SINGLE, it)
        }
        rv_main_top_tvshows.adapter = homeTvShowAdapter
        rv_main_top_tvshows.layoutManager = tvShowLayout

        viewModel.getTopTvShows(1)

        viewModel.topTvShowList.observe(viewLifecycleOwner, Observer {
            homeTvShowAdapter.setTvShowsList(it)
        })

        top_movies_more.setOnClickListener {
            viewModel.topMoviesMorePressed()
        }

        top_tvshows_more.setOnClickListener {
            viewModel.topTvShowsMorePressed()
        }

        viewModel.navigateScreen.observe(viewLifecycleOwner, EventObserver {
            navController(it)
        })

        viewModel.toastMessage.observe(viewLifecycleOwner, EventObserver {
            requireContext().createToast(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_button -> {
                navController(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
            }
            R.id.favorite_button -> {
                navController(HomeFragmentDirections.actionHomeFragmentToFavoritesFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }
}