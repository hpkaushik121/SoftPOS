package com.nownow.softpos.models.appConfig

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose


data class Result ( @SerializedName("resultBody")
               @Expose
               val resultBody: ResultBody? = null){

}