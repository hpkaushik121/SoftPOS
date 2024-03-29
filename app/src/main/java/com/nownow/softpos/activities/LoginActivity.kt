package com.nownow.softpos.activities

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.*
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.nownow.softpos.BuildConfig
import com.nownow.softpos.R
import com.nownow.softpos.api.ApiCalls
import com.nownow.softpos.databinding.ActivityLoginBinding
import com.nownow.softpos.helper.BiometricHelper
import com.nownow.softpos.models.core.request.*
import com.nownow.softpos.models.core.response.CoreResponseModel
import com.nownow.softpos.network.ServiceBuilder
import com.nownow.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : BaseActivity(), OnClickListener {

    lateinit var binding: ActivityLoginBinding
    var typeOfWalletOrEmail: String = "eMail Address"
    var isNewUser:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //android.os.Debug.waitForDebugger()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        getToken()
        this?.let { BiometricHelper.initializeBiometricScannerLogin(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this?.let { BiometricHelper.initializeBiometricScanner(it) }
        }
        updateUI()
        setclickListeners()
        binding.imgBiometric.setOnClickListener(View.OnClickListener {
            openBiometric()
        })
        /* binding.textForgetPinFirstTime.setOnClickListener {
             val b = ResetPINActivity.newInstance("OTPVALIDATE")
             b.show(supportFragmentManager, "Login Success")
         }
         binding.textForgetPin.setOnClickListener(View.OnClickListener {
             val b = ResetPINActivity.newInstance(Constants.FROM_LOGIN)
             b.show(supportFragmentManager, "Login Success")
         })*/
    }

    override fun onResume() {
        super.onResume()
        isNewUser=false
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val textLength = s?.length ?: 0
            val filter = InputFilter { source, start, end, _, _, _ ->
                for (i in start until end) {
                    if (Character.isWhitespace(source[i]) || source[i] == '-') {
                        return@InputFilter ""
                    }
                }
                null
            }

            /*First Time Login SetUp*/
            binding.editInputWalletOrEmail.filters = arrayOf(filter)
            binding.editInputPin1.filters = arrayOf(filter)
            val hasTextFirst1 = binding.editInputWalletOrEmail.text.isNotEmpty()
            val hasTextFirst2 = binding.editInputPin1.text.isNotEmpty()
            if (binding.btnChangeEmailWalletID.text.equals("Login with Email Address")) {
                /*if (hasTextFirst1) {*/
                if (binding.editInputWalletOrEmail.text.length >= 10) {
                    upDateErrorUI(false, "", "")
                    binding.btnProceed.isEnabled = true
                    binding.btnProceed.isClickable = true
                    binding.btnProceed.alpha = 1f
                    binding.textTermAndConditions.alpha = 1f
                    binding.textTermAndConditions.isClickable = true
                } else {
                    binding.btnProceed.isEnabled = false
                    binding.btnProceed.isClickable = false
                    binding.btnProceed.alpha = 0.5f
                    binding.textTermAndConditions.alpha = 0.5f
                    binding.textTermAndConditions.isClickable = true
                }

                /*  } else {
                      binding.btnProceed.isEnabled = false
                      binding.btnProceed.isClickable = false
                      binding.btnProceed.alpha = 0.5f
                      binding.textByProceeding.alpha = 0.5f
                      binding.textByProceeding.isClickable = false
                      //binding.textTermsOfUse.alpha - 0.5f
                  }*/
            } else {
                if (hasTextFirst2) {
                    binding.editInputPin1.letterSpacing = 0.5f
                } else {
                    binding.editInputPin1.letterSpacing = 0f
                }
                if (hasTextFirst1 && hasTextFirst2) {
                    binding.btnProceed.isEnabled = true
                    binding.btnProceed.isClickable = true
                    binding.btnProceed.alpha = 1f
                    binding.textTermAndConditions.alpha = 1f
                    binding.textTermAndConditions.isClickable = true
                    //binding.textTermsOfUse.alpha - 1f

                } else {
                    binding.btnProceed.isEnabled = false
                    binding.btnProceed.isClickable = false
                    binding.btnProceed.alpha = 0.5f
                    binding.textTermAndConditions.alpha = 0.5f
                    binding.textTermAndConditions.isClickable = true
                    //binding.textTermsOfUse.alpha - 0.5f
                }
            }

            /*Second Time Login Setup*/

            binding.editInputSecondEmailAndWallet.filters = arrayOf(filter)
            binding.editInputPin2.filters = arrayOf(filter)
            val hasText1 = binding.editInputSecondEmailAndWallet.text.isNotEmpty()
            val hasText2 = binding.editInputPin2.text.isNotEmpty()
            if (hasText2) {
                binding.editInputPin2.letterSpacing = 0.5f
            } else {
                binding.editInputPin2.letterSpacing = 0f
            }
            if (hasText1 && hasText2) {
                upDateErrorUI(false, "", "")
                binding.btnSecondProceed.isEnabled = true
                binding.btnSecondProceed.isClickable = true
                binding.btnSecondProceed.alpha = 1f

            } else {
                binding.btnSecondProceed.isEnabled = false
                binding.btnSecondProceed.isClickable = false
                binding.btnSecondProceed.alpha = 0.5f
            }
        }

        override fun afterTextChanged(s: Editable?) {
            /*First Time Login SetUp*/
            if (binding.editInputPin1.length() > 4) {
                val truncated = s?.subSequence(0, 4)
                binding.editInputPin1.setText(truncated)
                truncated?.length?.let { binding.editInputPin1.setSelection(it) }
            }
            if (binding.btnChangeEmailWalletID.text.equals("Login with Email Address")) {
                if (s?.length ?: 0 > 11) {
                    binding.editInputWalletOrEmail.setText(s?.subSequence(0, 11))
                    binding.editInputWalletOrEmail.setSelection(11)
                }
            }
            /*Second Time Login SetUp*/
            if (binding.editInputPin2.length() > 4) {
                val truncated = s?.subSequence(0, 4)
                binding.editInputPin2.setText(truncated)
                truncated?.length?.let { binding.editInputPin2.setSelection(it) }
            }

            if (binding.textUseWalletEmailUnderline.text.equals("Use Email")) {
                if (s?.length ?: 0 > 11) {
                    binding.editInputSecondEmailAndWallet.setText(s?.subSequence(0, 11))
                    binding.editInputSecondEmailAndWallet.setSelection(11)
                }
            }
        }
    }

    private fun setclickListeners() {
        binding.btnProceed.setOnClickListener(this)
        binding.btnChangeEmailWalletID.setOnClickListener(this)
        binding.btnSecondProceed.setOnClickListener(this)
        binding.textUseWalletEmailUnderline.setOnClickListener(this)
        binding.textTermAndConditions.setOnClickListener(this)
        /*First Time Login Both edittext action */
        binding.editInputWalletOrEmail.addTextChangedListener(textWatcher)
        binding.editInputPin1.addTextChangedListener(textWatcher)
        /*Second Time Login Both edittext action */
        binding.editInputSecondEmailAndWallet.addTextChangedListener(textWatcher)
        binding.editInputPin2.addTextChangedListener(textWatcher)
    }

    fun updateUI() {
        upDateErrorUI(false, "", "")
        binding.editInputPin1.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.editInputPin2.transformationMethod = UtilHandler.CustomPasswordSymbol()
        //binding.editInputPin1.addTextChangedListener(textWatcher)
        binding.textHeadingForLoginFirstTime.text = getString(R.string.please_login_with_email)
        if (!SharedPrefUtils.isFirstTimeLogin && !SharedPrefUtils.loggedOut) {
            binding.constraintFirstLogin.visibility = View.VISIBLE
            binding.constraintSecondLogin.visibility = View.GONE
        } else {
            binding.constraintFirstLogin.visibility = View.GONE
            binding.constraintSecondLogin.visibility = View.VISIBLE
            when (SharedPrefUtils.bioMetricUUID) {
                "Disable" -> {
                    binding.imgBiometric.visibility = View.GONE
                }
                "Enable" -> {
                    binding.imgBiometric.visibility = View.VISIBLE
                }
                else -> {
                    binding.imgBiometric.visibility = View.GONE
                }
            }
        }
        val text =
            "<font color=\"#495260\">By proceeding, you are agreeing to our </font> <font color=\"#ffa400\">Terms of Use.</font>"
        binding.textTermAndConditions.text = Html.fromHtml(text)
        //val forgotPinTxt = findViewById<TextView>(R.id.forgotPinTxt)
        binding.btnProceed.isEnabled = false
        binding.btnProceed.isClickable = false
        binding.btnProceed.alpha = 0.5f
        binding.btnProceed.alpha = 0.5f
        binding.textTermAndConditions.alpha = 0.5f
        binding.textTermAndConditions.isClickable = true
        //binding.textTermsOfUse.alpha - 0.5f
        binding.btnSecondProceed.isEnabled = false
        binding.btnSecondProceed.isClickable = false
        binding.btnSecondProceed.alpha = 0.5f
        binding.linearLoader12EmaiPin.visibility = View.VISIBLE
        binding.linearLoader13Wallet.visibility = View.GONE
    }

    private fun openBiometric() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this?.let { it1 ->
                BiometricHelper.showPrompt("", object : IBiometricListener {
                    override fun onEnrolled() {
                        Log.d(Constants.LOG_D_RESPONSE, "onEnrolled")
                        /* val intent = Intent(this@LoginActivity, LoginSuccessDialog::class.java)
                         intent.putExtra(Constants.REQUEST_FROM, "OTPVALIDATE")
                         startActivity(intent)*/
                        //UtilHandler.showToast(getString(R.string.setup_biometric_not_enrolled))
                    }

                    override fun onNegativeClick() {
                        Log.d(Constants.LOG_D_RESPONSE, "onNegativeClick")
                        val intent =
                            Intent(this@LoginActivity, LoginSuccessDialog::class.java)
                        intent.putExtra(Constants.REQUEST_FROM, Constants.FROM_LOGIN)
                        startActivity(intent)
                        //context?.let { it2 -> UtilHandler.showToast(it2,"User Canceled Biometric") }
                        //UtilHandler.showToast(getString(R.string.biometric_un_success))
                    }

                    override fun onSuccess() {
                        SharedPrefUtils.loggedOut=false
                      /*  val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.putExtra(Constants.REQUEST_FROM, Constants.ALREADYLOGIN)
                        startActivity(intent)*/
                        Log.d(Constants.LOG_D_RESPONSE, "onSuccess")
                       if(SharedPrefUtils.msisdn!=null && SharedPrefUtils.msisdn !="")
                        getUserInfo2(SharedPrefUtils.msisdn.toString(), "", checkMsisdn = true, fromBioMetric = true)
                        else
                            UtilHandler.showSnackBar("Can not Login!",binding.root)
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

    private val isValid: Boolean
        get() {
            if (binding.editInputWalletOrEmail.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding.editInputWalletOrEmail.error = getString(R.string.empty_wallet_field)
                binding.mainScrollView.scrollTo(0, 0)
                //scroll_view.scrollTo(0, 0)
                return false
            }
            if (binding.editInputWalletOrEmail.text.toString().trim().length < 3) {
                binding.editInputWalletOrEmail.error = getString(R.string.empty_wallet_field)
                return false
            }
            if (binding.textHintWalletOrEmail.text.equals("Wallet ID")) {
                if (binding.editInputWalletOrEmail.text.toString().trim().length <= 9) {
                    binding.editInputWalletOrEmail.error = getString(R.string.invalid_wallet_id)
                    return false
                } else {
                    return true
                }
            }

            if (binding.editInputWalletOrEmail.text.toString().contains(" ")) {
                binding.editInputWalletOrEmail.error = getString(R.string.empty_wallet_field)
                return false
            }
            return true
        }

    private fun getToken() {
        try {
            if (UtilHandler.isOnline(this)) {
                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                //val authPayload="apiClient:apiClientSecret"
                //val data = authPayload.toByteArray()
                //val base64 = Base64.encodeToString(data, Base64.NO_WRAP)
                val base64 = getBasicToken()
                showProgressDialog(getString(R.string.progress_dialog_message))


                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = Constants.SERVICE_TYPE_ZERO
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = "22"
                mfsSourceInfo.surroundSystem = "1"

                val mfsCommonServiceRequest = MfsCommonServiceRequest(
                    mfsSourceInfo = mfsSourceInfo,
                    mfsTransactionInfo = mfsTransactionInfo
                )
                val coreServiceRequest =
                    CoreServiceRequest(mfsCommonServiceRequest = mfsCommonServiceRequest, null)
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../authManagement/get", Gson().toJson(coreServiceRequest))
                retrofit.getToken(
                    base64,
                    Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this),
                    UtilHandler.getScreenDensity(this),
                    requestModel = coreServiceRequest
                ).enqueue(
                    object : Callback<CoreResponseModel> {
                        override fun onResponse(
                            call: Call<CoreResponseModel>,
                            response: Response<CoreResponseModel>
                        ) {
                            DialogUtils.hideDialog()
                            hideProgressDialog()
                            if (!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http..../authManagement/get", Gson().toJson(response.body()))
                            if (response.isSuccessful) {
                                val mCoreResponseModel: CoreResponseModel? = response.body()
                                if (mCoreResponseModel?.mfsResponseInfo != null) {
                                    SharedPrefUtils.tokenKey =
                                        Constants.BEARER + "" + mCoreResponseModel?.mfsResponseInfo?.token
                                }
                            }
                        }

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            DialogUtils.hideDialog()
                            Log.d(Constants.LOG_D_KEY, t.message.toString())
                        }

                    }
                )
            } else {
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
            }
        } catch (e: Exception) {
            Log.d(Constants.LOG_D_KEY, e.message.toString())
        }
    }

    private fun getBasicToken() = "Basic " + Base64.encodeToString(
        Constants.CORE_AUTH_KEY_US_PW.toByteArray(),
        Base64.NO_WRAP
    )

    private fun getUserInfo(userEmailorMsisdn: String, mpin: String, checkMsisdn: Boolean) {
        try {
            if (UtilHandler.isOnline(this)) {
                var mfsRequestInfo: MfsRequestInfo? = null
                showProgressDialog(getString(R.string.progress_dialog_message))
                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = Constants.SERVICE_TYPE_ZERO
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = Constants.CHANNEL_ID_23
                mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_1

                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                mfsRequestInfo = if (checkMsisdn)
                    MfsRequestInfo(
                        mToken = "",
                        mAccessCode = "",
                        customerMsisdn = userEmailorMsisdn
                    )
                else
                    MfsRequestInfo(mToken = "", mAccessCode = "", email = userEmailorMsisdn)


                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http....getUserInfo", Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.getUserInfo(
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
                                    Log.d("Http....getUserInfo", Gson().toJson(mCoreResponseModel))

                                if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status.equals("Success")) {

                                    if (mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(0)?.status.equals("1")) {
                                        if (SharedPrefUtils.msisdn != null && SharedPrefUtils.msisdn.equals(mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(0)?.msisdn)) {

                                        } else {
                                            isNewUser=true
                                        }
                                        SharedPrefUtils.entityType =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.entityType

                                        SharedPrefUtils.entitySubType =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.entitySubType

                                       SharedPrefUtils.tempMsisdn =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.msisdn
                                        SharedPrefUtils.entityId =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.entityId
                                        SharedPrefUtils.email =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.email
                                        /*Check is entity type is allowed to login or no*/
                                        val entityTypes =
                                            SharedPrefUtils.EntityTypeArray?.split(",")
                                                ?.map { it.toInt() }

                                        if (entityTypes != null &&SharedPrefUtils.tempMsisdn != null) {
                                            if (entityTypes.contains(SharedPrefUtils.entityType?.toInt())) {

                                                val checkValidEntitySubtype =
                                                    checkValidEntitySubtype(Constants.EMAIL)
                                                if (!checkValidEntitySubtype) {
                                                    isNewUser=false
                                                    return
                                                }
                                                verifyEmailOrMsisdnlAndmPin(
                                                    userEmailorMsisdn,
                                                    mpin,
                                                    checkMsisdn
                                                )
                                            } else {
                                                isNewUser=false
                                                upDateErrorUI(
                                                    true,
                                                    getString(R.string.invalid_credential_error_message),
                                                    Constants.EMAIL
                                                )
                                                UtilHandler.showSnackBar(
                                                    "You are not allowed to login",
                                                    binding.root
                                                )
                                                //DialogUtils.showCommonDialog(0,true,this@LoginActivity,"You are not allowed to login", OnClickListener{ })
                                            }
                                        }
                                    } else {
                                        upDateErrorUI(
                                            true,
                                            getString(R.string.wallet_not_active),
                                            Constants.EMAIL
                                        )
                                        UtilHandler.showSnackBar(
                                            getString(R.string.wallet_not_active),
                                            binding.root
                                        )
                                    }

                                } else {
                                    upDateErrorUI(
                                        true,
                                        getString(R.string.user_not_registered_error_message),
                                        Constants.EMAIL
                                    )
                                    UtilHandler.showSnackBar(
                                        mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.errorDescription,
                                        binding.root
                                    )
                                }


                            }

                        }

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            DialogUtils.hideDialog()
                            Log.d(Constants.LOG_D_KEY, t.message.toString())
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

    private fun checkValidEntitySubtype(isWalletOrEmailLogin: String): Boolean {

        val entitySubTypes = SharedPrefUtils.EntitySubTypeArray?.split(",")

        return if ((entitySubTypes != null) && entitySubTypes.isNotEmpty() && (SharedPrefUtils.entitySubType != null)) {

            if (entitySubTypes.contains(SharedPrefUtils.entitySubType)) {
                return entitySubTypes.contains(SharedPrefUtils.entitySubType)
            } else {
                upDateErrorUI(
                    true,
                    getString(R.string.update_kyc_message),
                    isWalletOrEmailLogin
                )
                false
            }


        } else {
            false
        }

    }

    private fun getUserInfo2(userEmailorMsisdn: String, mpin: String, checkMsisdn: Boolean,fromBioMetric:Boolean) {
        try {
            if (UtilHandler.isOnline(this)) {
                var mfsRequestInfo: MfsRequestInfo? = null
                showProgressDialog(getString(R.string.progress_dialog_message))
                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = Constants.SERVICE_TYPE_ZERO
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = Constants.CHANNEL_ID_23
                mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_1

                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                mfsRequestInfo = if (checkMsisdn)
                    MfsRequestInfo(
                        mToken = "",
                        mAccessCode = "",
                        customerMsisdn = userEmailorMsisdn
                    )
                else
                    MfsRequestInfo(mToken = "", mAccessCode = "", email = userEmailorMsisdn)


                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http...getUserInfo", Gson().toJson(coreServiceRequest))
                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.getUserInfo(
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
                                    Log.d("Http...getUserInfo2", Gson().toJson(mCoreResponseModel))
                                if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status.equals(
                                        "Success"
                                    )
                                ) {

                                    if (mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(0)?.status.equals("1")) {

                                        if (SharedPrefUtils.msisdn != null && SharedPrefUtils.msisdn.equals(mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(0)?.msisdn)) {

                                        } else {
                                            isNewUser=true
                                        }


                                        SharedPrefUtils.entityType =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.entityType

                                        SharedPrefUtils.entitySubType =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.entitySubType

                                        SharedPrefUtils.businessName =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.businessName

                                       SharedPrefUtils.tempMsisdn =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.msisdn
                                        SharedPrefUtils.entityId =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.entityId

                                        SharedPrefUtils.email =
                                            mCoreResponseModel?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                                                0
                                            )?.email


                                        if (SharedPrefUtils.tempMsisdn != "") {
                                           SharedPrefUtils.tempMsisdn?.let { validateUser(it) }
                                        } else {
                                            if(!fromBioMetric)
                                            validateUser(binding.editInputWalletOrEmail.text.toString())
                                            else
                                                validateUser(userEmailorMsisdn)
                                        }


                                    } else {
                                        UtilHandler.showSnackBar(
                                            getString(R.string.wallet_not_active),
                                            binding.root
                                        )
                                    }

                                } else {
                                    upDateErrorUI(
                                        true,
                                        getString(R.string.user_not_registered_error_message),
                                        Constants.WALLET
                                    )
                                }


                            }

                        }

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            DialogUtils.hideDialog()
                            Log.d(Constants.LOG_D_KEY, t.message.toString())
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

    private fun verifyEmailOrMsisdnlAndmPin(
        userEmailorMisdn: String,
        mPin: String,
        verifyMsisdn: Boolean
    ) {

        try {
            if (UtilHandler.isOnline(this)) {
                var mfsRequestInfo: MfsRequestInfo? = null
                showProgressDialog(getString(R.string.progress_dialog_message))
                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = Constants.SERVICE_TYPE_ZERO
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = Constants.CHANNEL_ID_23
                mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_4

                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                if (verifyMsisdn)
                    mfsRequestInfo = MfsRequestInfo(
                        mToken = "",
                        mAccessCode = UtilHandler.enCryptPassword(mPin),
                        msisdn = userEmailorMisdn,
                        entityType = SharedPrefUtils.entityType
                    )
                // mfsRequestInfo = MfsRequestInfo("", UtilHandler.enCryptPassword(mPin), userEmail, SharedPrefUtils.entityType)
                else
                    mfsRequestInfo = MfsRequestInfo(
                        mToken = "",
                        mAccessCode = UtilHandler.enCryptPassword(mPin),
                        email = userEmailorMisdn,
                        entityType = SharedPrefUtils.entityType
                    )


                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http...verifyMpin", Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.verifyEmailAndmPin(
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
                                    Log.d("Http...verifyMpin", Gson().toJson(mCoreResponseModel))

                                if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status.equals(
                                        "Success"
                                    )
                                ) {
                                    //if (!SharedPrefUtils.isFirstTimeLogin) {
                                    if (isNewUser) {
                                        sendRequestToGenerateOtp(SharedPrefUtils.tempMsisdn.toString())
                                    } else {
                                        sendToDashBoardScreen()
                                    }
                                    //SharedPrefUtils.tokeyKey=Constants.BEARER+ mCoreResponseModel?.mfsResponseInfo?.token
                                    // validateUser()

                                } else {
                                    isNewUser=false
                                    upDateErrorUI(
                                        true,
                                        getString(R.string.invalid_credential_error_message),
                                        Constants.EMAIL
                                    )
                                    UtilHandler.showSnackBar(
                                        mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.errorDescription,
                                        binding.root
                                    )
                                }

                            }

                        }

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            isNewUser=false
                            DialogUtils.hideDialog()
                            Log.d(Constants.LOG_D_KEY, t.message.toString())
                        }

                    }
                )
            } else {
                isNewUser=false
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
            }
        } catch (e: Exception) {
            isNewUser=false
            e.message
        }
    }

    private fun validateUser(msisdn: String) {
        try {
            if (UtilHandler.isOnline(this)) {
                showProgressDialog(getString(R.string.progress_dialog_message))
                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = Constants.SERVICE_TYPE_ZERO
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = Constants.CHANNEL_ID_23
                mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_4

                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                val mfsRequestInfo =
                    MfsRequestInfo(msisdn = msisdn)

                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../validateUser", Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.validateUser(
                    getBasicToken(), Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this), UtilHandler.getScreenDensity(this),
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
                                        "Http..../validateUser",
                                        Gson().toJson(mCoreResponseModel)
                                    )
                                if (mCoreResponseModel?.mfsResponseInfo != null) {
                                    if (isNumberActivated(
                                            mCoreResponseModel.mfsResponseInfo.status,
                                            mCoreResponseModel.mfsResponseInfo.msisdn
                                        )
                                    ) {
                                       SharedPrefUtils.tempMsisdn = msisdn
                                        SharedPrefUtils.entityType =
                                            mCoreResponseModel.mfsResponseInfo.entityType
                                        //if (!SharedPrefUtils.isFirstTimeLogin) {
                                        if (isNewUser) {
                                            /*Check is entity type is allowed to login or no*/
                                            val entityTypes =
                                                SharedPrefUtils.EntityTypeArray?.split(",")
                                                    ?.map { it.toInt() }
                                            if (entityTypes != null) {
                                                if (entityTypes.contains(SharedPrefUtils.entityType?.toInt())) {

                                                    val checkValidEntitySubtype =
                                                        checkValidEntitySubtype(Constants.WALLET)

                                                    if (!checkValidEntitySubtype) {
                                                        isNewUser=false
                                                        return
                                                    }
                                                    sendRequestToGenerateOtp(msisdn)


                                                } else {
                                                    isNewUser=false
                                                    upDateErrorUI(
                                                        true,
                                                        getString(R.string.invalid_credential_error_message),
                                                        Constants.WALLET
                                                    )
                                                    //UtilHandler.showSnackBar("You are not allowed to login",binding.root)
                                                    //DialogUtils.showCommonDialog(0,true,this@LoginActivity,"You are not allowed to login", OnClickListener{ })
                                                }
                                            }
                                        } else {
                                            sendToDashBoardScreen()
                                        }
                                    } else {
                                        var status = ""
                                        if (!TextUtils.isEmpty(mCoreResponseModel.mfsResponseInfo.status)) {

                                            if (mCoreResponseModel.mfsResponseInfo.status.equals(
                                                    RequestConstants.SUSPENDED_STATUS
                                                )
                                            ) {
                                                status = getString(R.string.suspended)
                                                isNewUser=false
                                                UtilHandler.showSnackBar(
                                                    getString(R.string.suspended),
                                                    binding.root
                                                )
                                            }

                                            if (mCoreResponseModel.mfsResponseInfo.status.equals(
                                                    RequestConstants.CLOSED_STATUS
                                                )
                                            ) {
                                                isNewUser=false
                                                status = getString(R.string.closed)
                                                UtilHandler.showSnackBar(
                                                    getString(R.string.closed),
                                                    binding.root
                                                )
                                            }

                                            if (mCoreResponseModel.mfsResponseInfo.status.equals(
                                                    RequestConstants.FROZEN_STATUS
                                                )
                                            ) {
                                                isNewUser=false
                                                status = getString(R.string.frozen)
                                                UtilHandler.showSnackBar(
                                                    getString(R.string.frozen),
                                                    binding.root
                                                )
                                            }

                                            if (mCoreResponseModel.mfsResponseInfo.status.equals(
                                                    RequestConstants.PENDING_STATUS
                                                )
                                            ) {
                                                isNewUser=false
                                                status = getString(R.string.pending)
                                                UtilHandler.showSnackBar(
                                                    getString(R.string.pending),
                                                    binding.root
                                                )
                                            }
                                        }
                                    }

                                }
                            } else {
                                isNewUser=false
                                UtilHandler.showSnackBar(
                                    getString(R.string.something_went_wrong),
                                    binding.root
                                )
                            }

                        }

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            isNewUser=false
                            DialogUtils.hideDialog()
                            Log.d(Constants.LOG_D_KEY, t.message.toString())
                        }

                    }
                )
            } else {
                isNewUser=false
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
            }
        } catch (e: Exception) {
            e.message
        }
    }

    private fun upDateErrorUI(isError: Boolean, message: String, isWalletOrEmailLogin: String) {
        val backgroundDrawable = binding.linearEmailWalletBackground.background
        val borderColorError = ContextCompat.getColor(this, R.color.otp_status_text_color_wrong)
        val borderColorNormal = ContextCompat.getColor(this, R.color.nfc_not_text_color_for_desc)
        // Create a new GradientDrawable based on the existing drawable
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(
            ContextCompat.getColor(
                this,
                R.color.edit_box_color
            )
        ) // Set the desired solid color
        gradientDrawable.cornerRadius =
            resources.getDimension(R.dimen.dp_16) // Set the desired corner radius
        if (isError) {
            gradientDrawable.setStroke(
                4,
                borderColorError
            ) // Set the desired border color and width
            if (isWalletOrEmailLogin == Constants.EMAIL) {
                binding.textErrorDescForWallet.visibility = View.GONE
                binding.textErrorDescForEmail.visibility = View.VISIBLE
                binding.textErrorOnWallet.visibility = View.GONE
                binding.textErrorOnEmail.visibility = View.VISIBLE
                binding.textErrorOnEmail.text = message
                /*For 2nd time login*/
                binding.errorLayout2ndLogin.visibility = View.VISIBLE
                binding.textErrorOnEmail2.text = message
            } else {
                binding.textErrorDescForWallet.visibility = View.VISIBLE
                binding.textErrorDescForEmail.visibility = View.GONE
                binding.textErrorOnWallet.visibility = View.VISIBLE
                binding.textErrorOnEmail.visibility = View.GONE
                binding.textErrorOnWallet.text = message
                /*For 2nd time login*/
                binding.errorLayout2ndLogin.visibility = View.VISIBLE
                binding.textErrorOnEmail2.text = message
            }

        } else {
            gradientDrawable.setStroke(
                4,
                borderColorNormal
            ) // Set the desired border color and width
            binding.textErrorDescForWallet.visibility = View.GONE
            binding.textErrorDescForEmail.visibility = View.GONE
            binding.errorLayout2ndLogin.visibility = View.GONE
        }
        // Set the modified GradientDrawable as the background of the view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            binding.linearEmailWalletBackground.background = gradientDrawable
            binding.llForPinFirstTimeLogin.background = gradientDrawable
            binding.emailWallet2ndLogin.background = gradientDrawable
            binding.pin2ndLogin.background = gradientDrawable

        } else {
            binding.linearEmailWalletBackground.setBackgroundDrawable(gradientDrawable)
            binding.llForPinFirstTimeLogin.setBackgroundDrawable(gradientDrawable)
            binding.emailWallet2ndLogin.setBackgroundDrawable(gradientDrawable)
            binding.pin2ndLogin.setBackgroundDrawable(gradientDrawable)

        }
    }

    private fun validateAndLogIn() {
        typeOfWalletOrEmail = if (binding.textUseWalletEmailUnderline.text.equals("Use Email")) {
            Constants.WALLET
        } else {
            Constants.EMAIL
        }
        if (TextUtils.isEmpty(binding.editInputSecondEmailAndWallet.text.toString())) {
            UtilHandler.showSnackBar(getString(R.string.email_empty), binding.root)
            return
        }
        if (TextUtils.isEmpty(binding.editInputPin2.text.toString())) {
            UtilHandler.showSnackBar(getString(R.string.pin_empty), binding.root)
            return
        }
        if (binding.editInputPin2.text.toString().length != 4) {
            UtilHandler.showSnackBar(getString(R.string.four_digit_pin), binding.root)
            return
        }
        if (binding.textUseWalletEmailUnderline.text.equals(getString(R.string.use_wallet_id_underline))) {

            if (!Constants.EMAIL_ADDRESS_PATTERN?.matcher(binding.editInputSecondEmailAndWallet.text.toString())
                    ?.matches()!!
            ) {
                UtilHandler.showSnackBar(getString(R.string.enter_valid_email), binding.root)
                return
            }
            getUserInfo(
                binding.editInputSecondEmailAndWallet.text.toString(),
                binding.editInputPin2.text.toString(), false
            )
        } else {
            //validateUser(binding.editInputSecondEmailAndWallet.text.toString())
            //verifyEmailOrMsisdnlAndmPin(binding.editInputSecondEmailAndWallet.text.toString(),binding.editInputPin2.text.toString(),true)
            getUserInfo(
                binding.editInputSecondEmailAndWallet.text.toString(),
                binding.editInputPin2.text.toString(),
                true
            )
        }
    }

    private fun sendToDashBoardScreen() {

        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra(Constants.EDITTEXTVALUE, binding.editInputWalletOrEmail.text.toString())
        intent.putExtra(Constants.TYPEWALLETOREMAIL, typeOfWalletOrEmail)
        startActivity(intent)
    }

    private fun sendRequestToGenerateOtp(msisdn: String) {
        try {
            if (UtilHandler.isOnline(this)) {
                showProgressDialog(getString(R.string.progress_dialog_message))
                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = Constants.SERVICE_TYPE_ZERO
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
                        msisdn = msisdn,
                        email = getEmail(),
                        length = Constants.OTP_LENGTH_6
                    )

                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../tokenManagement/get", Gson().toJson(coreServiceRequest))

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
                                        upDateErrorUI(false, "", "")
                                        val mCoreResponseModel: CoreResponseModel? = response.body()
                                        if(!BuildConfig.USE_PRODUCTION_URLS)
                                        Log.d("GenerateOtp_Response", Gson().toJson(mCoreResponseModel))
                                        val intent =
                                            Intent(
                                                this@LoginActivity,
                                                OtpValidateActivity::class.java
                                            )
                                        intent.putExtra(
                                            Constants.REQUEST_FROM,
                                            Constants.FROM_LOGIN
                                        )
                                        intent.putExtra(
                                            Constants.EDITTEXTVALUE,
                                            binding.editInputWalletOrEmail.text.toString()
                                        )
                                        intent.putExtra(
                                            Constants.TYPEWALLETOREMAIL,
                                            typeOfWalletOrEmail
                                        )
                                        startActivity(intent)
                                    } else {

                                        UtilHandler.showSnackBar(
                                            mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.errorDescription,
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

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            DialogUtils.hideDialog()
                            Log.d(Constants.LOG_D_KEY, t.message.toString())
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

    private fun getEmail(): String? {
        return if (SharedPrefUtils.email.equals(""))
            null
        else SharedPrefUtils.email
    }

    private fun isNumberActivated(status: String?, msisdn: String?): Boolean {
        var numberActivated = false
        numberActivated =
            if (!TextUtils.isEmpty(msisdn) && msisdn.equals(
                    RequestConstants.SUCCESS_S,
                    ignoreCase = true
                )
            ) {
                !TextUtils.isEmpty(status) && (status.equals(
                    RequestConstants.ACTIVATE_STATUS_1,
                    ignoreCase = true
                ) || status.equals(RequestConstants.ACTIVATE_STATUS_6, ignoreCase = true))
            } else {
                true
            }
        return numberActivated
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btn_proceed -> proceedToLogin()
            R.id.btn_change_email_walletID -> switchLoginTye()
            R.id.btn_second_proceed -> validateAndLogIn()
            R.id.text_useWalletEmailUnderline -> emailWalletUnderlineClick()
            R.id.textTermAndConditions -> openPrivacyPolicy()

        }

    }

    private fun openPrivacyPolicy() {
        val intent = Intent(this@LoginActivity, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun emailWalletUnderlineClick() {
        upDateErrorUI(false, "", "")
        if (binding.textUseWalletEmailUnderline.text.equals(getString(R.string.use_email_underline))) {
            binding.textUseWalletEmailUnderline.text = getString(R.string.use_wallet_id_underline)
            binding.textHintSecondWalletAndEmail.text = getString(R.string.email_address)
            binding.editInputSecondEmailAndWallet.hint =
                getString(R.string.please_input_email_address)
            binding.editInputSecondEmailAndWallet.text.clear()
            binding.editInputSecondEmailAndWallet.inputType =
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        } else {
            binding.textUseWalletEmailUnderline.text = getString(R.string.use_email_underline)
            binding.textHintSecondWalletAndEmail.text = getString(R.string.wallet_is_wid)
            binding.editInputSecondEmailAndWallet.hint = getString(R.string.please_input_wallet_id)
            binding.editInputSecondEmailAndWallet.text.clear()
            binding.editInputSecondEmailAndWallet.inputType =
                InputType.TYPE_CLASS_NUMBER
        }
    }

    private fun switchLoginTye() {
        OtpValidateActivity().clearMsisdnOTPCache(false)
        binding.editInputPin1.text.clear()
        upDateErrorUI(false, "", "")
        binding.btnProceed.isEnabled = false
        binding.btnProceed.isClickable = false
        binding.btnProceed.alpha = 0.5f
        if (binding.btnChangeEmailWalletID.text.equals(getString(R.string.login_with_email_address))) {
            binding.textHeadingForLoginFirstTime.text = getString(R.string.please_login_with_email)
            binding.btnChangeEmailWalletID.text = getString(R.string.login_with_wallet_id)
            binding.textHintWalletOrEmail.text = getString(R.string.email_address)
            binding.editInputWalletOrEmail.hint = "Please input your email address"
            binding.editInputWalletOrEmail.text.clear()
            binding.editInputWalletOrEmail.inputType =
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            binding.constraintEditetxtPin2.visibility = View.VISIBLE
            binding.editInputPin2.text.clear()
            binding.linearLoader12EmaiPin.visibility = View.VISIBLE
            binding.linearLoader13Wallet.visibility = View.GONE

        } else {
            binding.textHeadingForLoginFirstTime.text = getString(R.string.please_login_with_wallet)
            binding.btnChangeEmailWalletID.text = getString(R.string.login_with_email_address)
            binding.textHintWalletOrEmail.text = getString(R.string.wallet_id)
            binding.editInputWalletOrEmail.hint = "Please input your wallet ID"
            binding.editInputWalletOrEmail.text.clear()
            binding.editInputWalletOrEmail.inputType =
                InputType.TYPE_CLASS_NUMBER
            binding.constraintEditetxtPin2.visibility = View.GONE
            binding.linearLoader12EmaiPin.visibility = View.GONE
            binding.linearLoader13Wallet.visibility = View.VISIBLE
        }


    }

    private fun proceedToLogin() {
        typeOfWalletOrEmail = if (binding.textHintWalletOrEmail.text.equals("Wallet ID")) {
            Constants.WALLET
        } else {
            Constants.EMAIL
        }
        if (binding.btnChangeEmailWalletID.text.equals(getString(R.string.login_with_wallet_id))) {

            if (TextUtils.isEmpty(binding.editInputWalletOrEmail.text.toString())) {
                UtilHandler.showSnackBar(getString(R.string.email_empty), binding.root)
                return
            }
            if (!Constants.EMAIL_ADDRESS_PATTERN?.matcher(binding.editInputWalletOrEmail.text.toString())
                    ?.matches()!!
            ) {
                UtilHandler.showSnackBar(getString(R.string.enter_valid_email), binding.root)
                return
            }
            if (TextUtils.isEmpty(binding.editInputPin1.text.toString())) {
                UtilHandler.showSnackBar(getString(R.string.pin_empty), binding.root)
                return
            }
            if (binding.editInputPin1.text.toString().length != 4) {
                UtilHandler.showSnackBar(getString(R.string.four_digit_pin), binding.root)
                return
            }

            getUserInfo(
                binding.editInputWalletOrEmail.text.toString(),
                binding.editInputPin1.text.toString(),
                false
            )

        } else if (binding.btnChangeEmailWalletID.text.equals(getString(R.string.login_with_email_address))) {

            if (TextUtils.isEmpty(binding.editInputWalletOrEmail.text.toString())) {
                UtilHandler.showSnackBar(getString(R.string.empty_walletId), binding.root)
                return
            }
            /*if (binding.editInputPin1.text.toString().length !=4 ) {
                UtilHandler.showSnackBar(getString(R.string.four_digit_pin), binding.root)
                return
            }*/
            else
            //validateUser(binding.editInputWalletOrEmail.text.toString())
                getUserInfo2(binding.editInputWalletOrEmail.text.toString(), "",
                    checkMsisdn = true,
                    fromBioMetric = false
                )
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity();
    }
}