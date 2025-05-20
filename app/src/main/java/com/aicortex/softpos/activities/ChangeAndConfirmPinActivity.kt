package com.aicortex.softpos.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.api.CheckDialogEventInterface
import com.aicortex.softpos.databinding.ActivityChangeAndConfirmPinBinding
import com.aicortex.softpos.helper.BiometricHelper
import com.aicortex.softpos.models.core.request.*
import com.aicortex.softpos.models.core.response.CoreResponseModel
import com.aicortex.softpos.network.ServiceBuilder
import com.aicortex.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangeAndConfirmPinActivity : BaseActivity(), OnClickListener, CheckDialogEventInterface {
    lateinit var binding: ActivityChangeAndConfirmPinBinding
    var verify_pin_from_wallet: String? = null
    var otpPinString: String = ""
    var otpConfirmPinString: String = ""
    var finalResul: String = ""
    var stringPin: String? = null
    var editTextValueFromOtpValidateActivity: String? = null
    var typeEmailOrWalletFromOtpValidateActivity: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_and_confirm_pin)

        editTextValueFromOtpValidateActivity = intent.getStringExtra(Constants.EDITTEXTVALUE)
        typeEmailOrWalletFromOtpValidateActivity =
            intent.getStringExtra(Constants.TYPEWALLETOREMAIL)
        updateUI()
        verify_pin_from_wallet = intent.getStringExtra(Constants.VERIFY_PIN_FROM_WALLET)
        setClickListeners()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (intent.getStringExtra(Constants.REQUEST_FROM) != null) {
            if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.VERIFY_PIN_FROM_WALLET) {
                val intent = Intent(this, OtpValidateActivity::class.java)
                intent.putExtra(Constants.REQUEST_FROM, Constants.FROM_LOGIN)
                intent.putExtra(Constants.EDITTEXTVALUE, editTextValueFromOtpValidateActivity)
                intent.putExtra(
                    Constants.TYPEWALLETOREMAIL,
                    typeEmailOrWalletFromOtpValidateActivity
                )
                startActivity(intent)
            } else if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_FORGET_PIN) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_MOVE_FUND_WALLET) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        } else {
            val intent = Intent(this, OtpValidateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setClickListeners() {
        binding.proceedPinTxt.setOnClickListener(this)
        // binding.textForgetPinOnPinScreen.setOnClickListener(this)
        binding.backArrowBtn.setOnClickListener(this)

        binding.edt2Pin.isEnabled = false
        binding.edt3Pin.isEnabled = false
        binding.edt4Pin.isEnabled = false

        binding.edt1Pin.addTextChangedListener(textWatcher)
        binding.edt2Pin.addTextChangedListener(textWatcher)
        binding.edt3Pin.addTextChangedListener(textWatcher)
        binding.edt4Pin.addTextChangedListener(textWatcher)

        binding.edt2Pin.setOnKeyListener(keyListener)
        binding.edt3Pin.setOnKeyListener(keyListener)
        binding.edt4Pin.setOnKeyListener(keyListener)


    }

    fun updateUI() {
        binding.edt1Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.edt2Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.edt3Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.edt4Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        if (intent.getStringExtra(Constants.REQUEST_FROM) != null) {
            if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.VERIFY_PIN_FROM_WALLET) {
                binding.proceedPinTxt.visibility = View.GONE
                binding.linearLoader22FullEmailPin.visibility = View.VISIBLE
                binding.linearForgetPinOnPinScreen.visibility = View.VISIBLE
            } else if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_FORGET_PIN) {
                binding.textPinScreenHeader.text = getString(R.string.pin_creation)
                binding.proceedPinTxt.visibility = View.VISIBLE
                binding.linearLoader22FullEmailPin.visibility = View.GONE
                binding.proceedPinTxt.isClickable = false
                binding.proceedPinTxt.isEnabled = false
                binding.proceedPinTxt.alpha = 0.5f
                binding.linearForgetPinOnPinScreen.visibility = View.GONE
            } else {
                binding.proceedPinTxt.visibility = View.GONE
                binding.linearLoader22FullEmailPin.visibility = View.GONE
                binding.linearForgetPinOnPinScreen.visibility = View.GONE
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (binding.edt1Pin.text?.isNotEmpty() == true && binding.edt2Pin.text?.isNotEmpty() == true && binding.edt3Pin.text?.isNotEmpty() == true && binding.edt4Pin.text?.isNotEmpty() == true) {
                binding.proceedPinTxt.isEnabled = true
                binding.proceedPinTxt.isClickable = true
                binding.proceedPinTxt.alpha = 1f
                stringPin =
                    binding.edt1Pin.text.toString() + binding.edt2Pin.text.toString() + binding.edt3Pin.text.toString() + binding.edt4Pin.text.toString()
                //appendPIN(stringPin!!)
            } else {
                binding.proceedPinTxt.isEnabled = false
                binding.proceedPinTxt.isClickable = false
                binding.proceedPinTxt.alpha = 0.5f
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (s.length == 1) {
                // Move focus to the next EditText when a digit is entered
                when (s.hashCode()) {
                    binding.edt1Pin.text.hashCode() -> edt1PinAction(s)
                    binding.edt2Pin.text.hashCode() -> edt2PinAction(s)
                    binding.edt3Pin.text.hashCode() -> edt3PinAction(s)
                    binding.edt4Pin.text.hashCode() -> edt4PinAction(s)
                }
            }
        }
    }

    private val keyListener = View.OnKeyListener { view, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            // Move focus to the previous EditText when the Backspace key is pressed
            when (view) {
                is EditText -> {
                    when (view) {
                        binding.edt4Pin -> {
                            edt4KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt3Pin -> {
                            edt3KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt2Pin -> {
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

    /*Edittext text watcher action on every edittext*/
    private fun edt1PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt2Pin.isEnabled = true
            binding.edt2Pin.requestFocus()
            binding.edt1Pin.isEnabled = false
            val a = s.toString()
            binding.edt1Pin.removeTextChangedListener(textWatcher)
            binding.edt1Pin.setText(a)
            binding.edt1Pin.setSelection(a.length)
            binding.edt1Pin.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt2Pin.isEnabled = false
        }
    }

    private fun edt2PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt3Pin.isEnabled = true
            binding.edt3Pin.requestFocus()
            binding.edt2Pin.isEnabled = false
            val a = s.toString()
            binding.edt2Pin.removeTextChangedListener(textWatcher)
            binding.edt2Pin.setText(a)
            binding.edt2Pin.setSelection(a.length)
            binding.edt2Pin.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt3Pin.isEnabled = false
        }
    }

    private fun edt3PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt4Pin.isEnabled = true
            binding.edt4Pin.requestFocus()
            binding.edt3Pin.isEnabled = false
            val a = s.toString()
            binding.edt3Pin.removeTextChangedListener(textWatcher)
            binding.edt3Pin.setText(a)
            binding.edt3Pin.setSelection(a.length)
            binding.edt3Pin.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt4Pin.isEnabled = false
        }
    }

    private fun edt4PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt3Pin.requestFocus()
            val enteredDigit = s.toString()
            binding.edt4Pin.removeTextChangedListener(textWatcher)
            binding.edt4Pin.setText(enteredDigit)
            binding.edt4Pin.setSelection(enteredDigit.length)
            binding.edt4Pin.addTextChangedListener(textWatcher)
            stringPin?.let { verifyEmailAndmPin(SharedPrefUtils.tempMsisdn.toString(), it) }
            //Toast.makeText(applicationContext, "Entered digit: $finalResul", Toast.LENGTH_SHORT).show()
        }
    }

    /* key Listener for all edittext for deleting text from edittext*/
    private fun edt2KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt2Pin.text)){
                binding.edt2Pin.text?.clear()
            }else{
                binding.edt1Pin.text?.clear()
                binding.edt1Pin.isEnabled = true
                binding.edt1Pin.requestFocus()
                binding.edt2Pin.clearFocus()
                binding.edt2Pin.isEnabled = false
            }
            binding.edt3Pin.isEnabled = false
            binding.edt4Pin.isEnabled = false
            //deletePIN()
        } else {
            binding.edt1Pin.isEnabled = false
        }
    }

    private fun edt3KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt3Pin.text)){
                binding.edt3Pin.text?.clear()
            }else{
                binding.edt2Pin.text?.clear()
                binding.edt2Pin.isEnabled = true
                binding.edt2Pin.requestFocus()
                binding.edt3Pin.clearFocus()
                binding.edt3Pin.isEnabled = false
            }
            binding.edt1Pin.isEnabled = false
            binding.edt4Pin.isEnabled = false
            //  deletePIN()
        } else {
            binding.edt2Pin.isEnabled = false
        }
    }

    private fun edt4KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt4Pin.text)){
                binding.edt4Pin.text?.clear()
            }else{
                binding.edt3Pin.text?.clear()
                binding.edt3Pin.isEnabled = true
                binding.edt3Pin.requestFocus()
                binding.edt4Pin.clearFocus()
                binding.edt4Pin.isEnabled = false
            }
            binding.edt1Pin.isEnabled = false
            binding.edt2Pin.isEnabled = false
            //   deletePIN()

        } else {
            binding.edt3Pin.isEnabled = false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.proceedPinTxt -> proceedBtnUpdate()
            //R.id.textForgetPinOnPinScreen -> proceedToResetPinActivity()
            R.id.backArrowBtn -> onBackPressed()
        }
    }

    fun proceedBtnUpdate() {
        if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_FORGET_PIN) {
            when (binding.proceedPinTxt.text) {
                getString(R.string.confirm) -> {
                    binding.proceedPinTxt.text = getString(R.string.confirm_pin)
                }
                getString(R.string.confirm_pin) -> {
                    binding.textPinScreenHeader.text = getString(R.string.confirm_pin)
                    binding.proceedPinTxt.text = getString(R.string.proceed)
                }
                else -> {
                    /* val b = PinSuccessDialog.newInstance("BIOMETRIC")
                        b.show(supportFragmentManager, "Login Success")*/
                    //changePin()
                }
            }


        }
    }

    private fun proceedToNextScreen() {
        this?.let { BiometricHelper.initializeBiometricScannerLogin(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this?.let { BiometricHelper.initializeBiometricScanner(it) }
        }
        if (intent.getStringExtra(Constants.REQUEST_FROM) != null) {
            if (intent.getStringExtra(Constants.REQUEST_FROM)
                    .equals(Constants.FROM_MOVE_FUND_WALLET)
            ) {
                moveFundWallet(intent.getIntExtra(Constants.BILL_AMOUNT, 0))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.let { it1 ->
                        BiometricHelper.showPrompt("OTP", object : IBiometricListener {
                            override fun onEnrolled() {
                                Log.d(Constants.LOG_D_RESPONSE, "onEnrolled")
                                SharedPrefUtils.bioMetricUUID = "Disable"
                                SharedPrefUtils.isBiometricEnable = false
                                SharedPrefUtils.isFirstTimeLogin = true
                                val b = LoginSuccessDialog.newInstance("OTPVALIDATE", "N")
                                b.show(supportFragmentManager, "Login Success")
                            }

                            override fun onNegativeClick() {
                                Log.d(Constants.LOG_D_RESPONSE, "onNegativeClick")
                                SharedPrefUtils.bioMetricUUID = "Disable"
                                SharedPrefUtils.isBiometricEnable = false
                                SharedPrefUtils.isFirstTimeLogin = true
                                val b = LoginSuccessDialog.newInstance("OTPVALIDATE", "N")
                                b.show(supportFragmentManager, "Login Success")
                            }

                            override fun onSuccess() {
                                //SharedPrefUtils.bioMetricUUID = "Enable"
                                //SharedPrefUtils.isBiometricEnable = true
                                SharedPrefUtils.isFirstTimeLogin = true
                                val b =
                                    BiometricSettingActivity(object : CheckDialogEventInterface {

                                        override fun onDismiss(screenName: String) {
                                            //TODO("Not yet implemented")
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
                } else {
                    SharedPrefUtils.bioMetricUUID = "Disable"
                    SharedPrefUtils.isBiometricEnable = false
                    SharedPrefUtils.isFirstTimeLogin = true
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    startActivity(intent)
                }
            }

        } else {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun verifyEmailAndmPin(msisdn: String, mPin: String) {

        try {
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
                //val mfsRequestInfo = MfsRequestInfo("", UtilHandler.enCryptPassword(mPin), userEmail, SharedPrefUtils.entityType)
                val mfsRequestInfo = MfsRequestInfo(
                    mToken = "",
                    mAccessCode = UtilHandler.enCryptPassword(mPin),
                    msisdn = msisdn,
                    entityType = SharedPrefUtils.entityType
                )

                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
               if(!BuildConfig.USE_PRODUCTION_URLS)
                Log.d("Http...verifyMpin", Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.verifyEmailAndmPin(SharedPrefUtils.tokenKey,Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this),UtilHandler.getScreenDensity(this), coreServiceRequest).enqueue(
                    object : Callback<CoreResponseModel> {
                        override fun onResponse(
                            call: Call<CoreResponseModel>,
                            response: Response<CoreResponseModel>
                        ) {
                            DialogUtils.hideDialog()
                            if (response.isSuccessful) {
                                val mCoreResponseModel: CoreResponseModel? = response.body()
                                if(!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http...verifyMpin", Gson().toJson(mCoreResponseModel))

                                if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status.equals(
                                        "Success"
                                    )
                                ) {
                                    SharedPrefUtils.tokenKey = Constants.BEARER + "" + mCoreResponseModel?.mfsResponseInfo?.token
                                    SharedPrefUtils.msisdn=SharedPrefUtils.tempMsisdn  //final number user has logged in with tks
                                    proceedToNextScreen()
                                } else {
                                    // UtilHandler.showSnackBar(mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.errorDescription, binding.root)
                                    UtilHandler.showToast(
                                        applicationContext,
                                        mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.errorDescription
                                    )
                                }

                            }

                        }

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            DialogUtils.hideDialog()
                            Log.d(Constants.LOG_D_KEY, t.message.toString().toString())
                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                binding.root
                            )
                        }

                    }
                )
            } else {
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
                UtilHandler.showLongToast(getString(R.string.something_went_wrong))
            }
        } catch (e: Exception) {
            e.message
        }
    }

    override fun onDismiss(screenName: String) {
        if (screenName == Constants.BIOMETRIC) {
            val b = LoginSuccessDialog(object :
                CheckDialogEventInterface {
                override fun onDismiss(screenName: String) {

                }
            })
            b.show(supportFragmentManager, "Hi")
        } else if (screenName == Constants.LOGIN_SUCCESS) {
            val intent = Intent(this@ChangeAndConfirmPinActivity, DashboardActivity::class.java)
            startActivity(intent)
        }

    }

    private fun moveFundWallet(amount: Int) {
        try {
            if (UtilHandler.isOnline(this)) {
                showProgressDialog(getString(R.string.progress_dialog_message))
                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = Constants.MOVE_FUND_WALLET_SERVICE_TYPE
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = Constants.CHANNEL_ID_23
                mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_1
                var str: String? = SharedPrefUtils.email
                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                val mfsRequestInfo =
                    MfsRequestInfo(
                        fromEntityId =  SharedPrefUtils.entityId,
                        amount = amount,
                        walletTypeToDeduct = Constants.WALLET_TYPE_TO_DEDUCT_CREDIT,
                        toEntityId = SharedPrefUtils.parentID, //"16000000083"
                        walletTypeToCredit = Constants.WALLET_TYPE_TO_DEDUCT_CREDIT
                    )
                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )

                Log.d(Constants.LOG_D_REQUEST, Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.moveFundWallet(
                    SharedPrefUtils.tokenKey,Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this),UtilHandler.getScreenDensity(this),
                    coreServiceRequest
                )
                    .enqueue(
                        object : Callback<CoreResponseModel> {
                            override fun onResponse(
                                call: Call<CoreResponseModel>,
                                response: Response<CoreResponseModel>
                            ) {
                                DialogUtils.hideDialog()
                                if (response.isSuccessful) {
                                    //getFloorLimit()
                                    val mCoreResponseModel: CoreResponseModel? =
                                        response.body()

                                    if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status != null) {
                                        if (mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.status.equals(
                                                "Success"
                                            )
                                        ) {
                                            val intent = Intent(
                                                this@ChangeAndConfirmPinActivity,
                                                SuccessActivity::class.java
                                            )
                                            intent.putExtra(
                                                Constants.REQUEST_FROM,Constants.FROM_MOVE_FUND_WALLET)
                                            intent.putExtra(
                                                Constants.TRANSACTIONID,
                                                mCoreResponseModel.mfsResponseInfo?.transactionId
                                            )
                                            intent.putExtra(
                                                Constants.TRANSACTION_STATUS,
                                                mCoreResponseModel.mfsResponseInfo?.transactionStatus
                                            )
                                            intent.putExtra(
                                                Constants.BILL_AMOUNT,
                                                mCoreResponseModel.mfsResponseInfo?.transactionAmount
                                            )
                                            intent.putExtra(
                                                Constants.TRANSACTION_DATE,
                                                mCoreResponseModel.mfsResponseInfo?.transactionDate
                                            )
                                            intent.putExtra(
                                                Constants.REMARK,
                                                mCoreResponseModel.mfsResponseInfo?.transactionRemarks
                                            )
                                            intent.putExtra(
                                                Constants.TRANSACTION_SENDER_NAME,
                                                mCoreResponseModel.mfsResponseInfo?.senderName
                                            )
                                            intent.putExtra(
                                                Constants.TRANSACTION_SENDER_NUMBER,
                                                mCoreResponseModel.mfsResponseInfo?.senderMsisdn
                                            )
                                            intent.putExtra(
                                                Constants.TRANSACTION_RECEIVER_NAME,
                                                mCoreResponseModel.mfsResponseInfo?.receiverName
                                            )
                                            intent.putExtra(
                                                Constants.TRANSACTION_RECEIVER_NUMBER,
                                                mCoreResponseModel.mfsResponseInfo?.receiverMsisdn
                                            )
                                            startActivity(intent)
                                            //proceedToNext()

                                        } else {
                                            finish()
                                            UtilHandler.showSnackBar(mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.errorDescription,binding.root)
                                        }
                                    } else {
                                        UtilHandler.showLongToast(getString(R.string.something_went_wrong))
                                    }


                                } else {
                                    UtilHandler.showLongToast(getString(R.string.something_went_wrong))
                                }

                            }

                            override fun onFailure(
                                call: Call<CoreResponseModel>,
                                t: Throwable
                            ) {
                                DialogUtils.hideDialog()
                                Log.d(Constants.LOG_D_KEY, t.message.toString().toString())
                            }

                        }
                    )
            } else {
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
            }
        } catch (e: Exception) {
            e.message
            DialogUtils.hideDialog()
        }

    }

}