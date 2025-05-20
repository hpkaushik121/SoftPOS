package com.aicortex.softpos.models.core.request

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class MfsTransactionInfo(
    @SerializedName("timestamp")
    @Expose
    var timestamp: Long = 0,
    @SerializedName("requestId")
    @Expose
    var requestId: String? = null,

    @SerializedName("serviceType")
    @Expose
    var serviceType: String? = null

)






