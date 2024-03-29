package com.nownow.softpos.models.core.request

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.google.gson.annotations.Expose

class MfsCommonSourceInfo : Parcelable {
    @Expose
    var channelId: String? = null

    @Expose
    var clientTransactionId: String? = null

    @Expose
    var initiatorMsisdn: String? = null

    @Expose
    var requestId: String? = null

    @Expose
    var surroundSystem: String? = null

    @Expose
    var timestamp: String? = null

    @Expose
    var transType: String? = null

    @Expose
    var userId: String? = null
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(channelId)
        dest.writeString(clientTransactionId)
        dest.writeString(initiatorMsisdn)
        dest.writeString(requestId)
        dest.writeString(surroundSystem)
        dest.writeString(timestamp)
        dest.writeString(transType)
        dest.writeString(userId)
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        channelId = `in`.readString()
        clientTransactionId = `in`.readString()
        initiatorMsisdn = `in`.readString()
        requestId = `in`.readString()
        surroundSystem = `in`.readString()
        timestamp = `in`.readString()
        transType = `in`.readString()
        userId = `in`.readString()
    }

    companion object CREATOR : Creator<MfsCommonSourceInfo> {
        override fun createFromParcel(parcel: Parcel): MfsCommonSourceInfo {
            return MfsCommonSourceInfo(parcel)
        }

        override fun newArray(size: Int): Array<MfsCommonSourceInfo?> {
            return arrayOfNulls(size)
        }
    }


}