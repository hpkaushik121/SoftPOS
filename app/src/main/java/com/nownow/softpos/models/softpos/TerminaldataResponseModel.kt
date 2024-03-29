package com.nownow.softpos.models.softpos

import android.os.Parcel
import com.google.gson.annotations.SerializedName


class TerminaldataResponseModel(
    @SerializedName("responseInfo")
    var responseInfo: ResponseInfo?,
    var aid: String?,
    var maskedCard: String?,
    var amount: Int = 0,
    var cardType: String?
)
