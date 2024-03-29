package com.nownow.softpos.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.nownow.softpos.BuildConfig
import com.nownow.softpos.R
import com.nownow.softpos.api.ApiCalls
import com.nownow.softpos.databinding.ActivityProfileSettingsBinding
import com.nownow.softpos.helper.BiometricHelper
import com.nownow.softpos.models.softpos.*
import com.nownow.softpos.network.ServiceBuilderV2
import com.nownow.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileSettingsActivity : BaseActivity(), OnClickListener {
    lateinit var mBinding: ActivityProfileSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile_settings)
        setUI()
        setClickListeners()
        mBinding.toggleBiometricBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mBinding.textTurnOnOffLeft.text = "Turn OFF"
                mBinding.textTurnOnOffLeft.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray
                    )
                )
                mBinding.textTurnOnOffRight.text = "ON"
                mBinding.textTurnOnOffRight.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.orange_e9
                    )
                )
                mBinding.toggleBiometricBtn.trackTintList =
                    ContextCompat.getColorStateList(this, R.color.orange_e9)
                mBinding.toggleBiometricBtn.thumbTintList =
                    ContextCompat.getColorStateList(this, R.color.white)
                enableDisableBioMetric()
            } else {
                SharedPrefUtils.bioMetricUUID = "Disable"
                SharedPrefUtils.isBiometricEnable = false
                mBinding.textTurnOnOffLeft.text = " OFF"
                mBinding.textTurnOnOffLeft.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                mBinding.textTurnOnOffRight.text = "Turn ON"
                mBinding.textTurnOnOffRight.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.gray
                    )
                )
                mBinding.toggleBiometricBtn.trackTintList =
                    ContextCompat.getColorStateList(this, R.color.gray)
                mBinding.toggleBiometricBtn.thumbTintList =
                    ContextCompat.getColorStateList(this, R.color.white)
            }
        }
    }

    private fun setClickListeners() {
        //mBinding.toggleBiometricBtn.setOnClickListener(this)
        mBinding.llLogout.setOnClickListener(this)
        mBinding.llMoveFunds.setOnClickListener(this)
        mBinding.llTermsAndPolicy.setOnClickListener(this)
        mBinding.header.backAction.setOnClickListener(this)
        mBinding.llSecureWallet.setOnClickListener(this)

    }

    private fun setUI() {
        mBinding.textVersionName.text = BuildConfig.VERSION_NAME
        mBinding.textBuildname.text = BuildConfig.VERSION_CODE.toString()
        if (SharedPrefUtils.parentID != null && !SharedPrefUtils.parentID.equals("0")){
            mBinding.llMoveFunds.visibility = View.VISIBLE
        }else{
            mBinding.llMoveFunds.visibility = View.GONE
        }
        if (SharedPrefUtils.isTerminalLinked) {
            mBinding.textWalletOnProfile.text = SharedPrefUtils.msisdn
            mBinding.textTerminalLinkOnProfile.text = getString(R.string.wallet_linked)
        } else {
            mBinding.textWalletOnProfile.visibility = View.GONE
            mBinding.textTerminalLinkOnProfile.text = getString(R.string.wallet_unlinked)
        }
        if (SharedPrefUtils.UserFirstName != null) {
            mBinding.userNameOnProfile.text = UtilHandler.capitalizeFirstLetter(SharedPrefUtils.UserFirstName)
        } else {
            mBinding.userNameOnProfile.text = " "
        }
        mBinding.header.activityTitle.text = getString(R.string.profile_settings)
        if (SharedPrefUtils.bioMetricUUID != null && SharedPrefUtils.bioMetricUUID.equals("Enable")) {
            Log.d(Constants.LOG_D_RESPONSE, "YESENABLE")
            mBinding.toggleBiometricBtn.isChecked = true
            mBinding.textTurnOnOffLeft.text = "Turn OFF"
            mBinding.textTurnOnOffLeft.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.gray
                )
            )
            mBinding.textTurnOnOffRight.text = "ON"
            mBinding.textTurnOnOffRight.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.orange_e9
                )
            )
            //val trackColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            mBinding.toggleBiometricBtn.trackTintList =
                ContextCompat.getColorStateList(this, R.color.orange_e9)
            mBinding.toggleBiometricBtn.thumbTintList =
                ContextCompat.getColorStateList(this, R.color.white)
        } else if (SharedPrefUtils.bioMetricUUID != null && SharedPrefUtils.bioMetricUUID.equals("Disable")) {
            Log.d(Constants.LOG_D_RESPONSE, "NOENABLE")
            mBinding.toggleBiometricBtn.isChecked = false
            mBinding.textTurnOnOffLeft.text = " OFF"
            mBinding.textTurnOnOffLeft.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.black
                )
            )
            mBinding.textTurnOnOffRight.text = "Turn ON"
            mBinding.textTurnOnOffRight.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.gray
                )
            )
            //val trackColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            mBinding.toggleBiometricBtn.trackTintList =
                ContextCompat.getColorStateList(this, R.color.gray)
            mBinding.toggleBiometricBtn.thumbTintList =
                ContextCompat.getColorStateList(this, R.color.white)
        } else {
            mBinding.textTurnOnOffLeft.text = " OFF"
            mBinding.textTurnOnOffLeft.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.black
                )
            )
            mBinding.textTurnOnOffRight.text = "Turn ON"
            mBinding.textTurnOnOffRight.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.gray
                )
            )
            //val trackColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            mBinding.toggleBiometricBtn.trackTintList =
                ContextCompat.getColorStateList(this, R.color.gray)
            mBinding.toggleBiometricBtn.thumbTintList =
                ContextCompat.getColorStateList(this, R.color.white)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ll_secure_wallet -> secureWallet()
            R.id.ll_logout -> logOut()
            R.id.toggle_biometric_btn -> enableDisableBioMetric()
            R.id.ll_terms_and_policy -> navigateToTermsAndConditions()
            R.id.ll_move_funds -> moveFunds()
            R.id.backAction -> onBackPressed()
            else -> enableDisableBioMetric()

        }
    }
   private fun showFullScreenProgressBar() {
       mBinding.rootLayout.visibility=View.GONE
       mBinding.progressLayout.visibility=View.VISIBLE

   }
    private fun hideFullScreenProgressBar()
    {
        mBinding.rootLayout.visibility=View.VISIBLE
        mBinding.progressLayout.visibility=View.GONE
    }
    private fun showSuccesskeyDownload(){
        mBinding.progressBar.visibility=View.GONE
        //mBinding.progressBar.color
        mBinding.imgSuccess.visibility=View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        },1000)

    }
    private fun secureWallet() {
        try {
            if (!UtilHandler.isOnline(this)) {
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), mBinding.root)
                return
            }
            showFullScreenProgressBar()
            val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
            retrofit.getTerminalDetails(SharedPrefUtils.tokenKey,Constants.PLATFORM,
                UtilHandler.getPackageVersion(this),UtilHandler.getScreenDensity(this), UtilHandler.getDeviceId(this))
                .enqueue(object : Callback<GetTerminalDetails> {
                    override fun onResponse(
                        call: Call<GetTerminalDetails>,
                        response: Response<GetTerminalDetails>
                    ) {
                        //hideProgressDialog()
                        if (response.isSuccessful) {
                            if(!BuildConfig.USE_PRODUCTION_URLS)
                            Log.d("Http..../getTerminalDetailsByTerminalId", Gson().toJson(response.body()))
                            val terminalDetails: GetTerminalDetails? = response.body()
                            if (terminalDetails?.cterminalId != null) {
                                downloadTerminalData(terminalDetails?.cterminalId)
                            }
                            else{
                                hideFullScreenProgressBar()
                                UtilHandler.showSnackBar("Could not get Terminal Id",
                                    mBinding.root
                                )
                            }
                        } else {
                            hideFullScreenProgressBar()
                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                mBinding.root
                            )
                        }

                    }

                    override fun onFailure(call: Call<GetTerminalDetails>, t: Throwable) {
                        hideFullScreenProgressBar()
                        UtilHandler.showSnackBar(
                            getString(R.string.something_went_wrong),
                            mBinding.root
                        )

                    }

                })
        } catch (e: Exception) {
            hideFullScreenProgressBar()
            e.printStackTrace()
        }
    }

    private fun downloadTerminalData(cTerminalId: String?) {
        try {

            if (!UtilHandler.isOnline(this)) {
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), mBinding.root)
                return
            }
            val valueMap = HashMap<String, String>()
            valueMap["cTerminalId"] = cTerminalId.toString()
            val isoRequest = IsoRequest(cardData = valueMap)
            val transactionInfo = TransactionInfo(
                comments = "this is the device validation request",
                cardType = "NFC",
                isoRequest = isoRequest
            )
            val requestInfo = RequestInfo(
                msisdn = SharedPrefUtils.msisdn,
                requestCts = UtilHandler.getCurrentDate(),
                requestType = "TerminalData",
                serialId = UtilHandler.getDeviceId(this),
                terminalId = UtilHandler.getDeviceId(this),
                transactionInfo = transactionInfo
            )

            val requestModel = CommonQposRequestModel(requestInfo = requestInfo)
            //DialogUtils.showDialog(this, getString(R.string.progress_dialog_message))
            if(!BuildConfig.USE_PRODUCTION_URLS)
                Log.d("Http.../getTerminalData",Gson().toJson(requestModel))

            val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
            retrofit.downloadTerminalData(SharedPrefUtils.tokenKey, Constants.PLATFORM,
                UtilHandler.getPackageVersion(this),UtilHandler.getScreenDensity(this),requestModel).enqueue(
                object : Callback<CommonQposResponseModel> {
                    override fun onFailure(call: Call<CommonQposResponseModel>, t: Throwable) {
                        //hideProgressDialog()
                        hideFullScreenProgressBar()
                        UtilHandler.showSnackBar(
                            getString(R.string.something_went_wrong),
                            mBinding.root
                        )

                    }

                    override fun onResponse(
                        call: Call<CommonQposResponseModel>,
                        response: Response<CommonQposResponseModel>
                    ) {
                        if (response.isSuccessful) {
                            //hideProgressDialog()
                            //hideFullScreenProgressBar()
                            val commonQposResponseModel = response.body()
                            if(!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http.../getTerminalData", Gson().toJson(commonQposResponseModel))

                            if (commonQposResponseModel != null) {
                                if (commonQposResponseModel!!.responseInfo!!.transactionInfo!!.vendoreResultCode == 0 && commonQposResponseModel!!.responseInfo!!.transactionInfo!!.resultCode == 0) {

                                    SoftPosConstants.getDataFormat(commonQposResponseModel!!.responseInfo!!.transactionInfo!!.terminalData)
                                    showSuccesskeyDownload()
                                    UtilHandler.showSnackBar(
                                        "Keys Downloaded/Updated successfully.",
                                        mBinding.getRoot()
                                    );


                                } else {

                                    //DialogUtils.hideDialog()
                                    hideFullScreenProgressBar()

                                    UtilHandler.showSnackBar(
                                        "Unable to perform Master Key Injection " + commonQposResponseModel.responseInfo!!.transactionInfo!!.resultDesc,
                                        mBinding.root
                                    )
                                }

                            }
                        }
                    }


                }
            )


        } catch (e: Exception) {
           hideFullScreenProgressBar()
            UtilHandler.showSnackBar(getString(R.string.something_went_wrong), mBinding.root)
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun logOut() {
        SharedPrefUtils.parentID = null
        SharedPrefUtils.UserFirstName = null
        SharedPrefUtils.DeviceID = null
        SharedPrefUtils.loggedOut=true
        val intent = Intent(this@ProfileSettingsActivity, SplashActivity::class.java)
        startActivity(intent)
    }

    private fun enableDisableBioMetric() {
        this.let { BiometricHelper.initializeBiometricScannerLogin(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.let { BiometricHelper.initializeBiometricScanner(it) }
        }
        if (mBinding.toggleBiometricBtn.isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.let { it1 ->
                    BiometricHelper.showPrompt(" ", object : IBiometricListener {
                        override fun onEnrolled() {
                            biometricActionNoBiometric(getString(R.string.no_biometric_or_hardware))
                        }

                        override fun onNegativeClick() {
                            Log.d(Constants.LOG_D_RESPONSE, "onNegativeClick")
                            biometricActionNoBiometric(getString(R.string.no_biometric_or_hardware))
                            //UtilHandler.showToast(getString(R.string.biometric_un_success))
                        }

                        override fun onSuccess() {
                            SharedPrefUtils.bioMetricUUID = "Enable"
                            SharedPrefUtils.isBiometricEnable = true
                            UtilHandler.showSnackBar(
                                getString(R.string.biometric_setup_successful),
                                mBinding.root
                            )
                            //dismiss()
                            //val b = LoginSuccessDialog.newInstance("BIOMETRIC", "Y")
                            //b.show(requireActivity().supportFragmentManager, "Login Success")

                            /*val intent = Intent(activity, LoginSuccessDialog::class.java)
                            intent.putExtra(Constants.REQUEST_FROM, "BIOMETRIC")
                            intent.putExtra(Constants.IS_BIO_ACTIVE, "Y")
                            startActivity(intent)*/

                            Log.d(Constants.LOG_D_RESPONSE, "onSuccess")
                            //biometricStatusUpdate()
                        }

                        override fun onCancel(errString: String) {
                            biometricActionNoBiometric(getString(R.string.user_cancel_biometric))
                            Log.d(Constants.LOG_D_RESPONSE, "onCancel")
                            //UtilHandler.showToast(getString(R.string.biometric_un_success) + " " + errString)
                            //switch_biometric.isChecked = false
                            //button_setup.isEnabled = false
                        }

                        override fun onAuthError() {
                            mBinding.toggleBiometricBtn.isChecked = false
                            SharedPrefUtils.bioMetricUUID = "Disable"
                            SharedPrefUtils.isBiometricEnable = false
                            biometricActionNoBiometric(getString(R.string.no_biometric_or_hardware))
                            Log.d(Constants.LOG_D_RESPONSE, "onAuthError")
                            // switch_biometric.isChecked = false
                            //button_setup.isEnabled = false

                        }
                    }, it1)
                }
            } else {
                UtilHandler.showSnackBar("No Biometric Setup/Biometric hardware", mBinding.root)
            }
        } else {

            //val b = LoginSuccessDialog.newInstance("BIOMETRIC", "N")
            //b.show(requireActivity().supportFragmentManager, "Login Success")
        }
        Log.d(Constants.LOG_D_RESPONSE, mBinding.toggleBiometricBtn.isEnabled.toString())
    }

    private fun navigateToTermsAndConditions() {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun moveFunds() {
        val b = MoveFundDialog.newInstance(intent.getStringExtra(Constants.WALLET1_BALANCE_AMOUNT))
        b.show(supportFragmentManager, "Move Fund Wallet")
    }

    private fun biometricActionNoBiometric(message: String) {
        UtilHandler.showSnackBar(message, mBinding.root)
        SharedPrefUtils.bioMetricUUID = "Disable"
        SharedPrefUtils.isBiometricEnable = false
        mBinding.toggleBiometricBtn.isChecked = false
        mBinding.textTurnOnOffLeft.text = " OFF"
        mBinding.textTurnOnOffLeft.setTextColor(
            ContextCompat.getColor(
                this@ProfileSettingsActivity,
                R.color.black
            )
        )
        mBinding.textTurnOnOffRight.text = "Turn ON"
        mBinding.textTurnOnOffRight.setTextColor(
            ContextCompat.getColor(
                this@ProfileSettingsActivity,
                R.color.gray
            )
        )
        mBinding.toggleBiometricBtn.trackTintList =
            ContextCompat.getColorStateList(this@ProfileSettingsActivity, R.color.gray)
        mBinding.toggleBiometricBtn.thumbTintList =
            ContextCompat.getColorStateList(this@ProfileSettingsActivity, R.color.white)
    }
}