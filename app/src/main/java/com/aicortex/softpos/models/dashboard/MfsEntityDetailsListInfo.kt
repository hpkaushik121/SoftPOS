package com.aicortex.softpos.models.dashboard

import com.aicortex.softpos.models.core.response.MfsEntityInfo

data class MfsEntityDetailsListInfo(
    val additionalFields: List<AdditionalField>,
    val mfsBiomtericInfo: List<MfsBiomtericInfo>,
    val mfsEntityInfo: List<MfsEntityInfo>,
    val mfsNinInfo: List<MfsNinInfo>
)