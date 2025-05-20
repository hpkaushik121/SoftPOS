package com.aicortex.softpos.models.core.request

import android.os.Parcel
import android.os.Parcelable

data class MfsCommonServiceRequest(
    val mfsTransactionInfo: MfsTransactionInfo,
    val mfsSourceInfo: MfsSourceInfo,
    val mfsRequestInfo: MfsRequestInfo? = null
):Parcelable {
    constructor(parcel: Parcel) : this(
        TODO("mfsTransactionInfo"),
        TODO("mfsSourceInfo"),
        TODO("mfsRequestInfo")
    ) {
    }



    companion object CREATOR : Parcelable.Creator<MfsCommonServiceRequest> {
        override fun createFromParcel(parcel: Parcel): MfsCommonServiceRequest {
            return MfsCommonServiceRequest(parcel)
        }

        override fun newArray(size: Int): Array<MfsCommonServiceRequest?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }
}


