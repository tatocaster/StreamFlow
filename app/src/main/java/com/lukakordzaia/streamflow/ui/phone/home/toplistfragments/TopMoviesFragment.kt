package com.lukakordzaia.streamflow.ui.phone.home.toplistfragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lukakordzaia.streamflow.databinding.FragmentPhoneSingleCategoryBinding
import com.lukakordzaia.streamflow.network.LoadingState
import com.lukakordzaia.streamflow.ui.baseclasses.BaseFragment
import com.lukakordzaia.streamflow.ui.phone.categories.singlegenre.SingleCategoryAdapter
import com.lukakordzaia.streamflow.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TopMoviesFragment : BaseFragment<FragmentPhoneSingleCategoryBinding>() {
    private val viewModel: SingleTopListViewModel by viewModel()
    private lateinit var singleCategoryAdapter: SingleCategoryAdapter
    private var page = 1
    private var pastVisibleItems: Int = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var loading = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPhoneSingleCategoryBinding
        get() = FragmentPhoneSingleCategoryBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!hasInitializedRootView) {
            hasInitializedRootView = true
            viewModel.getTopMovies(page)
        }

        topBarListener("ტოპ ფილმები")

        fragmentObservers()
        topMoviesContainer()
    }

    private fun fragmentObservers() {
        viewModel.noInternet.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                requireContext().createToast(AppConstants.NO_INTERNET)
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.getTopMovies(page)
                }, 3000)
            }
        })

        viewModel.navigateScreen.observe(viewLifecycleOwner, EventObserver {
            navController(it)
        })

        viewModel.toastMessage.observe(viewLifecycleOwner, EventObserver {
            requireContext().createToast(it)
        })
    }

    private fun topMoviesContainer() {
        viewModel.topMovieLoader.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.RUNNING -> binding.progressBar.setVisible()
                LoadingState.Status.SUCCESS -> binding.progressBar.setGone()
            }
        })

        val layoutManager = GridLayoutManager(requireActivity(), 2, GridLayoutManager.VERTICAL, false)
        singleCategoryAdapter = SingleCategoryAdapter(requireContext()) {
            viewModel.onSingleTitlePressed(AppConstants.NAV_TOP_MOVIES_TO_SINGLE, it)
        }
        binding.rvSingleCategory.adapter = singleCategoryAdapter
        binding.rvSingleCategory.layoutManager = layoutManager

        viewModel.topMovieList.observe(viewLifecycleOwner, {
            singleCategoryAdapter.setCategoryTitleList(it)
        })

        binding.rvSingleCategory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.childCount
                    totalItemCount = layoutManager.itemCount
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    if (!loading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        loading = true
                        fetchMoreTopMovies()
                    }
                }
            }
        })
    }

    private fun fetchMoreTopMovies() {
        binding.progressBar.setVisible()
        page++
        Log.d("currentpage", page.toString())
        viewModel.getTopMovies(page)
    }
}