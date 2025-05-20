package com.aicortex.softpos.models.core.response

import com.aicortex.softpos.models.dashboard.WalletInfo

data class MfsEntityInfo(
    val idType: String? = null,
    val cityId: String? = null,
    val segment: String? = null,
    val cityName: String? = null,
    val entityId: String? = null,
    val entitySubType: String? = null,
    val entityType: String? = null,
    val gender: String? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val idNo: String? = null,
    val regionCode: String? = null,
    val lastName: String? = null,
    val placeOfBirth: String? = null,
    val idPhoto: String? = null,
    val motherName: String? = null,
    val status: String? = null,
    val msisdn: String? = null,
    val idPhotoSelfie: String? = null,
    val countryCode: String? = null,
    val regionName: String? = null,
    val createdOn: String? = null,
    val kycRequest: String? = null,
    val last_modified_date: String? = null,
    val createdBy: String? = null,
    val email: String? = null,
    val address: String? = null,
    val dob: String? = null,
    val profilePic: String? = null,
    val language: String? = null,
    val idNo2: String? = null,
    val idPhoto2: String? = null,
    val businessName: String? = null,
    val idType2: String? = null,
    val walletInfo: WalletInfo? = null
)
     
