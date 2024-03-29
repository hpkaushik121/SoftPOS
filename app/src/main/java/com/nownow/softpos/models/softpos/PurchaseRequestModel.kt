package com.nownow.softpos.models.softpos

import com.google.gson.annotations.SerializedName


class PurchaseRequestModel(
    @SerializedName("amount")
    var amount: String?,

    @SerializedName("cardDetails")
    var cardDetails: CardDetails?,


    @SerializedName("msisdn")
    var msisdn: String?,

    @SerializedName("requestCts")
    var requestCts: String?,

    @SerializedName("serialId")
    var serialId: String?,

    @SerializedName("cTerminalId")
    private var cTerminalId: String?,

    @SerializedName("transactionId")
    private var mTransactionId: String?,

    @SerializedName("invoiceId")
    var invoiceId: String?,

    @SerializedName("stan")
    var stan: String?,

    @SerializedName("authId")
    var authId: String?,

    @SerializedName("batchId")
    var batchId: String?
)
