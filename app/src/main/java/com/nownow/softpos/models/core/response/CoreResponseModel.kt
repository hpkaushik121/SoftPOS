package com.nownow.softpos.models.core.response

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose


data class CoreResponseModel(
    @SerializedName("mfsCommonServiceResponse")
    @Expose
    val mfsCommonServiceResponse: MfsCommonServiceResponse? = null,
    @SerializedName("mfsResponseInfo")
    @Expose
    val mfsResponseInfo: MfsResponseInfo? = null,

    @SerializedName("mfsOptionalInfo")
    @Expose
    val mfsOptionalInfo: MfsOptionalInfoResponse? = null

)





