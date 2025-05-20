package com.aicortex.softpos.models.appConfig

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class AppConfigResponse(
    @SerializedName("success")
    @Expose
     val success: Boolean? = null,

    @SerializedName("result")
    @Expose
     val result: Result? = null,

    @SerializedName("responseCode")
    @Expose
     val responseCode: String? = null
)

