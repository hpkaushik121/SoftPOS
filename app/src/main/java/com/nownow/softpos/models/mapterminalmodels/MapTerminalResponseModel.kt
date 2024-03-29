package com.nownow.softpos.models.mapterminalmodels

import com.google.gson.annotations.SerializedName

data class MapTerminalResponseModel(
    @SerializedName("status"     ) var status     : Boolean?    = null,
    @SerializedName("message"    ) var message    : String?     = null,
    @SerializedName("resultBody" ) var resultBody : MapTerminalResultBody? = MapTerminalResultBody()
)
