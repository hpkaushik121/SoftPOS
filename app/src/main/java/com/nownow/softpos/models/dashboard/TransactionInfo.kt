package com.nownow.softpos.models.dashboard

import android.os.Parcel
import android.os.Parcelable

data class TransactionInfo(

    val receiverName: String,
    val senderName: String,
    var serviceName: String,
    val timeStamp: String,
    val transactionId: String,
    val transactionType: String,
    val transactionStatus: String,
    val transactionAmount: String,
    val transactionName: String,
    val transactionDate: String,
    val transactionRemarks: String,
    val transactionInfo: String,
    val receiverMsisdn: String,
    val transactionFees:String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(receiverName)
        parcel.writeString(senderName)
        parcel.writeString(serviceName)
        parcel.writeString(timeStamp)
        parcel.writeString(transactionId)
        parcel.writeString(transactionType)
        parcel.writeString(transactionStatus)
        parcel.writeString(transactionAmount)
        parcel.writeString(transactionName)
        parcel.writeString(transactionDate)
        parcel.writeString(transactionRemarks)
        parcel.writeString(transactionInfo)
        parcel.writeString(receiverMsisdn)
        parcel.writeString(transactionFees)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TransactionInfo> {
        override fun createFromParcel(parcel: Parcel): TransactionInfo {
            return TransactionInfo(parcel)
        }

        override fun newArray(size: Int): Array<TransactionInfo?> {
            return arrayOfNulls(size)
        }
    }
}