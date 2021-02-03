package com.lukakordzaia.imoviesapp.ui.phone.genres

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lukakordzaia.imoviesapp.network.Result
import com.lukakordzaia.imoviesapp.datamodels.GenreList
import com.lukakordzaia.imoviesapp.repository.GenresRepository
import com.lukakordzaia.imoviesapp.ui.baseclasses.BaseViewModel
import kotlinx.coroutines.launch

class GenresViewModel(private val repository: GenresRepository) : BaseViewModel() {

    private val _allGenresList = MutableLiveData<List<GenreList.Data>>()
    val allGenresList: LiveData<List<GenreList.Data>> = _allGenresList

    fun onSingleGenrePressed(genreId: Int) {
        navigateToNewFragment(GenresFragmentDirections.actionGenresFragmentToSingleGenreFragment(genreId))
    }

    fun getAllGenres() {
        viewModelScope.launch {
            when (val genres = repository.getAllGenres()) {
                is Result.Success -> {
                    _allGenresList.value = genres.data.data
                    setLoading(false)
                }
                is Result.Error -> {
                    Log.d("errorgenres", genres.exception)
                }
            }
        }
    }
}