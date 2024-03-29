package com.nownow.softpos.models.appConfig

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class Config(
    @SerializedName("key")
    @Expose
    val key: String? = null,

    @SerializedName("value")
    @Expose
    val value: String? = null
) {

}