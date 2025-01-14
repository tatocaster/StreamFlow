package com.lukakordzaia.core.network.interceptors

import com.lukakordzaia.core.sharedpreferences.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class DefaultHeaderInterceptor(private val sharedPreferences: SharedPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()

        request = request.newBuilder()
            .addHeader("User-Agent", "imovies")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${sharedPreferences.getLoginToken()}" )
            .build()
        
        return chain.proceed(request)
    }
}