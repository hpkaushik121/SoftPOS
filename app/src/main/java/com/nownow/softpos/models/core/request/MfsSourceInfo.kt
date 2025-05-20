package com.aicortex.softpos.models.core.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MfsSourceInfo(
    @SerializedName("channelId")
    @Expose
    var channelId: String? = null,

    @SerializedName("surroundSystem")
    @Expose
    var surroundSystem: String? = null,
    )





