package com.nownow.softpos.models.core.request

import android.os.Parcelable
import com.google.gson.annotations.Expose
import android.os.Parcel
import android.os.Parcelable.Creator

class Info(
    @Expose
    var key: String? = null,
    @Expose
    var value: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<Info> {
        override fun createFromParcel(parcel: Parcel): Info {
            return Info(parcel)
        }

        override fun newArray(size: Int): Array<Info?> {
            return arrayOfNulls(size)
        }
    }
}







