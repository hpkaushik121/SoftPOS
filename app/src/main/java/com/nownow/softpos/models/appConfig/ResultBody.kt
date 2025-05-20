package com.aicortex.softpos.models.appConfig

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class ResultBody(
    @SerializedName("configType")
    @Expose
    val configType: String? = null,

    @SerializedName("config")
    @Expose
    val config: List<Config>? = null
)
