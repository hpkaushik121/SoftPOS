package com.nownow.softpos.models.core.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.nownow.softpos.models.dashboard.TransactionListInfo

data class MfsResponseInfo(
    @SerializedName("token")
    @Expose
    val token: String? = null,
    val mfsEntityDetailsListInfo: MfsEntityDetailsListInfo? = null,
    @SerializedName("msisdn")
    @Expose
    val msisdn: String? = null,
    @SerializedName("deviceId")
    @Expose
    val deviceId: String? = null,
    @SerializedName("securityQuestions")
    @Expose
    val securityQuestions: String? = null,
    @SerializedName("status")
    @Expose
    val status: String? = null,
    @SerializedName("emailStatus")
    @Expose
    val emailStatus: String? = null,
    @SerializedName("email")
    @Expose
    val email: String? = null,
    @SerializedName("entityType")
    @Expose
    val entityType: String? = null,
    @SerializedName("entitySubType")
    @Expose
    val entitySubType: String? = null,
    @SerializedName("passwordFlag")
    @Expose
    val passwordFlag: String? = null,
    @SerializedName("mpinFlag")
    @Expose
    val mpinFlag: String? = null,
    @SerializedName("kycStatus")
    @Expose
    val kycStatus: String? = null,
    @SerializedName("entityId")
    @Expose
    val entityId: String? = null,
    val transactionListInfo: TransactionListInfo? = null,
    @SerializedName("transactionId")
    @Expose
    val transactionId: String? = null,
    @SerializedName("transactionStatus")
    @Expose
    val transactionStatus: String? = null,
    @SerializedName("transactionAmount")
    @Expose
    val transactionAmount: String? = null,
    @SerializedName("transactionDate")
    @Expose
    val transactionDate: String? = null,
    @SerializedName("transactionRemarks")
    @Expose
    val transactionRemarks: String? = null,
    @SerializedName("senderName")
    @Expose
    val senderName: String? = null,
    @SerializedName("receiverName")
    @Expose
    val receiverName: String? = null,
    @SerializedName("senderMsisdn")
    @Expose
    val senderMsisdn: String? = null,
    @SerializedName("receiverMsisdn")
    @Expose
    val receiverMsisdn: String? = null,

    @SerializedName("transactionFees")
    @Expose
    val transactionFees: String? = null,

)
