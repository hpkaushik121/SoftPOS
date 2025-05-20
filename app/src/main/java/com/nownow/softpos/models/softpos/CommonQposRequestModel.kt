package com.aicortex.softpos.models.softpos

import com.google.gson.annotations.SerializedName


data class CommonQposRequestModel(
    @SerializedName("requestInfo")
    var requestInfo: RequestInfo?
)

