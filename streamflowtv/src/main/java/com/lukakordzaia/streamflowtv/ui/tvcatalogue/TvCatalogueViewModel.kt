package com.lukakordzaia.streamflowtv.ui.tvcatalogue

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lukakordzaia.core.utils.AppConstants
import com.lukakordzaia.core.baseclasses.BaseViewModel
import com.lukakordzaia.core.datamodels.SingleTitleModel
import com.lukakordzaia.core.network.LoadingState
import com.lukakordzaia.core.network.Result
import com.lukakordzaia.core.network.toTitleListModel
import kotlinx.coroutines.launch

class TvCatalogueViewModel : BaseViewModel() {
    private val _tvCatalogueList = MutableLiveData<List<SingleTitleModel>>()
    val tvCatalogueList: LiveData<List<SingleTitleModel>> = _tvCatalogueList

    fun getNewMoviesTv(page: Int) {
        setGeneralLoader(LoadingState.LOADING)
        viewModelScope.launch {
            when (val movies = environment.homeRepository.getNewMovies(page)) {
                is Result.Success -> {
                    val data = movies.data.data
                    _tvCatalogueList.value = data.toTitleListModel()
                    setGeneralLoader(LoadingState.LOADED)
                }
                is Result.Error -> {
                    newToastMessage(movies.exception)
                }
                is Result.Internet -> {
                    setNoInternet()
                }
            }
        }
    }

    fun getTopMoviesTv(page: Int) {
        setGeneralLoader(LoadingState.LOADING)
        viewModelScope.launch {
            when (val topMovies = environment.homeRepository.getTopMovies(page)) {
                is Result.Success -> {
                    val data = topMovies.data.data
                    _tvCatalogueList.value = data.toTitleListModel()
                    setGeneralLoader(LoadingState.LOADED)
                }
                is Result.Error -> {
                    newToastMessage(topMovies.exception)
                }
                is Result.Internet -> {
                    setNoInternet()
                }
            }
        }
    }

    fun getTopTvShowsTv(page: Int) {
        setGeneralLoader(LoadingState.LOADING)
        viewModelScope.launch {
            when (val tvShows = environment.homeRepository.getTopTvShows(page)) {
                is Result.Success -> {
                    val data = tvShows.data.data
                    _tvCatalogueList.value = data.toTitleListModel()
                    setGeneralLoader(LoadingState.LOADED)
                }
                is Result.Error -> {
                    newToastMessage(tvShows.exception)
                }
                is Result.Internet -> {
                    setNoInternet()
                }
            }
        }
    }

    fun fetchContent(type: Int, page: Int) {
        when (type) {
            AppConstants.LIST_NEW_MOVIES -> {
                getNewMoviesTv(page)
            }
            AppConstants.LIST_TOP_MOVIES -> {
                getTopMoviesTv(page)
            }
            AppConstants.LIST_TOP_TV_SHOWS -> {
                getTopTvShowsTv(page)
            }
        }
    }
}