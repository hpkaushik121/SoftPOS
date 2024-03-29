package com.nownow.softpos.models.dashboard

import com.google.gson.annotations.SerializedName

data class CommonDashboardResponseBaseModel(
    @SerializedName("commonDashboardResponse" )
    var commonDashboardResponse : CommonDashboardResponse?
)
