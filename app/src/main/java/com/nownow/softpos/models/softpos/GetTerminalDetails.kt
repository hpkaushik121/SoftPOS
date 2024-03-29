package com.nownow.softpos.models.softpos

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.nownow.softpos.models.softpos.MposPtsp

class GetTerminalDetails(
    @SerializedName("message")
    @Expose
    var message: String? = null,

    @SerializedName("id")
    @Expose
    val id: Int? = null,

    @SerializedName("msisdn")
    @Expose
    val msisdn: String? = null,

    @SerializedName("entityId")
    @Expose
    val entityId: String? = null,

    @SerializedName("terminalId")
    @Expose
    val terminalId: String? = null,

    @SerializedName("serviceType")
    @Expose
    val serviceType: Int? = null,

    @SerializedName("status")
    @Expose
    val status: Int? = null,

    @SerializedName("deviceCategory")
    @Expose
    val deviceCategory: Any? = null,

    @SerializedName("cterminalId")
    @Expose
    val cterminalId: String? = null,

    @SerializedName("mposPtsp")
    @Expose
    val mposPtsp: MposPtsp? = null,

    @SerializedName("insertDate")
    @Expose
    val insertDate: String? = null,

    @SerializedName("modifiedDate")
    @Expose
    val modifiedDate: String? = null,

    @SerializedName("mappingId")
    @Expose
    val mappingId: Any? = null,

    @SerializedName("cterminalId2")
    @Expose
    val cterminalId2: String? = null,

    @SerializedName("keyUpdated")
    @Expose
    val keyUpdated: Boolean? = null

)
