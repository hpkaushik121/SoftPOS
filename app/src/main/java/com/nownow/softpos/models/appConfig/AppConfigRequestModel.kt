package com.aicortex.softpos.models.appConfig

import com.google.gson.annotations.SerializedName

data class AppConfigRequestModel(
    @SerializedName("sharedSecretKeyApp")
    private val sharedSecretKeyApp: String? = null,

    @SerializedName("configType")
    private val configType: String? = null
)

