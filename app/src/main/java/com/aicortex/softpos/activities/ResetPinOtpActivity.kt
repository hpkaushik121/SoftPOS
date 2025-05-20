package com.aicortex.softpos.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import com.aicortex.softpos.R
import com.aicortex.softpos.databinding.ActivityResetPinOtpBinding

class ResetPinOtpActivity : AppCompatActivity() {
    lateinit var binding: ActivityResetPinOtpBinding
    var firstStringForEmailOrWallet = " "
    var secondStringForEmailOrWallet = " "
    var fullStringForPleaseTypeOtpWallet = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_pin_otp)

        fullStringForPleaseTypeOtpWallet = getString(R.string.please_type_pin_otp)+"<b> registered email or phone number.</b>"
        binding.textPleaseTypeOtpPin.text = HtmlCompat.fromHtml(fullStringForPleaseTypeOtpWallet, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.otpValidateTxt.setOnClickListener {
            val intent = Intent(this, ChangeAndConfirmPinActivity::class.java)
            startActivity(intent)
        }
    }
}