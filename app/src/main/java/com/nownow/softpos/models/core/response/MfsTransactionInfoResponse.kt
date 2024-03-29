package com.nownow.softpos.models.core.response

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class MfsTransactionInfoResponse(
    @SerializedName("timestamp")
    @Expose
    val timestamp: String? = null,
    @SerializedName("requestId")
    @Expose
    val requestId: String? = null,
    @SerializedName("responseId")
    @Expose
    val responseId: String? = null
)


