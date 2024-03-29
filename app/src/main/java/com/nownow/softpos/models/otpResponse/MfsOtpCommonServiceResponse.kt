package com.nownow.softpos.models.otpResponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MfsOtpCommonServiceResponse(
    @SerializedName("mfsStatusInfo")
    @Expose
    var mfsStatusInfo: MfsOtpStatusInfo? = null,
    @SerializedName("mfsTransactionInfo")
    @Expose
    val mfsTransactionInfo: MfsOtpTransactionInfo? = null
)
