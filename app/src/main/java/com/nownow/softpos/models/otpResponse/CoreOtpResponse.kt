package com.aicortex.softpos.models.otpResponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CoreOtpResponse(
    @SerializedName("mfsCommonServiceResponse")
    @Expose
    var mfsCommonServiceResponse: MfsOtpCommonServiceResponse? = null,
    @SerializedName("mfsResponseInfo")
    @Expose
    val mfsResponseInfo: String? = null,

    @SerializedName("mfsOptionalInfo")
    @Expose
    val mfsOptionalInfo: MfsOtpOptionalInfo? = null
)
