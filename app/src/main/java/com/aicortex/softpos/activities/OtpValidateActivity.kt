package com.aicortex.softpos.activities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.api.CheckDialogEventInterface
import com.aicortex.softpos.databinding.ActivityOtpValidateBinding
import com.aicortex.softpos.helper.BiometricHelper
import com.aicortex.softpos.models.VerifyViewModel
import com.aicortex.softpos.models.core.request.*
import com.aicortex.softpos.models.core.response.CoreResponseModel
import com.aicortex.softpos.models.otpResponse.CoreOtpResponse
import com.aicortex.softpos.network.ServiceBuilder
import com.aicortex.softpos.utils.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpValidateActivity : BaseActivity(), OnClickListener, CheckDialogEventInterface {
    private lateinit var mViewModel: VerifyViewModel
    lateinit var binding: ActivityOtpValidateBinding
    var emailorwalletid: String? = null
    var email_wallet: String? = null
    var firstStringForEmailOrWallet = " "
    var secondStringForEmailOrWallet = " "
    var fullStringForPleaseTypeOtpWallet = ""

    var otpOnEmailOrSms = ""
    var otpString: String = ""

    var finalResul: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otp_validate)
        emailorwalletid = intent.getStringExtra(Constants.EDITTEXTVALUE)
        email_wallet = intent.getStringExtra(Constants.TYPEWALLETOREMAIL)
        otpOnEmailOrSms = intent.getStringExtra(Constants.OTP_ON_SMS_OR_EMAIL).toString()
        firstStringForEmailOrWallet = getString(R.string.please_type_login_otp)
        updateUI()
        setupListeners()
        mViewModel = ViewModelProvider(this).get(VerifyViewModel::class.java)
        //mViewModel = ViewModelProviders.of(this).get(VerifyViewModel::class.java)
        binding.notGetOtpTxt.setOnClickListener {
            resentOtp(SharedPrefUtils.tempMsisdn.toString())
        }
        // Set the screen to stay on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        startTimerForOtp()

        this?.let { BiometricHelper.initializeBiometricScannerLogin(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this?.let { BiometricHelper.initializeBiometricScanner(it) }
        }
    }

    private fun startTimerForOtp() {
        if (mViewModel.countDownTimer != null) {
            mViewModel.countDownTimer!!.cancel()
        }

        if (intent.getStringExtra(Constants.REQUEST_FROM).equals(Constants.FROM_LOGIN)) {

            var millis = UtilHandler.millisLeft(SharedPrefUtils.tempMsisdn, false)
            if (millis == 0L)
                millis = UtilHandler.millisLeft(SharedPrefUtils.tempMsisdn, true)

            mViewModel.countDownTimer =
                MyCountDownTimer(millis, 500)
        } else
            mViewModel.countDownTimer =
                MyCountDownTimer(Constants.COUNTER_TIME_FIRST, 500)
        mViewModel.countDownTimer!!.start()

    }

    fun updateUI() {
        binding.textWrongAndRightOtp.visibility = View.GONE
        if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_FORGET_PIN) {
            binding.linearProgressBarContainer.visibility = View.GONE
            binding.textOtpScreenHeader.text = getString(R.string.reset_pin)
            secondStringForEmailOrWallet = "<b> registered email address.</b>"
            fullStringForPleaseTypeOtpWallet =
                "$firstStringForEmailOrWallet<b>$secondStringForEmailOrWallet</b>"
            //binding.textPleaseTypeOtp.text = HtmlCompat.fromHtml(fullStringForPleaseTypeOtpWallet, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_LOGIN) {
            binding.textOtpScreenHeader.text = getString(R.string.otp_validation)
        } else {
            binding.textOtpScreenHeader.text = getString(R.string.otp_validation)
        }


        if (emailorwalletid != null) {
            if (email_wallet != null) {
                if (email_wallet.equals("WALLET")) {
                    secondStringForEmailOrWallet = "<b>+234</b> <b>$emailorwalletid</b>"
                    fullStringForPleaseTypeOtpWallet =
                        "$firstStringForEmailOrWallet Wallet ID number <b>$secondStringForEmailOrWallet</b>"
                    //binding.textPleaseTypeOtp.text = HtmlCompat.fromHtml(fullStringForPleaseTypeOtpWallet, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    binding.textPleaseTypeOtp.text = getString(R.string.kindly_add_6digit_number)
                    binding.linearLoader23Wallet.visibility = View.VISIBLE
                    binding.linearLoader22FullEmailPin.visibility = View.GONE
                    binding.footerTextEmailOrLogin.text = "Email Address"

                } else {
                    secondStringForEmailOrWallet = "<b>$emailorwalletid</b>"
                    fullStringForPleaseTypeOtpWallet =
                        "$firstStringForEmailOrWallet Email ID <b>$secondStringForEmailOrWallet</b>"
                    //binding.textPleaseTypeOtp.text = HtmlCompat.fromHtml(fullStringForPleaseTypeOtpWallet, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    binding.textPleaseTypeOtp.text =
                        getString(R.string.kindly_add_6digit_number_on_email)
                    binding.linearLoader23Wallet.visibility = View.GONE
                    binding.linearLoader22FullEmailPin.visibility = View.VISIBLE
                    binding.footerTextEmailOrLogin.text = "Wallet ID"
                }
            } else {
            }

        } else {
        }
    }

    private fun setupListeners() {
        /*Disable all edittext first time*/
        binding.edt2.isEnabled = false
        binding.edt3.isEnabled = false
        binding.edt4.isEnabled = false
        binding.edt5.isEnabled = false
        binding.edt6.isEnabled = false
        // Set text change listeners for all EditTexts
        binding.edt1.addTextChangedListener(textWatcher)
        binding.edt2.addTextChangedListener(textWatcher)
        binding.edt3.addTextChangedListener(textWatcher)
        binding.edt4.addTextChangedListener(textWatcher)
        binding.edt5.addTextChangedListener(textWatcher)
        binding.edt6.addTextChangedListener(textWatcher)

        // Set key listeners for all EditTexts
        binding.edt1.setOnKeyListener(keyListener)
        binding.edt2.setOnKeyListener(keyListener)
        binding.edt3.setOnKeyListener(keyListener)
        binding.edt4.setOnKeyListener(keyListener)
        binding.edt5.setOnKeyListener(keyListener)
        binding.edt6.setOnKeyListener(keyListener)

        binding.footerTextEmailOrLogin.setOnClickListener(this)
        binding.backArrowBtn.setOnClickListener(this)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            if (s.length == 1) {
                // Move focus to the next EditText when a digit is entered
                when (s.hashCode()) {
                    binding.edt1.text.hashCode() -> edt1PinAction(s)
                    binding.edt2.text.hashCode() -> edt2PinAction(s)
                    binding.edt3.text.hashCode() -> edt3PinAction(s)
                    binding.edt4.text.hashCode() -> edt4PinAction(s)
                    binding.edt5.text.hashCode() -> edt5PinAction(s)
                    binding.edt6.text.hashCode() -> edt6PinAction(s)

                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.footerTextEmailOrLogin -> backToLogin()
            R.id.backArrowBtn -> onBackPressed()
        }
    }

    private fun backToLogin() {
        val intent = Intent(this@OtpValidateActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    private val keyListener = View.OnKeyListener { view, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            // Move focus to the previous EditText when the Backspace key is pressed
            when (view) {
                is EditText -> {
                    when (view) {
                        binding.edt6 -> {
                            edt6KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt5 -> {
                            edt5KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt4 -> {
                            edt4KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt3 -> {
                            edt3KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt2 -> {
                            edt2KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        else -> {
                            // First EditText, do nothing
                        }
                    }
                }
            }
        }
        false
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.countDownTimer!!.cancel()
    }

    private fun verifyOtp(otp: String) {
        try {
            Log.d("Token", SharedPrefUtils.tokenKey.toString())
            if (UtilHandler.isOnline(this)) {
                showProgressDialog(getString(R.string.progress_dialog_message))
                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = "0"
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = Constants.CHANNEL_ID_23
                mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_4

                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                val mfsRequestInfo = MfsRequestInfo()
                mfsRequestInfo.entityId = SharedPrefUtils.tempMsisdn
                mfsRequestInfo.entityType = SharedPrefUtils.entityType
                mfsRequestInfo.language = "ENG"
                //mfsRequestInfo.mToken = UtilHandler.enCryptPassword(otp)
                mfsRequestInfo.mToken = otp
                mfsRequestInfo.phone = 0
                // mfsRequestInfo.mtoken = ""

                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../validate", Gson().toJson(coreServiceRequest))
                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.verifyOtp(
                    SharedPrefUtils.tokenKey,
                    Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this),
                    UtilHandler.getScreenDensity(this),
                    coreServiceRequest
                )
                    .enqueue(
                        object : Callback<CoreOtpResponse> {
                            override fun onResponse(
                                call: Call<CoreOtpResponse>,
                                response: Response<CoreOtpResponse>
                            ) {
                                DialogUtils.hideDialog()
                                if (response.isSuccessful) {
                                    val mCoreResponseModel: CoreOtpResponse? = response.body()
                                    if (!BuildConfig.USE_PRODUCTION_URLS)
                                        Log.d(
                                            "Http..../validate",
                                            Gson().toJson(mCoreResponseModel)
                                        )

                                    if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status != null) {

                                        if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status.equals(
                                                "Success"
                                            )
                                        ) {
                                            processToNextScreen()
                                        } else {
                                            //processToNextScreen()
                                            updateOtpStatusText()
                                            //processToNextScreen()
                                            UtilHandler.showSnackBar(
                                                mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.errorDescription,
                                                binding.root
                                            )
                                        }

                                    } else {
                                        UtilHandler.showSnackBar(
                                            getString(R.string.something_went_wrong),
                                            binding.root
                                        )
                                    }
                                }

                            }

                            override fun onFailure(call: Call<CoreOtpResponse>, t: Throwable) {
                                DialogUtils.hideDialog()
                                Log.d(Constants.LOG_D_RESPONSE, "t.message.toString()")
                                UtilHandler.showSnackBar(
                                    getString(R.string.something_went_wrong),
                                    binding.root
                                )
                            }

                        }
                    )
            } else {
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
            }
        } catch (e: Exception) {
            hideProgressDialog()
            Log.d("Exception", e.message.toString())
            e.message
        }
    }

    private fun processToNextScreen() {
        binding.textWrongAndRightOtp.visibility = View.GONE
        if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_FORGET_PIN) {
            clearMsisdnOTPCache(true)
            val intent = Intent(this, ChangeAndConfirmPinActivity::class.java)
            intent.putExtra(Constants.REQUEST_FROM, Constants.FROM_FORGET_PIN)
            intent.putExtra(Constants.EDITTEXTVALUE, emailorwalletid)
            intent.putExtra(Constants.TYPEWALLETOREMAIL, email_wallet)
            startActivity(intent)
            finish()
        } else {
            if (email_wallet != null) {
                if (email_wallet.equals(Constants.WALLET)) {
                    clearAllEdittext()
                    clearMsisdnOTPCache(true)
                    val intent = Intent(this, ChangeAndConfirmPinActivity::class.java)
                    intent.putExtra(Constants.REQUEST_FROM, Constants.VERIFY_PIN_FROM_WALLET)
                    intent.putExtra(Constants.EDITTEXTVALUE, emailorwalletid)
                    intent.putExtra(Constants.TYPEWALLETOREMAIL, email_wallet)
                    startActivity(intent)
                    finish()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // here set the number
                        this?.let { it1 ->
                            BiometricHelper.showPrompt("OTP", object : IBiometricListener {
                                override fun onEnrolled() {
                                    clearAllEdittext()
                                    Log.d(Constants.LOG_D_RESPONSE, "onEnrolled")
                                    SharedPrefUtils.isFirstTimeLogin = true
                                    val b = LoginSuccessDialog.newInstance("OTPVALIDATE", "N")
                                    b.show(supportFragmentManager, "Login Success")
                                }

                                override fun onNegativeClick() {
                                    //clearMsisdnOTPCache(true)
                                    Log.d(Constants.LOG_D_RESPONSE, "onNegativeClick")
                                    clearAllEdittext()
                                    val b = LoginSuccessDialog.newInstance("OTPVALIDATE", "N")
                                    b.show(supportFragmentManager, "Login Success")
                                }

                                override fun onSuccess() {
                                    clearAllEdittext()
                                    //clearMsisdnOTPCache(true)
                                    SharedPrefUtils.isFirstTimeLogin = true
                                    val b = BiometricSettingActivity(object :
                                        CheckDialogEventInterface {
                                        override fun onDismiss(screenName: String) {
                                            TODO("Not yet implemented")
                                        }
                                    })
                                    b.show(supportFragmentManager, "Hi")
                                    //context?.let { it2 -> UtilHandler.showToast(it2,"Successfully Setup Biometric") }
                                    Log.d(Constants.LOG_D_RESPONSE, "onSuccess")
                                    //biometricStatusUpdate()
                                }

                                override fun onCancel(errString: String) {
                                    //context?.let { it2 -> UtilHandler.showToast(it2,"User Canceled Event Biometric") }
                                    Log.d(Constants.LOG_D_RESPONSE, "onCancel")
                                    //UtilHandler.showToast(getString(R.string.biometric_un_success) + " " + errString)
                                    //switch_biometric.isChecked = false
                                    //button_setup.isEnabled = false
                                }

                                override fun onAuthError() {
                                    //binding.toggleBiometricBtn.isChecked = false
                                    //SharedPrefUtils.bioMetricUUID = "Disable"
                                    //context?.let { it2 -> UtilHandler.showToast(it2," Biometric Auth Error") }
                                    Log.d(Constants.LOG_D_RESPONSE, "onAuthError")
                                    // switch_biometric.isChecked = false
                                    //button_setup.isEnabled = false

                                }
                            }, it1)
                        }
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this?.let { it1 ->
                        BiometricHelper.showPrompt("OTP", object : IBiometricListener {
                            override fun onEnrolled() {
                                Log.d(Constants.LOG_D_RESPONSE, "onEnrolled")
                                clearAllEdittext()
                                SharedPrefUtils.isFirstTimeLogin = true
                                val b = LoginSuccessDialog.newInstance("OTPVALIDATE", "N")
                                b.show(supportFragmentManager, "Login Success")
                            }

                            override fun onNegativeClick() {
                                clearAllEdittext()
                                Log.d(Constants.LOG_D_RESPONSE, "onNegativeClick")
                                val b = LoginSuccessDialog.newInstance("OTPVALIDATE", "N")
                                b.show(supportFragmentManager, "Login Success")
                            }

                            override fun onSuccess() {
                                clearAllEdittext()
                                //clearMsisdnOTPCache(true)
                                SharedPrefUtils.isFirstTimeLogin = true
                                val b = BiometricSettingActivity(object :
                                    CheckDialogEventInterface {

                                    override fun onDismiss(screenName: String) {
                                        TODO("Not yet implemented")
                                    }
                                })
                                b.show(supportFragmentManager, "Hi")
                                //context?.let { it2 -> UtilHandler.showToast(it2,"Successfully Setup Biometric") }
                                Log.d(Constants.LOG_D_RESPONSE, "onSuccess")
                                //biometricStatusUpdate()
                            }

                            override fun onCancel(errString: String) {
                                //context?.let { it2 -> UtilHandler.showToast(it2,"User Canceled Event Biometric") }
                                Log.d(Constants.LOG_D_RESPONSE, "onCancel")
                                //UtilHandler.showToast(getString(R.string.biometric_un_success) + " " + errString)
                                //switch_biometric.isChecked = false
                                //button_setup.isEnabled = false
                            }

                            override fun onAuthError() {
                                //binding.toggleBiometricBtn.isChecked = false
                                //SharedPrefUtils.bioMetricUUID = "Disable"
                                //context?.let { it2 -> UtilHandler.showToast(it2," Biometric Auth Error") }
                                Log.d(Constants.LOG_D_RESPONSE, "onAuthError")
                                // switch_biometric.isChecked = false
                                //button_setup.isEnabled = false

                            }
                        }, it1)
                    }
                }
            }
        }
    }

    private fun resentOtp(msisdn: String) {
        if (!UtilHandler.canOtpSend(SharedPrefUtils.tempMsisdn)) {
            binding.secondRemainingTxt.visibility = View.VISIBLE
            binding.notGetOtpTxt.visibility = View.GONE
            startTimerForOtp()
        } else {
            try {
                if (UtilHandler.isOnline(this)) {
                    showProgressDialog(getString(R.string.progress_dialog_message))
                    val mfsTransactionInfo = MfsTransactionInfo()
                    mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                    mfsTransactionInfo.serviceType = "0"
                    mfsTransactionInfo.timestamp = System.currentTimeMillis()
                    val mfsSourceInfo = MfsSourceInfo()
                    mfsSourceInfo.channelId = Constants.CHANNEL_ID_22
                    mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_3

                    val mfsCommonServiceRequest =
                        MfsCommonServiceRequest(
                            mfsSourceInfo = mfsSourceInfo,
                            mfsTransactionInfo = mfsTransactionInfo
                        )
                    val mfsRequestInfo =
                        MfsRequestInfo(
                            msisdn = SharedPrefUtils.tempMsisdn,
                            email = getEmail(),
                            length = Constants.OTP_LENGTH_6

                        )

                    val coreServiceRequest = CoreServiceRequest(
                        mfsCommonServiceRequest = mfsCommonServiceRequest,
                        mfsRequestInfo = mfsRequestInfo
                    )

                    if (!BuildConfig.USE_PRODUCTION_URLS)
                        Log.d("Http....tokenManagement/get", Gson().toJson(coreServiceRequest))

                    val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                    retrofit.generateOtp(
                        SharedPrefUtils.tokenKey,
                        Constants.PLATFORM,
                        UtilHandler.getPackageVersion(this),
                        UtilHandler.getScreenDensity(this),
                        coreServiceRequest
                    ).enqueue(
                        object : Callback<CoreResponseModel> {
                            override fun onResponse(
                                call: Call<CoreResponseModel>,
                                response: Response<CoreResponseModel>
                            ) {
                                DialogUtils.hideDialog()
                                if (response.isSuccessful) {
                                    val mCoreResponseModel: CoreResponseModel? = response.body()
                                    if (!BuildConfig.USE_PRODUCTION_URLS)
                                        Log.d(
                                            "Http....tokenManagement/get",
                                            Gson().toJson(mCoreResponseModel)
                                        )
                                    if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status != null) {
                                        if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status.equals(
                                                "Success"
                                            )
                                        ) {
                                            binding.secondRemainingTxt.visibility = View.VISIBLE
                                            binding.notGetOtpTxt.visibility = View.GONE
                                            startTimerForOtp()
                                            UtilHandler.showSnackBar(
                                                getString(R.string.otp_sent),
                                                binding.root
                                            )
                                        } else {
                                            binding.secondRemainingTxt.visibility = View.GONE
                                            binding.notGetOtpTxt.visibility = View.VISIBLE
                                            UtilHandler.showSnackBar(
                                                mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.errorDescription,
                                                binding.root
                                            )
                                        }
                                    } else {
                                        binding.secondRemainingTxt.visibility = View.GONE
                                        binding.notGetOtpTxt.visibility = View.VISIBLE
                                        UtilHandler.showSnackBar(
                                            getString(R.string.something_went_wrong),
                                            binding.root
                                        )
                                    }
                                }

                            }

                            override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                                DialogUtils.hideDialog()
                                Log.d(Constants.LOG_D_KEY, t.message.toString())
                                UtilHandler.showSnackBar(
                                    getString(R.string.something_went_wrong),
                                    binding.root
                                )
                            }

                        }
                    )
                } else {
                    UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
                }
            } catch (e: Exception) {
                hideProgressDialog()
                e.message
            }
        }

    }

    private fun getEmail(): String? {
        return if (SharedPrefUtils.email.equals(""))
            null
        else SharedPrefUtils.email
    }

    private fun clearAllEdittext() {
        binding.edt1.text!!.clear()
        binding.edt2.text!!.clear()
        binding.edt3.text!!.clear()
        binding.edt4.text!!.clear()
        binding.edt5.text!!.clear()
        binding.edt6.text!!.clear()
    }

    private fun updateOtpStatusText() {
        /*For changing otp box border color*/
        val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.wrongotpbackground)
        binding.linearOtp1.background = drawable
        binding.linearOtp2.background = drawable
        binding.linearOtp3.background = drawable
        binding.linearOtp4.background = drawable
        binding.linearOtp5.background = drawable
        binding.linearOtp6.background = drawable
        /*for hiding the keyboard so that user can see whole screen*/
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.textWrongAndRightOtp.visibility = View.VISIBLE
        binding.textWrongAndRightOtp.text = "OTP INVALID"
        binding.textWrongAndRightOtp.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.otp_status_text_background_wrong
            )
        )
        binding.textWrongAndRightOtp.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.otp_status_text_color_wrong
            )
        )

    }

    override fun onBackPressed() {
        super.onBackPressed()
        //val intent = Intent(this, LoginActivity::class.java)
        //startActivity(intent)
    }

    /*Edittext text watcher action on every edittext*/
    private fun edt1PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt2.isEnabled = true
            binding.edt2.requestFocus()
            binding.edt1.isEnabled = false
            binding.edt1.clearFocus()

            val a = s.toString()
            finalResul = finalResul.plus(a)
            //val displayText = a.replace(Regex("\\d"), "*")
            binding.edt1.removeTextChangedListener(textWatcher)
            binding.edt1.setText(a)
            binding.edt1.setSelection(a.length)
            binding.edt1.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt2.isEnabled = false
        }
    }

    private fun edt2PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt3.isEnabled = true
            binding.edt3.requestFocus()
            binding.edt2.clearFocus()
            binding.edt2.isEnabled = false
            val a = s.toString()
            finalResul = finalResul.plus(a)
            //val displayText = a.replace(Regex("\\d"), "*")
            binding.edt2.removeTextChangedListener(textWatcher)
            binding.edt2.setText(a)
            binding.edt2.setSelection(a.length)
            binding.edt2.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt3.isEnabled = false
        }
    }

    private fun edt3PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt4.isEnabled = true
            binding.edt4.requestFocus()
            binding.edt3.clearFocus()
            binding.edt3.isEnabled = false
            val a = s.toString()
            finalResul = finalResul.plus(a)
            //val displayText = a.replace(Regex("\\d"), "*")
            binding.edt3.removeTextChangedListener(textWatcher)
            binding.edt3.setText(a)
            binding.edt3.setSelection(a.length)
            binding.edt3.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt4.isEnabled = false
        }
    }

    private fun edt4PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt5.isEnabled = true
            binding.edt5.requestFocus()
            binding.edt4.clearFocus()
            binding.edt4.isEnabled = false
            val enteredDigit = s.toString()
            finalResul = finalResul.plus(enteredDigit)
            //val displayText = enteredDigit.replace(Regex("\\d"), "*")
            binding.edt4.removeTextChangedListener(textWatcher)
            binding.edt4.setText(enteredDigit)
            binding.edt4.setSelection(enteredDigit.length)
            binding.edt4.addTextChangedListener(textWatcher)
            //checkUpperEdittext(finalResul)
            //Toast.makeText(applicationContext, "Entered digit: $finalResul", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt5.isEnabled = false
        }
    }

    private fun edt5PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt6.isEnabled = true
            binding.edt6.requestFocus()
            binding.edt5.clearFocus()
            binding.edt5.isEnabled = false
            val enteredDigit = s.toString()
            finalResul = finalResul.plus(enteredDigit)
            //val displayText = enteredDigit.replace(Regex("\\d"), "*")
            binding.edt5.removeTextChangedListener(textWatcher)
            binding.edt5.setText(enteredDigit)
            binding.edt5.setSelection(enteredDigit.length)
            binding.edt5.addTextChangedListener(textWatcher)
            //checkUpperEdittext(finalResul)
            //Toast.makeText(applicationContext, "Entered digit: $finalResul", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt6.isEnabled = false
        }
    }

    private fun edt6PinAction(s: Editable) {
        if (s?.length == 1) {
            //binding.edt5.requestFocus()
            val enteredDigit = s.toString()
            finalResul = finalResul.plus(enteredDigit)
            //val displayText = enteredDigit.replace(Regex("\\d"), "*")
            binding.edt6.removeTextChangedListener(textWatcher)
            binding.edt6.setText(enteredDigit)
            binding.edt6.setSelection(enteredDigit.length)
            binding.edt6.addTextChangedListener(textWatcher)
            checkAllEdittextValue(finalResul)
            //Toast.makeText(applicationContext, "Entered digit: $finalResul", Toast.LENGTH_SHORT).show()
        }
    }

    /* key Listener for all edittext for deleting text from edittext*/
    private fun edt2KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt2.text)) {
                binding.edt2.text?.clear()

            } else {
                binding.edt1.text?.clear()
                binding.edt1.isEnabled = true
                binding.edt1.requestFocus()
                binding.edt2.clearFocus()
                binding.edt2.isEnabled = false
            }
            binding.edt3.isEnabled = false
            binding.edt4.isEnabled = false
            binding.edt5.isEnabled = false
            binding.edt6.isEnabled = false
        } else {
            binding.edt1.isEnabled = false
        }
    }

    private fun edt3KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt3.text)) {
                binding.edt3.text?.clear()

            } else {
                binding.edt2.text?.clear()
                binding.edt2.isEnabled = true
                binding.edt2.requestFocus()
                binding.edt3.clearFocus()
                binding.edt3.isEnabled = false
            }
            binding.edt1.isEnabled = false
            binding.edt4.isEnabled = false
            binding.edt5.isEnabled = false
            binding.edt6.isEnabled = false
        } else {
            binding.edt2.isEnabled = false
        }
    }

    private fun edt4KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt4.text)) {
                binding.edt4.text?.clear()

            } else {
                binding.edt3.text?.clear()
                binding.edt3.isEnabled = true
                binding.edt3.requestFocus()
                binding.edt4.clearFocus()
                binding.edt4.isEnabled = false
            }
            binding.edt1.isEnabled = false
            binding.edt2.isEnabled = false
            binding.edt5.isEnabled = false
            binding.edt6.isEnabled = false

        } else {
            binding.edt3.isEnabled = false
        }
    }

    private fun edt5KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt5.text)) {
                binding.edt5.text?.clear()

            } else {
                binding.edt4.text?.clear()
                binding.edt4.isEnabled = true
                binding.edt4.requestFocus()
                binding.edt5.clearFocus()
                binding.edt5.isEnabled = false
            }

            //binding.edt5.text?.clear()
            //binding.edt3Pin.requestFocus()
            //binding.edt4.isEnabled = true
            //binding.edt4.requestFocus()
            binding.edt1.isEnabled = false
            binding.edt2.isEnabled = false
            binding.edt3.isEnabled = false
            binding.edt6.isEnabled = false

        } else {
            binding.edt4.isEnabled = false
        }
    }

    private fun edt6KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt6.text)) {
                binding.edt6.text?.clear()
            } else {
                binding.edt5.text?.clear()
                binding.edt5.isEnabled = true
                binding.edt5.requestFocus()
                binding.edt6.clearFocus()
                binding.edt6.isEnabled = false
            }
            binding.edt1.isEnabled = false
            binding.edt2.isEnabled = false
            binding.edt3.isEnabled = false
            binding.edt4.isEnabled = false

        } else {
            binding.edt5.isEnabled = false
        }
    }

    private fun checkAllEdittextValue(otp: String) {
        if (binding.edt4.length() == 1) {
            if (binding.edt1.text!!.isNotEmpty() && binding.edt2.text!!.isNotEmpty() && binding.edt3.text!!.isNotEmpty() && binding.edt4.text!!.isNotEmpty() && binding.edt5.text!!.isNotEmpty() && binding.edt6.text!!.isNotEmpty()) {
                otpString =
                    binding.edt1.text.toString() + binding.edt2.text.toString() + binding.edt3.text.toString() + binding.edt4.text.toString() + binding.edt5.text.toString() + binding.edt6.text.toString()
                finalResul = ""
                // here otp by pass
                val defaultAccounts = SharedPrefUtils.DefaultAccountArray?.split(",")
                if (defaultAccounts != null && SharedPrefUtils.tempMsisdn != null) {
                    if (defaultAccounts?.contains(SharedPrefUtils.tempMsisdn.toString()) == true) {
                        if (otpString == "197900")
                            processToNextScreen()
                        else
                            updateOtpStatusText()
                    } else {
                        verifyOtp(otpString)
                    }
                } else {
                    verifyOtp(otpString)
                }
            } else {
                Toast.makeText(applicationContext, "please fill valid otp", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDismiss(screenName: String) {
        //clearMsisdnOTPCache(true)
        if (screenName == Constants.BIOMETRIC) {
            val b = LoginSuccessDialog(object :
                CheckDialogEventInterface {
                override fun onDismiss(screenName: String) {

                }
            })
            b.show(supportFragmentManager, "Hi")
        } else if (screenName == Constants.LOGIN_SUCCESS) {
            val intent = Intent(this@OtpValidateActivity, DashboardActivity::class.java)
            startActivity(intent)
        }

    }

    inner class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            val seconds = millisUntilFinished / 1000
            Log.e("sec", "sec$seconds")
            binding.secondRemainingTxt.text =
                String.format("%02d", seconds / 60) + ":" + String.format(
                    "%02d",
                    seconds % 60
                ) + "sec remaining"
            //tv_minute.text = String.format("%02d", seconds / 60)
        }

        override fun onFinish() {
            val msisdnOtpData: String? = SharedPrefUtils.MSISDN_OTP
            val jsonObject = JSONObject(msisdnOtpData)
            if (jsonObject.has(SharedPrefUtils.tempMsisdn)) {
                val value = jsonObject.getString(SharedPrefUtils.tempMsisdn)
                val s = value.split("_".toRegex()).toTypedArray()
                val lastOtpSend = s[0].toLong()
                val step = s[1]
                if (step.equals("3", ignoreCase = true)) {
                    binding.notGetOtpTxt.visibility = View.GONE
                    DialogUtils.showCommonDialog(
                        1,
                        false,
                        this@OtpValidateActivity,
                        getString(R.string.retry_later_limit_exceed)
                    ) {
                        if (SharedPrefUtils.isFirstTimeLogin) {
                            startActivity(
                                Intent(
                                    this@OtpValidateActivity,
                                    DashboardActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                            finish()
                        } else {
                            startActivity(
                                Intent(
                                    this@OtpValidateActivity,
                                    LoginActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                            finish()
                        }
                    }
                } else {
                    binding.secondRemainingTxt.visibility = View.GONE
                    binding.notGetOtpTxt.visibility = View.VISIBLE
                }
            } else {
                binding.secondRemainingTxt.visibility = View.GONE
                binding.notGetOtpTxt.visibility = View.VISIBLE
            }
        }
    }

    fun clearMsisdnOTPCache(boolean: Boolean) {
        UtilHandler.removeMSISDNfromOtpCache(SharedPrefUtils.tempMsisdn)
        if (boolean)
            finish()
    }
}