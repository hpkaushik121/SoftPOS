package com.nownow.softpos.models

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel

class VerifyViewModel : ViewModel() {
    //val otp = ValiFieldOtp()
    var resultOtp: String? = null
    var mDigitsForDone = 4
    //val flag = Constants.USERINFO_STATUS_1
    val mResendClickCount = 0
    var countDownTimer: CountDownTimer? = null
    lateinit var targetMail: String


   /* override fun onCleared() {
        VaildFieldViewModel.destroyFields(otp)
        super.onCleared()
    }*/
}