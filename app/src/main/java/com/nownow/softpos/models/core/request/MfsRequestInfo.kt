package com.aicortex.softpos.models.core.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MfsRequestInfo(
    @SerializedName("token")
    @Expose
    var mToken: String? = null,

    @SerializedName("accessCode")
    @Expose
    var mAccessCode: String? = null,

    @SerializedName("email")
    @Expose
    var email: String? = null,

    @SerializedName("entityType")
    @Expose
    var entityType: String? = null,

    @SerializedName("entityId")
    @Expose
    var entityId: String? = null,

    @SerializedName("language")
    @Expose
    var language: String? = null,

    @SerializedName("msisdn")
    @Expose
    var msisdn: String? = null,
    /** customerMsisdn used to hit getUserInfo using  msisdn*/
    @SerializedName("customerMsisdn")
    @Expose
    var customerMsisdn: String? = null,

    @SerializedName("length")
    @Expose
    var length: String? = null,
    @SerializedName("newMpin")
    @Expose
    var newMpin: String? = null,
    @SerializedName("type")
    @Expose
    var type: String? = null,
    @SerializedName("notificationFlag")
    @Expose
    var notificationFlag: String? = null,
    @SerializedName("historyCount")
    @Expose
    var historyCount: String? = null,
    @SerializedName("terminalId")
    @Expose
    var terminalId: String? = null,
    @SerializedName("serviceName")
    @Expose
    var serviceName: String? = null,
    @SerializedName("serviceId")
    @Expose
    var serviceId: String? = null,
    /*Request for move fund wallet API*/
    @SerializedName("fromEntityId")
    @Expose
    var fromEntityId: String? = null,
    @SerializedName("amount")
    @Expose
    var amount: Int? = null,
    @SerializedName("walletTypeToDeduct")
    @Expose
    var walletTypeToDeduct: Int? = null,
    @SerializedName("toEntityId")
    @Expose
    var toEntityId: String? = null,
    @SerializedName("walletTypeToCredit")
    @Expose
    var walletTypeToCredit: Int? = null,

    @SerializedName("phone")
    @Expose
    var phone: Int? = null,
    @SerializedName("count")
    @Expose
    var count: String? = null,
    @SerializedName("offSet")
    @Expose
    var offSet: String? = null,
    @SerializedName("transactionId")
    @Expose
    var transactionId: String? = null

)
