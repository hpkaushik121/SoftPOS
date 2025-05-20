package com.aicortex.softpos.models.otpResponse

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MfsOtpOptionalInfo(
    @SerializedName("info")
    @Expose
    private var info: Any? = null
)
