package com.nownow.softpos.models.softpos

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import android.os.Parcel
import android.os.Parcelable.Creator
import java.util.HashMap

class IsoRequest (
    var cardData: HashMap<String, String>?=null ,
    var nibssRequest: ByteArray?=null
    )