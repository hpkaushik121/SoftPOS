package com.nownow.softpos.models.core.response

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose


class MfsCommonServiceResponse(
    @SerializedName("mfsStatusInfo")
    @Expose
    val mfsStatusInfo: MfsStatusInfo? = null,
    @SerializedName("mfsTransactionInfo")
    @Expose
    val mfsTransactionInfo: MfsTransactionInfoResponse? = null
)



