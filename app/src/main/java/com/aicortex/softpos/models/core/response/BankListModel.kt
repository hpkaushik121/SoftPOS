package com.aicortex.softpos.models.core.response
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.aicortex.softpos.models.core.response.BankListData
class BankListModel(
    @SerializedName("status_code")
    @Expose
     val statusCode: Int? = null,

    @SerializedName("status")
    @Expose
     val status: String? = null,

    @SerializedName("message")
    @Expose
     val message: String? = null,

    @SerializedName("object")
    @Expose
     val object_: List<BankListData>? = null
)
