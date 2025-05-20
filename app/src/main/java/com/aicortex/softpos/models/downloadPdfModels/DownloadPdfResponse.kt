package com.aicortex.softpos.models.downloadPdfModels

data class DownloadPdfResponse(
    var link: String,
    var message: String,
    var status: Boolean
)