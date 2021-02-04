package com.lukakordzaia.imoviesapp.ui.phone.singletitle

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lukakordzaia.imoviesapp.database.ImoviesDatabase
import com.lukakordzaia.imoviesapp.database.DbDetails
import com.lukakordzaia.imoviesapp.datamodels.TitleData
import com.lukakordzaia.imoviesapp.datamodels.TitleDetails
import com.lukakordzaia.imoviesapp.datamodels.TitleEpisodes
import com.lukakordzaia.imoviesapp.datamodels.TitleFiles
import com.lukakordzaia.imoviesapp.network.Result
import com.lukakordzaia.imoviesapp.repository.SingleTitleRepository
import com.lukakordzaia.imoviesapp.ui.baseclasses.BaseViewModel
import com.lukakordzaia.imoviesapp.ui.phone.singletitle.choosetitledetails.ChooseTitleDetailsFragmentDirections
import kotlinx.coroutines.launch

class SingleTitleViewModel(private val repository: SingleTitleRepository) : BaseViewModel() {
    private val _singleTitleData = MutableLiveData<TitleData.Data>()
    val singleTitleData: LiveData<TitleData.Data> = _singleTitleData

    private val _titleDetails = MutableLiveData<TitleDetails>()
    val titleDetails: LiveData<TitleDetails> = _titleDetails

    private val _titleIsInDb = MutableLiveData<Boolean>()
    val titleIsInDb: LiveData<Boolean> = _titleIsInDb

    private val _singleMovieFiles = MutableLiveData<TitleFiles>()

    private val _movieNotYetAdded = MutableLiveData<Boolean>()
    val movieNotYetAdded: LiveData<Boolean> = _movieNotYetAdded

    private val _availableLanguages = MutableLiveData<MutableList<String>>()
    val availableLanguages: LiveData<MutableList<String>> = _availableLanguages

    private val _chosenLanguage = MutableLiveData<String>()
    val chosenLanguage: LiveData<String> = _chosenLanguage

    private val _chosenSeason = MutableLiveData<Int>(0)
    val chosenSeason: LiveData<Int> = _chosenSeason

    private val _chosenEpisode = MutableLiveData<Int>(0)
    val chosenEpisode: LiveData<Int> = _chosenEpisode

    private val _episodeNames = MutableLiveData<List<TitleEpisodes>>()
    val episodeNames: LiveData<List<TitleEpisodes>> = _episodeNames

    fun onPlayPressed(titleId: Int, titleDetails: TitleDetails) {
        navigateToNewFragment(SingleTitleFragmentDirections.actionSingleTitleFragmentToChooseTitleDetailsFragment(
                titleId,
                titleDetails.numOfSeasons,
                titleDetails.isTvShow
        ))
    }

    fun onTrailerPressed(titleId: Int, isTvShow: Boolean, trailerURl: String?) {
        navigateToNewFragment(SingleTitleFragmentDirections.actionSingleTitleFragmentToVideoPlayerFragmentNav(
                0,
                0,
                titleId,
                isTvShow,
                "ENG",
                trailerUrl = trailerURl
        ))
    }

    fun getSingleTitleData(titleId: Int) {
        viewModelScope.launch {
            when (val data = repository.getSingleTitleData(titleId)) {
                is Result.Success -> {
                    _singleTitleData.value = data.data.data
                    checkTvShowAndFiles()
                    setLoading(false)
                }
                is Result.Error -> {
                    Log.d("errorsinglemovies", data.exception)
                }
            }
        }
    }

    fun checkTitleInDb(context: Context, titleId: Int): LiveData<Boolean> {
        val database = ImoviesDatabase.getDatabase(context)?.getDao()
        return repository.checkTitleInDb(database!!, titleId)
    }

    fun titleIsInDb(exists: Boolean) {
        _titleIsInDb.value = exists
    }

    fun getSingleWatchedTitleDetails(context: Context, titleId: Int): LiveData<DbDetails> {
        val database = ImoviesDatabase.getDatabase(context)?.getDao()
        return repository.getSingleWatchedTitles(database!!, titleId)
    }

    fun deleteTitleFromDb(context: Context, titleId: Int) {
        val database = ImoviesDatabase.getDatabase(context)?.getDao()
        viewModelScope.launch {
            repository.deleteTitleFromDb(database!!, titleId)
        }
    }

    private fun checkTvShowAndFiles() {
        _titleDetails.value = TitleDetails(singleTitleData.value!!.seasons.data.last().number, singleTitleData.value!!.isTvShow!!)
    }

    fun onPlayButtonPressed(titleId: Int, isTvShow: Boolean) {
        navigateToNewFragment(
            ChooseTitleDetailsFragmentDirections.actionChooseTitleDetailsFragmentToVideoPlayerFragmentNav(
                chosenSeason.value!!,
                chosenEpisode.value!!,
                titleId,
                isTvShow,
                chosenLanguage.value!!,
                trailerUrl = null
            ),
        )
    }


    fun onContinueWatchingPressed(dbDetails: DbDetails,) {
        navigateToNewFragment(
            ChooseTitleDetailsFragmentDirections.actionChooseTitleDetailsFragmentToVideoPlayerFragmentNav(
            titleId = dbDetails.titleId,
            chosenSeason = dbDetails.season,
            chosenEpisode = dbDetails.episode,
            isTvShow = dbDetails.isTvShow,
            watchedTime = dbDetails.watchedTime,
            chosenLanguage = dbDetails.language,
            trailerUrl = null
        ))
    }

    fun getSingleTitleFiles(movieId: Int) {
        viewModelScope.launch {
            when (val files = repository.getSingleTitleFiles(movieId)) {
                is Result.Success -> {
                    val data = files.data.data
                    if (data.isNotEmpty()) {
                        val languages: MutableList<String> = ArrayList()
                        _singleMovieFiles.value = files.data
                        data[0].files!!.forEach {
                            it.lang?.let { it1 -> languages.add(it1) }
                        }
                        _availableLanguages.value = languages

                        _movieNotYetAdded.value = false
                    } else {
                        _movieNotYetAdded.value = true
                    }
                    setLoading(false)
                }
                is Result.Error -> {
                    Log.d("errorfiles", files.exception)
                }
            }
        }
    }

    fun getTitleLanguageFiles(language: String) {
        _chosenLanguage.value = language
    }

    fun getSeasonFiles(movieId: Int, season: Int) {
        _chosenSeason.value = season
        viewModelScope.launch {
            when (val files = repository.getSingleTitleFiles(movieId, season)) {
                is Result.Success -> {
                    val data = files.data.data

                    val getEpisodeNames: MutableList<TitleEpisodes> = ArrayList()
                    data.forEach {
                        getEpisodeNames.add(TitleEpisodes(it.episode, it.title, it.poster))
                    }
                    _episodeNames.value = getEpisodeNames
                    Log.d("episodenames", "${episodeNames.value}")

                    setLoading(false)
                }
                is Result.Error -> {
                    Log.d("errorseasons", files.exception)
                }
            }
        }
    }

    fun getEpisodeFile(episodeNum: Int) {
        _chosenEpisode.value = episodeNum
    }
}