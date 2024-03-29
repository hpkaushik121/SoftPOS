package com.nownow.softpos.models.softpos

import com.google.gson.annotations.SerializedName

class QPOSHistory(
    @SerializedName("amount")
    var amount:Int = 0,

    @SerializedName("expiryDate")
    var expiryDate: String?,

    @SerializedName("pan")
    var pan: String?,

    @SerializedName("rrn")
    var rrn: String?,

    @SerializedName("stan")
    var stan: String?,

    @SerializedName("status")
    var status: Long?,

    @SerializedName("transactionDate")
    var transactionDate: String?
)

