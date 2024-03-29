package com.nownow.softpos.models.otpResponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MfsOtpTransactionInfo(
    @SerializedName("timestamp") @Expose
    var timestamp: String? = null,
    @SerializedName("requestId")
    @Expose
    val requestId: String? = null,

    @SerializedName("responseId")
    @Expose
    val responseId: String? = null
)
