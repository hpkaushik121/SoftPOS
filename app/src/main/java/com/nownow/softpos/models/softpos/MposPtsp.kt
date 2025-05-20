package com.aicortex.softpos.models.softpos

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MposPtsp(
    @SerializedName("id") @Expose
    var id: Int? = null,
    @SerializedName("status")
    @Expose
    val status: String? = null,
    @SerializedName("ptsp")
    @Expose
    var ptsp: String? = null
)
