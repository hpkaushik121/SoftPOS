package com.nownow.softpos.models.dashboard

data class TerminalStatusResponse(
    val status: String,
    val errorCode: String,
    val errorDescription: String
)
