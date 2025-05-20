package com.aicortex.softpos.models.core.request

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose

class MfsOptionalInfo : Parcelable {
    @Expose
    var info: List<Info?>? = null
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(info)
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        info = ArrayList()
        `in`.readList(info as ArrayList<Info?>, Info::class.java.classLoader)
    }

    companion object CREATOR : Parcelable.Creator<MfsOptionalInfo> {
        override fun createFromParcel(parcel: Parcel): MfsOptionalInfo {
            return MfsOptionalInfo(parcel)
        }

        override fun newArray(size: Int): Array<MfsOptionalInfo?> {
            return arrayOfNulls(size)
        }
    }


}