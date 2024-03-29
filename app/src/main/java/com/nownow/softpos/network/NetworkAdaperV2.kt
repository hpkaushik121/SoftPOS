package com.nownow.softpos.network
import com.nownow.softpos.BuildConfig
import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceBuilderV2 {
    //private val client = OkHttpClient.Builder().build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.MPOS_SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getOkHttpClient())
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }

    private fun getOkHttpClient(): OkHttpClient {
        val client =  OkHttpClient.Builder()
        client.connectTimeout(5, TimeUnit.MINUTES)
        client.writeTimeout(5, TimeUnit.MINUTES)
        client.readTimeout(5, TimeUnit.MINUTES)
       return client.build()
    }
}