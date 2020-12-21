package com.lukakordzaia.imoviesapp.repository

import com.lukakordzaia.imoviesapp.network.Result
import com.lukakordzaia.imoviesapp.network.RetrofitBuilder
import com.lukakordzaia.imoviesapp.network.TitlesNetwork
import com.lukakordzaia.imoviesapp.network.datamodels.TitleData

class SingleTitleRepository {
    private val destinationService = RetrofitBuilder.buildService(TitlesNetwork::class.java)

    suspend fun getSingleTitleData(movieId: Int): Result<TitleData> {
        return RetrofitBuilder.retrofitCall { destinationService.getSingleTitle(movieId) }
    }
}