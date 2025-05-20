package com.aicortex.softpos.models.downloadPdfModels

data class DownloadPdfRequest(
    val email_id: String? = null,
    val entityId: String? = null,
    val fromDate: String? = null,
    val msisdn: String? = null,
    val toDate: String? = null,
    val transactionId: String? = null,
    val username: String? = null
)