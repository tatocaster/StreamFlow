package com.lukakordzaia.streamflow.ui.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lukakordzaia.streamflow.database.continuewatchingdb.ContinueWatchingRoom
import com.lukakordzaia.streamflow.datamodels.TitleMediaItemsUri
import com.lukakordzaia.streamflow.datamodels.VideoPlayerData
import com.lukakordzaia.streamflow.network.LoadingState
import com.lukakordzaia.streamflow.network.Result
import com.lukakordzaia.streamflow.network.models.imovies.request.user.PostTitleWatchTimeRequestBody
import com.lukakordzaia.streamflow.network.models.imovies.response.singletitle.GetSingleTitleFilesResponse
import com.lukakordzaia.streamflow.ui.baseclasses.BaseViewModel
import com.lukakordzaia.streamflow.utils.Event
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class VideoPlayerViewModel : BaseViewModel() {
    private val _numOfSeasons = MutableLiveData<Int>()
    val numOfSeasons: LiveData<Int> = _numOfSeasons

    private val _availableLanguages = MutableLiveData<List<String>>()
    val availableLanguages: LiveData<List<String>> = _availableLanguages

    private val _availableSubtitles = MutableLiveData<List<String>>()
    val availableSubtitles: LiveData<List<String>> = _availableSubtitles

    private var episodeLink: String? = null
    private var subtitleLink: String? = null
    val mediaAndSubtitle = MutableLiveData<TitleMediaItemsUri>()

    private val _videoPlayerData = MutableLiveData<VideoPlayerData>()
    val videoPlayerData: LiveData<VideoPlayerData> = _videoPlayerData

    private val qualityForDb = MutableLiveData("HIGH")

    private val _setTitleName = MutableLiveData<String>()
    val setTitleName: LiveData<String> = _setTitleName

    var totalEpisodesInSeason = MutableLiveData<Int>()

    val saveLoader = MutableLiveData<LoadingState>()

    private val _newEpisodeStarted = MutableLiveData<Event<Boolean>>()
    val newEpisodeStarted: LiveData<Event<Boolean>> = _newEpisodeStarted

    fun setVideoPlayerData(videoPlayerData: VideoPlayerData) {
        _videoPlayerData.value = videoPlayerData

        if (videoPlayerData.trailerUrl == null) {
            getTitleFiles(videoPlayerData)
            getSingleTitleData(videoPlayerData.titleId)
        }
    }

    fun addContinueWatching(playBackDuration: Long, titleDuration: Long) {
        val dbDetails = ContinueWatchingRoom(
            videoPlayerData.value!!.titleId,
            videoPlayerData.value!!.chosenLanguage,
            TimeUnit.MILLISECONDS.toSeconds(playBackDuration),
            TimeUnit.MILLISECONDS.toSeconds(titleDuration),
            videoPlayerData.value!!.isTvShow,
            videoPlayerData.value!!.chosenSeason,
            videoPlayerData.value!!.chosenEpisode
        )
        if (playBackDuration > 0 && titleDuration > 0) {
            if (sharedPreferences.getLoginToken() != "") {
                saveLoader.value = LoadingState.LOADING
                viewModelScope.launch {
                    when (environment.userRepository.titleWatchTime(
                        PostTitleWatchTimeRequestBody(
                            duration = TimeUnit.MILLISECONDS.toSeconds(titleDuration).toInt(),
                            progress = TimeUnit.MILLISECONDS.toSeconds(playBackDuration).toInt(),
                            language = videoPlayerData.value!!.chosenLanguage,
                            quality = qualityForDb.value!!
                        ),
                        videoPlayerData.value!!.titleId,
                        videoPlayerData.value!!.chosenSeason,
                        videoPlayerData.value!!.chosenEpisode
                    )) {
                        is Result.Success -> {
                            saveLoader.value = LoadingState.LOADED
                        }
                        is Result.Error -> {
                            saveLoader.value = LoadingState.ERROR
                        }
                    }
                }
            } else {
                viewModelScope.launch {
                    environment.databaseRepository.insertContinueWatchingInRoom(dbDetails)
                }
            }
        } else {
            saveLoader.value = LoadingState.ERROR
        }
    }

    fun getTitleFiles(videoPlayerData: VideoPlayerData) {
        setNoInternet(false)

        viewModelScope.launch {
            when (val files = environment.singleTitleRepository.getSingleTitleFiles(videoPlayerData.titleId, videoPlayerData.chosenSeason)) {
                is Result.Success -> {
                    totalEpisodesInSeason.value = files.data.data.size

                    val season = if (videoPlayerData.chosenSeason == 0) {
                        files.data.data[0]
                    } else {
                        files.data.data[videoPlayerData.chosenEpisode - 1]
                    }

                    _setTitleName.value = season.title

                    checkAvailability(season.files, videoPlayerData.chosenLanguage, videoPlayerData.chosenSubtitle)

                    val mediaItems = TitleMediaItemsUri(episodeLink, subtitleLink)
                    mediaAndSubtitle.value = mediaItems
                }
                is Result.Error -> {
                    Log.d("errornextepisode", files.exception)
                }
                is Result.Internet -> {
                    setNoInternet()
                }
            }
        }
    }

    private fun checkAvailability(episodeFiles: List<GetSingleTitleFilesResponse.Data.File>, chosenLanguage: String, chosenSubtitle: String?) {
        val languages: MutableList<String> = ArrayList()

        episodeFiles.forEach { file ->
            languages.add(file.lang)

            if (file.lang == chosenLanguage) {
                checkAudio(file, chosenSubtitle)
            }
        }
        _availableLanguages.value = languages

        // If new episode is not available in same language, automatically switches to first available language
        if (episodeLink.isNullOrEmpty()) {
            newToastMessage("$chosenLanguage - ვერ მოიძებნა. ავტომატურად ჩაირთო - ${episodeFiles[0].lang}")
            checkAudio(episodeFiles[0], chosenSubtitle)
        }
    }

    private fun checkAudio(singleEpisodeFiles: GetSingleTitleFilesResponse.Data.File, chosenSubtitle: String?) {
        episodeLink = null

        if (singleEpisodeFiles.files.size == 1) {
            episodeLink = singleEpisodeFiles.files[0].src
        } else if (singleEpisodeFiles.files.size > 1) {
            singleEpisodeFiles.files.forEach {
                if (it.quality == "HIGH") {
                    episodeLink = it.src
                }
            }
        }

        checkSubtitles(singleEpisodeFiles.subtitles, singleEpisodeFiles.lang, chosenSubtitle)
    }

    private fun checkSubtitles(fileSubtitles: List<GetSingleTitleFilesResponse.Data.File.Subtitle?>?, chosenLanguage: String, chosenSubtitle: String?) {
        subtitleLink = null

        val subtitlesList: MutableList<String> = ArrayList()
        subtitlesList.add(0, "გათიშვა")


        if (!fileSubtitles.isNullOrEmpty()) {
            fileSubtitles.forEach {
                subtitlesList.add(it!!.lang)
            }

            if (chosenSubtitle == null || chosenSubtitle == "გათიშვა") {
                when (fileSubtitles.size) {
                    1 -> {
                        subtitleLink = fileSubtitles[0]!!.url
                        _videoPlayerData.value = _videoPlayerData.value?.copy(chosenSubtitle = fileSubtitles[0]!!.lang)
                    }
                    else -> {
                        fileSubtitles.forEach {
                            if (it!!.lang.equals(chosenLanguage, true)) {
                                subtitleLink = it.url
                                _videoPlayerData.value = _videoPlayerData.value?.copy(chosenSubtitle = it.lang)
                            }
                        }
                    }
                }
            } else {
                fileSubtitles.forEach {
                    if (it!!.lang.equals(chosenSubtitle, true)) {
                        subtitleLink = it.url
                    }
                }

                // If chosen subtitle language is not available for next episode, automatically switches to first available subtitle
                if (subtitleLink == null) {
                    subtitleLink = fileSubtitles[0]!!.url
                    _videoPlayerData.value = _videoPlayerData.value?.copy(chosenSubtitle = fileSubtitles[0]!!.lang)
                    newToastMessage("$chosenSubtitle - სუბტიტრები ვერ მოიძებნა. ავტომატურად ჩაირთო - ${fileSubtitles[0]!!.lang} სუბტიტრები")
                }
            }
        } else {
            subtitleLink = null
            _videoPlayerData.value = _videoPlayerData.value?.copy(chosenSubtitle = "გათიშვა")
        }

        _availableSubtitles.value = subtitlesList
    }

    fun getSingleTitleData(titleId: Int) {
        viewModelScope.launch {
            when (val titleData = environment.singleTitleRepository.getSingleTitleData(titleId)) {
                is Result.Success -> {
                    val data = titleData.data.data

                    if (!data.isTvShow || data.seasons == null) {
                        _numOfSeasons.value = 0
                    } else {
                        _numOfSeasons.value = data.seasons.data.size
                    }
                }
                is Result.Error -> {
                    Log.d("errorsinglemovies", titleData.exception)
                }
            }
        }
    }

    fun episodeHasStarted() {
        _newEpisodeStarted.value = Event(true)
    }
}