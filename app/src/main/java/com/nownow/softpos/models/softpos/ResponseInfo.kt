package com.nownow.softpos.models.softpos

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ResponseInfo(
    @SerializedName("msisdn")
    var msisdn: String?,

    @SerializedName("requestCts")
    var requestCts: String?,

    @SerializedName("responseCts")
    var responseCts: String?,

    @SerializedName("responseType")
    var responseType: String?,

    @SerializedName("serialId")
    var serialId: String?,

    @SerializedName("terminalId")
    var terminalId: String?,

    @SerializedName("transactionId")
    var transactionId: String?,

    @SerializedName("transactionInfo")
    var transactionInfo: TransactionInfo?,

    @SerializedName("isKeyUpdated")
    var isKeyUpdated: Boolean = false
):Serializable