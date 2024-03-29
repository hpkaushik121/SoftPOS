package com.nownow.softpos.models.dashboard

data class TerminalStatusResponseModel(
    val status: String,
    val errorCode: String,
    val errorDescription: String
)
