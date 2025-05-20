package com.aicortex.softpos.models.otpResponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MfsOtpStatusInfo(
    @SerializedName("status") @Expose
    var status: String? = null,
    @SerializedName("errorCode")
    @Expose
    private val errorCode: Int? = null,

    @SerializedName("errorDescription")
    @Expose
    val errorDescription: String? = null
)
