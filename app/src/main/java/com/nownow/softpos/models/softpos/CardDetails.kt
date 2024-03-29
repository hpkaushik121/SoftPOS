package com.nownow.softpos.models.softpos
data class CardDetails(
    val aid: String,
    val cardHolderName: String,
    val cardNumber: String,
    val cardType: String,
    val expiryDate: String,
    val track2: String,
    var pinBlock: String,
    val iccData: String,
    val panSeq: String
    
) 
    