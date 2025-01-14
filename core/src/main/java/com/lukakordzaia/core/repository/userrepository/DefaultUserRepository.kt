package com.lukakordzaia.core.repository.userrepository

import com.lukakordzaia.core.network.Result
import com.lukakordzaia.core.network.imovies.ImoviesCall
import com.lukakordzaia.core.network.imovies.ImoviesNetwork
import com.lukakordzaia.core.network.models.imovies.request.user.PostLoginBody
import com.lukakordzaia.core.network.models.imovies.request.user.PostTitleWatchTimeRequestBody
import com.lukakordzaia.core.network.models.imovies.response.user.GetUserDataResponse
import com.lukakordzaia.core.network.models.imovies.response.user.GetUserLogoutResponse
import com.lukakordzaia.core.network.models.imovies.response.user.PostUserLoginResponse
import com.lukakordzaia.core.network.models.imovies.response.user.PostWatchTimeResponse

class DefaultUserRepository(private val service: ImoviesNetwork): ImoviesCall(), UserRepository {
    override suspend fun userLogin(loginBody: PostLoginBody): Result<PostUserLoginResponse> {
        return imoviesCall { service.postUserLogin(loginBody) }
    }

    override suspend fun userData(): Result<GetUserDataResponse> {
        return imoviesCall { service.getUserData() }
    }

    override suspend fun userLogout(): Result<GetUserLogoutResponse> {
        return imoviesCall { service.postUserLogOut() }
    }

    override suspend fun titleWatchTime(
        watchTimeRequest: PostTitleWatchTimeRequestBody,
        titleId: Int,
        season: Int,
        episode: Int
    ): Result<PostWatchTimeResponse> {
        return imoviesCall { service.postTitleWatchTime(watchTimeRequest, titleId, season, episode) }
    }
}