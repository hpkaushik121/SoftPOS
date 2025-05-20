package com.aicortex.softpos.models.core.response

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class BankListData(
    @SerializedName("bankName")
    @Expose
     val bankName: String? = null,

    @SerializedName("accNo")
    @Expose
     val accNo: String? = null,

    @SerializedName("accName")
    @Expose
     val accName: String? = null,

    @SerializedName("bin")
    @Expose
     val bin: String? = null,

    @SerializedName("icon")
    @Expose
     val icon: String? = null
)
