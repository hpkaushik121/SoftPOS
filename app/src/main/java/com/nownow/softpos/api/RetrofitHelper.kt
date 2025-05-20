package com.aicortex.softpos.api

import com.aicortex.softpos.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    fun getInstance():Retrofit{
        return  Retrofit.Builder()
            /*.baseUrl(BuildConfig.MPOS_SERVER_URL)*/
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}