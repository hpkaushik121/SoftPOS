package com.nownow.softpos.models.softpos
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class TransactionInfo(
    @SerializedName("amount")
    var amount: Int = 0,

    @SerializedName("authId")
    var authId: String?=null,

    @SerializedName("batchId")
    var batchId: String?=null,

    @SerializedName("CardCategory")
    var cardCategory: String?=null,

    @SerializedName("isoRequest")
    var isoRequest: IsoRequest?=null,

    @SerializedName("cardType")
    var cardType: String?=null,

    @SerializedName("cTerminalId")
     var cTerminalId: String?=null,

    @SerializedName("invoiceId")
    var invoiceId: String?=null,

    @SerializedName("stan")
    var stan: String?=null,

    @SerializedName("comments")
    var comments: String?=null,

    @SerializedName("resultCode")
    var resultCode: Int = -101,

    @SerializedName("resultDesc")
    var resultDesc: String?=null,

    @SerializedName("rrn")
    var rrn: String?=null,

    @SerializedName("vendoreResultCode")
    var vendoreResultCode: Int = -101,

    @SerializedName("vendoreResultDesc")
    var vendoreResultDesc: String?=null,

    @SerializedName("terminalData")
    var terminalData: String?=null,

    @SerializedName("history")
    var history: List<QPOSHistory?>?=null,

    @SerializedName("isRefunded")
    var isRefunded: Boolean = false,

    @SerializedName("successCount")
    @Expose
    var successCount: String?=null,

    @SerializedName("declineAmount")
    @Expose
    var declineAmount: String?=null,

    @SerializedName("successAmount")
    @Expose
    var successAmount: String?=null,

    @SerializedName("declineCount")
    @Expose
    var declineCount: String?=null,

    @SerializedName("reverseAmount")
    @Expose
    var reverseAmount: String?=null,

    @SerializedName("reverseCount")
    @Expose
    var reverseCount: String?=null,
):Serializable


