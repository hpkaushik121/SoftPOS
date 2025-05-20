package com.aicortex.softpos.models.core.response

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class Info(
    @SerializedName("value")
    @Expose
    var value: String? = null,
    @SerializedName("key")
    @Expose
    var key: String? = null
)
