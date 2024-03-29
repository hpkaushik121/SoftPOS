package com.nownow.softpos.models.dashboard

data class Wallet(
    val availBalance: String,
    val curBalance: String,
    val entityId: String,
    val reservedBalance: String,
    val walletType: Double
)