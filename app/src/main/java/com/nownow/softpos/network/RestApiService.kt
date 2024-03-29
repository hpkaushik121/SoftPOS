package com.nownow.softpos.network

import android.content.Context
import com.nownow.softpos.api.ApiCalls
import com.nownow.softpos.models.softpos.CommonQposRequestModel
import com.nownow.softpos.models.softpos.TerminaldataResponseModel
import com.nownow.softpos.utils.Constants
import com.nownow.softpos.utils.UtilHandler

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestApiService {

    fun softPosPay(context:Context?,
        token: String?,
        commonQposRequest: CommonQposRequestModel,
        onResult: (TerminaldataResponseModel?) -> Unit) {
        val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
        retrofit.softPosPay(token, Constants.PLATFORM,
            UtilHandler.getPackageVersion(context!!), UtilHandler.getScreenDensity(context!!),commonQposRequest).enqueue(
            object : Callback<TerminaldataResponseModel> {
                override fun onFailure(call: Call<TerminaldataResponseModel>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse( call: Call<TerminaldataResponseModel>, response: Response<TerminaldataResponseModel>) {
                    val responseBody = response.body()
                    //Log.d("responseBody", responseBody!!.responseInfo!!.transactionInfo!!.terminalData.toString())
                    onResult(responseBody)
                }
            } )
    }



}