package com.aicortex.softpos.models.dashboard

data class CommonDashboardResponse(
    val dashboardHistoryResponse: DashboardHistoryResponse,
    val dashboardUserInfoResponse: DashboardUserInfoResponse,
    val errorCode: String,
    val errorDescription: String,
    val status: String,
    val parentId: String,
    val terminalStatusResponse: TerminalStatusResponseModel
)