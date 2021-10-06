package com.lukakordzaia.streamflow.ui.phone.phonewatchlist

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lukakordzaia.streamflow.R
import com.lukakordzaia.streamflow.databinding.DialogRemoveFavoriteBinding
import com.lukakordzaia.streamflow.databinding.FragmentPhoneWatchlistBinding
import com.lukakordzaia.streamflow.network.LoadingState
import com.lukakordzaia.streamflow.ui.baseclasses.BaseFragment
import com.lukakordzaia.streamflow.ui.shared.WatchlistViewModel
import com.lukakordzaia.streamflow.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhoneWatchlistFragment : BaseFragment<FragmentPhoneWatchlistBinding>() {
    private val watchlistViewModel: WatchlistViewModel by viewModel()
    private lateinit var watchlistMoviesAdapter: WatchlistAdapter
    private var type = AppConstants.WATCHLIST_MOVIES

    private var page = 1
    private var pastVisibleItems: Int = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var loading = false
    private var hasMore = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPhoneWatchlistBinding
        get() = FragmentPhoneWatchlistBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authCheck()
        fragmentListeners()
        fragmentObservers()
        favMoviesContainer()
        setButtons(AppConstants.WATCHLIST_MOVIES)
    }

    private fun authCheck() {
        if (sharedPreferences.getLoginToken() != "") {
            watchlistViewModel.getUserWatchlist(page, AppConstants.WATCHLIST_MOVIES)
            binding.favoriteMoviesContainer.setVisible()
            binding.favoriteNoAuth.setGone()
        } else {
            binding.favoriteMoviesContainer.setGone()
            binding.favoriteNoAuth.setVisible()
        }
    }

    private fun fragmentListeners() {
        binding.toolbar.homeProfile.setOnClickListener {
            watchlistViewModel.onProfileButtonPressed()
        }

        binding.profileButton.setOnClickListener {
            watchlistViewModel.onProfileButtonPressed()
        }

        binding.watchlistMovies.setOnClickListener {
            watchlistViewModel.clearWatchlist()

            page = 1
            watchlistViewModel.getUserWatchlist(page, AppConstants.WATCHLIST_MOVIES)
            setButtons(AppConstants.WATCHLIST_MOVIES)

            type = AppConstants.WATCHLIST_MOVIES
        }

        binding.watchlistTvShows.setOnClickListener {
            watchlistViewModel.clearWatchlist()

            page = 1
            watchlistViewModel.getUserWatchlist(page, AppConstants.WATCHLIST_TV_SHOWS)
            setButtons(AppConstants.WATCHLIST_TV_SHOWS)

            type = AppConstants.WATCHLIST_TV_SHOWS
        }
    }

    private fun fragmentObservers() {
        watchlistViewModel.userWatchlist.observe(viewLifecycleOwner, { watchlist ->
            watchlistMoviesAdapter.setItems(watchlist)
        })

        watchlistViewModel.toastMessage.observe(viewLifecycleOwner, EventObserver {
            requireContext().createToast(it)
        })

        watchlistViewModel.navigateScreen.observe(viewLifecycleOwner, EventObserver {
            navController(it)
        })

        watchlistViewModel.removedTitle.observe(viewLifecycleOwner, EventObserver {
            watchlistMoviesAdapter.notifyItemRemoved(it)
        })

        watchlistViewModel.hasMorePage.observe(viewLifecycleOwner, {
            hasMore = it
        })
    }

    private fun favMoviesContainer() {
        watchlistViewModel.watchListLoader.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.RUNNING -> {
                    binding.favoriteMoviesProgressBar.setVisible()
                }
                LoadingState.Status.SUCCESS -> {
                    binding.favoriteMoviesProgressBar.setGone()
                    binding.rvFavoritesMovies.setVisible()
                    loading = false
                }
            }
        })

        watchlistViewModel.noFavorites.observe(viewLifecycleOwner, {
            binding.favoriteNoMovies.setVisibleOrGone(it)
        })

        val moviesLayout = GridLayoutManager(requireActivity(), 2, GridLayoutManager.VERTICAL, false)
        watchlistMoviesAdapter = WatchlistAdapter(requireContext(),
            {
                watchlistViewModel.onSingleTitlePressed(it)
            },
            { titleId: Int, position: Int ->
                removeTitleDialog(titleId, position)
            }
        )
        binding.rvFavoritesMovies.layoutManager = moviesLayout
        binding.rvFavoritesMovies.adapter = watchlistMoviesAdapter

        binding.rvFavoritesMovies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = moviesLayout.childCount
                    totalItemCount = moviesLayout.itemCount
                    pastVisibleItems = moviesLayout.findFirstVisibleItemPosition()

                    if (!loading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = true
                        fetchMoreTitle()
                    }
                }
            }
        })
    }

    private fun removeTitleDialog(titleId: Int, position: Int) {
        val binding = DialogRemoveFavoriteBinding.inflate(LayoutInflater.from(requireContext()))
        val removeFavorite = Dialog(requireContext())
        removeFavorite.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        removeFavorite.setContentView(binding.root)

        binding.confirmButton.setOnClickListener {
            watchlistViewModel.deleteWatchlistTitle(titleId, position)
            removeFavorite.dismiss()
        }
        binding.cancelButton.setOnClickListener {
            removeFavorite.dismiss()
        }
        removeFavorite.show()
    }

    private fun fetchMoreTitle() {
        if (hasMore) {
            binding.favoriteMoviesProgressBar.setVisible()
            page++
            watchlistViewModel.getUserWatchlist(page, type)
        }
    }

    private fun setButtons(type: String) {
        when (type) {
            AppConstants.WATCHLIST_MOVIES -> {
                binding.watchlistMovies.setColor(R.color.accent_color)
                binding.watchlistTvShows.setColor(R.color.secondary_color)
            }
            AppConstants.WATCHLIST_TV_SHOWS -> {
                binding.watchlistMovies.setColor(R.color.secondary_color)
                binding.watchlistTvShows.setColor(R.color.accent_color)
            }
        }
    }
}