package com.aicortex.softpos.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.databinding.ActivityAccountDetailsBinding
import com.aicortex.softpos.models.core.request.*
import com.aicortex.softpos.models.core.response.CoreResponseModel
import com.aicortex.softpos.models.dashboard.TransactionInfo
import com.aicortex.softpos.network.ServiceBuilder
import com.aicortex.softpos.utils.Constants
import com.aicortex.softpos.utils.Logger
import com.aicortex.softpos.utils.SharedPrefUtils
import com.aicortex.softpos.utils.UtilHandler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountDetailsActivity : BaseActivity(), View.OnClickListener {
    private var manualCheck: Boolean = false
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMiliSeconds: Long = 60000*2 //2 minute in milliseconds

    //private var timeLeftInMiliSeconds:Long =10000 //10 second in milliseconds
    private var countDownInterval: Long = 1000 //1 Second in milliseconds
    lateinit var mBinding: ActivityAccountDetailsBinding
    lateinit var amount: String
    var txHistoryList: MutableList<TransactionInfo> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_account_details)
        getIntentData()
        setClickListeners()
        updateUI()
    }

    private fun updateUI() {
        if (amount != null) {
            mBinding.txtAmount.text = getString(R.string.amount_icon) + " " + amount
        }

        setBusinessName()

        if (SharedPrefUtils.msisdn != null) {
            mBinding.editWid.setText(SharedPrefUtils.msisdn)
        }

        mBinding.editInputBankName.setText("aicortex Digital Systems Ltd")
        disableCheckTranButton()
        startTimer()
    }

    private fun setBusinessName() {
        if (SharedPrefUtils.businessName != null && SharedPrefUtils.businessName != "") {
            mBinding.editMerchantName.setText(UtilHandler.capitalizeFirstLetter(SharedPrefUtils.businessName))
        } else {
            mBinding.editMerchantName.setText(UtilHandler.capitalizeFirstLetter(SharedPrefUtils.UserFirstName + " " + SharedPrefUtils.UserLastName))
        }
    }

    private fun disableCheckTranButton() {
        mBinding.btnCheckTran.isEnabled = false
        mBinding.btnCheckTran.isClickable = false
        mBinding.btnCheckTran.alpha = 0.5f
    }

    private fun enableCheckTranButton() {
        mBinding.btnCheckTran.text = getString(R.string.check_transaction)
        mBinding.btnCheckTran.isEnabled = true
        mBinding.btnCheckTran.isClickable = true
        mBinding.btnCheckTran.alpha = 1f
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMiliSeconds, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMiliSeconds = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                // Timer finished, perform any action you want here
                //timerTextView.text = "Timer Finished!"

                enableCheckTranButton()

            }
        }

        countDownTimer.start()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMiliSeconds / 1000) / 60
        val seconds = (timeLeftInMiliSeconds / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        //timerTextView.text = timeLeftFormatted
        mBinding.btnCheckTran.text = "Wait for $timeLeftFormatted seconds"
        if (minutes==0.toLong() && seconds == 10.toLong()) {
            manualCheck = false
            checkTransaction()
        }
    }

    private fun getIntentData() {
        amount = intent.getStringExtra(Constants.BILL_AMOUNT).toString()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setClickListeners() {
        mBinding.backArrowBtn.setOnClickListener(this)
        mBinding.btnCheckTran.setOnClickListener(this)

    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
            R.id.backArrowBtn -> onBackPressed()
            R.id.btn_check_tran -> {
                manualCheck = true
                checkTransaction()
            }
        }
    }

    private fun checkTransaction() {
        fetchTransactionHistory(0, 1)
    }

    private fun fetchTransactionHistory(pageNumber: Int, itemsPerPage: Int) {

        try {

            if (txHistoryList != null) {
                txHistoryList.clear()

            }
            if (UtilHandler.isOnline(this)) {

                val mfsTransactionInfo = MfsTransactionInfo()
                mfsTransactionInfo.requestId = UtilHandler.generateRequestId("").toString()
                mfsTransactionInfo.serviceType = "0"
                mfsTransactionInfo.timestamp = System.currentTimeMillis()
                val mfsSourceInfo = MfsSourceInfo()
                mfsSourceInfo.channelId = Constants.CHANNEL_ID_23
                mfsSourceInfo.surroundSystem = Constants.SURROUND_SYSTEM_1
                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                val mfsRequestInfo =
                    MfsRequestInfo(

                        entityId = SharedPrefUtils.entityId,
                        serviceId = Constants.SOFT_POS_SERVICE_TYPE_TAP_TAP + "," + Constants.SOFT_POS_SERVICE_TYPE_PWT,
                        count = "5",
                        offSet = pageNumber.toString()
                    )

                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../newHistory", Gson().toJson(coreServiceRequest))

                showProgressDialog(getString(R.string.progress_dialog_message))
                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.transactionHistoryNew(
                    SharedPrefUtils.tokenKey, Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this), UtilHandler.getScreenDensity(this),
                    coreServiceRequest
                )
                    .enqueue(
                        object : Callback<CoreResponseModel> {
                            override fun onResponse(
                                call: Call<CoreResponseModel>,
                                response: Response<CoreResponseModel>
                            ) {

                                val data: String = response.body().toString()
                                if (response.isSuccessful) {
                                    hideProgressDialog()
                                    val mCoreResponseModel: CoreResponseModel? =
                                        response.body()
                                    if(!BuildConfig.USE_PRODUCTION_URLS) {
                                        Log.d(
                                            "Http..../newHistory",
                                            Gson().toJson(mCoreResponseModel)
                                        )
                                    }
                                    if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status != null) {
                                        if (mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.status == "Success") {
                                            if (mCoreResponseModel.mfsResponseInfo?.transactionListInfo?.transactionInfo != null) {
                                                val txHistoryList =
                                                    mCoreResponseModel.mfsResponseInfo.transactionListInfo.transactionInfo
                                                hideProgressDialog()
                                                if (txHistoryList != null && txHistoryList.isNotEmpty()) {
                                                    if (txHistoryList[0].transactionType == Constants.SOFT_POS_SERVICE_TYPE_PWT) {
                                                        if (!SharedPrefUtils.lastTranId.equals(
                                                                txHistoryList[0].transactionId
                                                            )
                                                        ) {
                                                            //UtilHandler.showSnackBar("Transaction found",mBinding.root)
                                                            SharedPrefUtils.lastTranId =
                                                                txHistoryList[0].transactionId
                                                            if (amount == txHistoryList[0].transactionAmount) {
                                                                if (txHistoryList[0].transactionStatus.equals(
                                                                        Constants.PAYMENT_SUCCESS,
                                                                        true
                                                                    )
                                                                )
                                                                    redirectToPaymentStatusPage(
                                                                        txHistoryList.toMutableList(),
                                                                        Constants.PAYMENT_SUCCESS
                                                                    )
                                                                else if (txHistoryList[0].transactionStatus.equals(
                                                                        Constants.PAYMENT_FAILED,
                                                                        true
                                                                    )
                                                                )
                                                                    redirectToPaymentStatusPage(
                                                                        txHistoryList.toMutableList(),
                                                                        Constants.PAYMENT_FAILED
                                                                    )
                                                            } else {
                                                                navigateToTxHistory()
                                                            }

                                                        } else {
                                                            navigateToTxHistory()
                                                        }

                                                    } else {
                                                        navigateToTxHistory()
                                                    }
                                                } else {
                                                    UtilHandler.showSnackBar(
                                                        getString(R.string.no_history),
                                                        mBinding.root
                                                    )
                                                }


                                            } else {
                                                hideProgressDialog()

                                            }

                                        } else {
                                            hideProgressDialog()
                                            UtilHandler.showSnackBar(
                                                mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.errorDescription,
                                                mBinding.root
                                            )
                                        }
                                    } else {
                                        hideProgressDialog()
                                        UtilHandler.showSnackBar(
                                            getString(R.string.something_went_wrong),
                                            mBinding.root
                                        )
                                    }


                                } else {
                                    hideProgressDialog()
                                    UtilHandler.showSnackBar(
                                        getString(R.string.something_went_wrong),
                                        mBinding.root
                                    )
                                }
                            }

                            override fun onFailure(
                                call: Call<CoreResponseModel>,
                                t: Throwable
                            ) {
                                hideProgressDialog()
                                Logger.d(Constants.LOG_D_KEY, t.message)
                            }
                        }
                    )
            } else {
                hideProgressDialog()
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), mBinding.root)
            }
        } catch (e: Exception) {
            hideProgressDialog()
            e.message
            hideProgressDialog()
        }
    }

    private fun navigateToTxHistory() {
        if (manualCheck) {
            val intent = Intent(this, TransactionHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun redirectToPaymentStatusPage(
        txHistoryList: MutableList<TransactionInfo>,
        paymentStatus: String
    ) {

        try {
            val intent = Intent(this, SuccessActivity::class.java)
            intent.putExtra(Constants.PAYMENT_STATUS, paymentStatus)
            intent.putExtra(Constants.REQUEST_FROM, Constants.FROM_ACCOUNT_DETAILS)
            intent.putExtra(Constants.TRANSACTIONID, txHistoryList[0].transactionId)
            intent.putExtra(Constants.SERVICE_NAME, txHistoryList[0].serviceName)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}