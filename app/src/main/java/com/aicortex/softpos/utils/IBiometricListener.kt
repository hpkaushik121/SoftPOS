package com.aicortex.softpos.utils

interface IBiometricListener {
    fun onEnrolled()
    fun onNegativeClick()
    fun onSuccess()
    fun onCancel(errString: String)
    fun onAuthError()
}