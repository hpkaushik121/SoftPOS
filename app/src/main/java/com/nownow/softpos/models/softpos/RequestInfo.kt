package com.nownow.softpos.models.softpos

import com.google.gson.annotations.SerializedName


class RequestInfo(

    @SerializedName("msisdn")
    var msisdn: String?=null,

    @SerializedName("requestCts")
    var requestCts: String?=null,

    @SerializedName("requestType")
    var requestType: String?=null,

    @SerializedName("serialId")
    var serialId: String?=null,

    @SerializedName("terminalId")
    var terminalId: String?=null,

    @SerializedName("transactionInfo")
    var transactionInfo: TransactionInfo?=null,

    @SerializedName("isKeyUpdated")
    var isKeyUpdated: Boolean = false,

    @SerializedName("rrn")
    var rRN: String?=null,

    @SerializedName("transactionId")
    var transactionId: String?=null
)

