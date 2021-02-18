package com.lukakordzaia.streamflow.repository

import com.lukakordzaia.streamflow.datamodels.*
import com.lukakordzaia.streamflow.network.Result
import com.lukakordzaia.streamflow.network.RetrofitBuilder
import com.lukakordzaia.streamflow.network.traktv.TraktvCall
import com.lukakordzaia.streamflow.utils.AppConstants

class TraktRepository(retrofitBuilder: RetrofitBuilder): TraktvCall() {
    private val service = retrofitBuilder.buildTraktvService()

    suspend fun getDeviceCode(): Result<TraktvDeviceCode> {
        return traktvCall { service.getDeviceCode(SendTraktvClientId(AppConstants.TRAKTV_CLIENT_ID)) }
    }

    suspend fun getUserToken(tokenRequest: TraktRequestToken): Result<TraktGetToken> {
        return traktvCall { service.getUserToken(tokenRequest) }
    }

    suspend fun createNewList(newList: TraktNewList, accessToken: String): Result<String> {
        return traktvCall { service.createNewList(newList, accessToken) }
    }

    suspend fun getSfList(accessToken: String): Result<TraktGetUserList> {
        return traktvCall { service.getStreamFlowList(accessToken) }
    }

    suspend fun getUserProfile(accessToken: String): Result<TraktUserProfile> {
        return traktvCall { service.getUserProfile(accessToken) }
    }
}