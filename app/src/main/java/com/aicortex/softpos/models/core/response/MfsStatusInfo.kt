package com.aicortex.softpos.models.core.response

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class MfsStatusInfo(
    @SerializedName("status")
    @Expose
    val status: String? = null,
    @SerializedName("errorCode")
    @Expose
    val errorCode: Int? = null,
    @SerializedName("errorDescription")
    @Expose
    val errorDescription: String? = null
)




