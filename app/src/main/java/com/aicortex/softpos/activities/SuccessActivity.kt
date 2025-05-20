package com.aicortex.softpos.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.databinding.ActivitySuccessBinding
import com.aicortex.softpos.models.core.request.*
import com.aicortex.softpos.models.core.response.BankListModel
import com.aicortex.softpos.models.core.response.CoreResponseModel
import com.aicortex.softpos.models.dashboard.TransactionInfo
import com.aicortex.softpos.models.downloadPdfModels.DownloadPdfRequest
import com.aicortex.softpos.models.downloadPdfModels.DownloadPdfResponse
import com.aicortex.softpos.network.ServiceBuilder
import com.aicortex.softpos.utilities.UtilsHandlerPos
import com.aicortex.softpos.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class SuccessActivity : BaseActivity(), OnClickListener {
    private var isShareScreenShotClicked: Boolean = false
    private var soundPlayed: Boolean = false
    lateinit var binding: ActivitySuccessBinding
    var transactionIdGlobal: String? = null
    var channel: NotificationChannel? = null
    var mBuilder: NotificationCompat.Builder? = null
    private val PERMISSION_REQUEST_CODE = 123
    private var mNotificationManager: NotificationManager? = null
    private val REQUEST_CODE_MANAGE_ALL_FILES_ACCESS = 1
    var textToSpeech: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_success)
        upDateUI()
        /* proceedToDashTxt.setOnClickListener {
             val intent = Intent(this, DashboardActivity::class.java)
             startActivity(intent)
         }*/

    }

    override fun onResume() {
        super.onResume()
        if (isShareScreenShotClicked) {
            isShareScreenShotClicked = false
            upDateUI()
        }
    }

    private fun upDateUI() {
        binding.linearShare.setOnClickListener(this)
        binding.backArrowBtn.setOnClickListener(this)
        binding.proceedToDashTxt.setOnClickListener(this)
        binding.llDownload.setOnClickListener(this)
        val bundle = intent.extras
        if (bundle != null) {
            if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_TX_HISTORY) {
                dataFromTxHistory(bundle)
            } else if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_ACCOUNT_DETAILS) {
                dataFromAccountDetails(bundle)
            } else if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_MOVE_FUND_WALLET) {
                dataFromMoveFundWallet()
            } else {
                dataFromPayment()
            }
            //val clickedData = intent.getParcelableExtra<TransactionInfo>("data")
            //val clickedData2 = bundle.getSerializable("data") as? YourParcelableClass

        }

    }

    private fun dataFromMoveFundWallet() {
        transactionIdGlobal = intent.getStringExtra(Constants.TRANSACTIONID)
        binding.imageForStatus.visibility = View.VISIBLE
        binding.backArrowAndText.visibility = View.GONE
        binding.linearHistoryDetailAmount.visibility = View.GONE
        binding.linearTransactionDetailAmount.visibility = View.VISIBLE
        setTransactionId(intent.getStringExtra(Constants.TRANSACTIONID))
        setAmount(getAmount(), Constants.FROM_MOVE_FUND_WALLET)
        setTransactionStatus(
            intent.getStringExtra(Constants.TRANSACTION_STATUS),
            Constants.FROM_MOVE_FUND_WALLET
        )
        setSenderMsisdn(intent.getStringExtra(Constants.TRANSACTION_SENDER_NUMBER))

        setRemark(intent.getStringExtra(Constants.REMARK))
        setRRN(null)
        setStan(null)
        setCardNumber(null)
        setCardType(null)
        setCurrentDate()
    }

    private fun dataFromPayment() {


        binding.imageForStatus.visibility = View.VISIBLE
        binding.backArrowAndText.visibility = View.GONE
        binding.linearHistoryDetailAmount.visibility = View.GONE
        binding.linearTransactionDetailAmount.visibility = View.VISIBLE
        val txID = intent.getStringExtra(Constants.TRANSACTIONID)
        setCurrentDate()
        if (txID != null) {
            getTxHistoryDetailStatus(txID)
        } else {

            transactionIdGlobal = intent.getStringExtra(Constants.TRANSACTIONID)
            /*binding.imageForStatus.visibility = View.VISIBLE
            binding.backArrowAndText.visibility = View.GONE
            binding.linearHistoryDetailAmount.visibility = View.GONE
            binding.linearTransactionDetailAmount.visibility = View.VISIBLE*/
            val amount = getAmount()
            val paymentStatus = intent.getStringExtra(Constants.PAYMENT_STATUS)
            val receiverMsisdn = intent.getStringExtra(Constants.MSISDN)
            val transactionId = intent.getStringExtra(Constants.TRANSACTIONID)
            val remark = intent.getStringExtra(Constants.REMARK)
            val rrn = intent.getStringExtra(Constants.RRN)
            val stan = intent.getStringExtra(Constants.STAN)
            val cardNumber = intent.getStringExtra(Constants.CARD_NUMBER)
            val cardType = intent.getStringExtra(Constants.CARD_TP)
            val serviceType = intent.getStringExtra(Constants.SERVICE_TYPE)

            setAmount(amount, Constants.FROM_PAYMENT_PAGE)
            setTransactionStatus(paymentStatus, Constants.FROM_PAYMENT_PAGE)
            setTransactionId(transactionId)
            setRemark(remark)
            setRRN(rrn)
            setStan(stan)
            setCardNumber(cardNumber)
            setCardType(cardType)
            //setCurrentDate()
            setReceiverMsisdn(receiverMsisdn)
            setServiceType(serviceType)

        }
    }

    private fun getAmount() = intent.getStringExtra(Constants.BILL_AMOUNT)

    private fun setCardType(cardType: String?) {
        if (cardType?.contains("MASTER") == true) {
            binding.idMasterCardImg.setImageDrawable(getDrawable(R.drawable.ic_mastercard))
        } else if (cardType?.contains("VISA") == true) {
            binding.idMasterCardImg.setImageDrawable(getDrawable(R.drawable.ic_visa))
        }
    }

    private fun setCardNumber(cardNumber: String?) {
        binding.llCardNumber.visibility = View.GONE
        cardNumber?.let {
            binding.llCardNumber.visibility = View.VISIBLE
            binding.textCardNumberValue.text = UtilsHandlerPos.maskPan(cardNumber)
        }

    }

    private fun setStan(stan: String?) {
        binding.llStan.visibility = View.GONE
        stan?.let {
            binding.llStan.visibility = View.VISIBLE
            binding.textStanValue.text = stan
        }
    }

    private fun setRRN(rrn: String?) {
        binding.llRrn.visibility = View.GONE
        rrn?.let {
            binding.llRrn.visibility = View.VISIBLE
            binding.textRRNValue.text = rrn
        }
    }

    private fun setRemark(remark: String?) {
        binding.llRemark.visibility = View.GONE
        remark?.let {
            binding.llRemark.visibility = View.VISIBLE
            binding.textRemarkValue.text = remark
        }
    }

    private fun setTransactionId(transactionId: String?) {
        binding.llTransactionId.visibility = View.GONE
        transactionId?.let {
            binding.llTransactionId.visibility = View.VISIBLE
            binding.textTransactionIDValue.text = transactionId
        }
    }

    private fun setTransactionFees(transactionFees: String?) {
        binding.llTransactionFee.visibility = View.GONE
        transactionFees?.let {
            binding.llTransactionFee.visibility = View.VISIBLE
            binding.textTransactionFeesValue.text = getString(R.string.naira_icon) + it
        }
    }

    private fun setSenderMsisdn(senderMsisdn: String?) {
        binding.llSenderNumber.visibility = View.GONE
        senderMsisdn?.let {
            binding.llSenderNumber.visibility = View.VISIBLE
            binding.textSenderNumberValue.text = senderMsisdn
            if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_ACCOUNT_DETAILS || intent.getStringExtra(
                    Constants.SERVICE_NAME
                ) == Constants.SERVICE_NAME_REPLACE || intent.getStringExtra(Constants.SERVICE_NAME) == "PWT"
            )
                binding.textSenderNumber.text = "Customer Account Number"
        }
    }

    private fun setReceiverMsisdn(receiverMsisdn: String?) {
        binding.llReceiverNumber.visibility = View.GONE
        receiverMsisdn?.let {
            binding.llReceiverNumber.visibility = View.VISIBLE
            binding.textReceiverNumberValue.text = receiverMsisdn
        }
    }

    private fun setReceiverName(receiverName: String?) {
        binding.llReceiverName.visibility = View.GONE
        receiverName?.let {
            binding.llReceiverName.visibility = View.VISIBLE
            binding.textReceiverNameValue.text = it
        }
    }

    private fun setTransactionStatus(paymentStatus: String?, fromPage: String) {
        if (fromPage == Constants.FROM_TX_HISTORY) {
            paymentStatus?.let {
                when (paymentStatus) {
                    "SUCCESS" -> {
                        binding.textStatusTxHistory.text = "Completed"
                        binding.textStatusTxHistory.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.green_2
                            )
                        )
                        binding.textStatusTxHistory.background = ContextCompat.getDrawable(
                            this,
                            R.drawable.tx_history_inner_design_light_green
                        )
                       // showDownLoadIcon()
                    }
                    "FAILED" -> {
                        binding.textStatusTxHistory.background = ContextCompat.getDrawable(
                            this,
                            R.drawable.tx_history_inner_design_for_failed
                        )
                        binding.textStatusTxHistory.text = "Failed"
                        binding.textStatusTxHistory.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.nfc_not_text_color
                            )
                        )
                    }
                    else -> {
                        binding.textStatusTxHistory.background = ContextCompat.getDrawable(
                            this,
                            R.drawable.tx_history_inner_design_for_pending
                        )
                        binding.textStatusTxHistory.text = "Pending"
                        binding.textStatusTxHistory.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.pending_text_color
                            )
                        )
                    }
                }
            }
        } else if (fromPage == Constants.FROM_MOVE_FUND_WALLET) {
            if (paymentStatus.equals(Constants.PAYMENT_SUCCESS, true)) {
                binding.imageForStatus.setImageResource(R.drawable.ic_success)
                binding.textStatusPaymentDetail.text = getString(R.string.transfer_successful)
                //  showDownLoadIcon()

            } else {
                binding.imageForStatus.setImageResource(R.drawable.not_supported3)
                binding.textStatusPaymentDetail.text = getString(R.string.transfer_failed)
            }
        } else if (fromPage == Constants.FROM_ACCOUNT_DETAILS) {

            if (paymentStatus.equals(Constants.PAYMENT_SUCCESS, true)) {
                binding.imageForStatus.setImageResource(R.drawable.ic_success)
                binding.textStatusPaymentDetail.text = getString(R.string.transfer_successful)
                //showDownLoadIcon()
            } else {
                binding.imageForStatus.setImageResource(R.drawable.not_supported3)
                binding.textStatusPaymentDetail.text = getString(R.string.transfer_failed)
            }

        } else {
            if (paymentStatus.equals(Constants.PAYMENT_SUCCESS, true)) {
                binding.imageForStatus.setImageResource(R.drawable.ic_success)
                binding.textStatusPaymentDetail.text = getString(R.string.payment_success)
               // showDownLoadIcon()
            } else {
                binding.imageForStatus.setImageResource(R.drawable.not_supported3)
                binding.textStatusPaymentDetail.text = getString(R.string.payment_failed)
                binding.textStatusPaymentDetail.setTextColor(resources.getColor(R.color.otp_status_text_color_wrong))

            }
        }
    }

    private fun onSoundAlert(msgForSoundAlert: String?, mContext: Context) {
        textToSpeech = TextToSpeech(mContext.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech!!.language = Locale.ENGLISH
                textToSpeech!!.setSpeechRate(1.0f)
                onTextToSpeech(msgForSoundAlert)
            }
            //                if(status!=TextToSpeech.ERROR){
            //                    textToSpeech.setLanguage(Locale.UK);
            //                    textToSpeech.setSpeechRate(0.5f);
            //
            //                }
        }
    }

    private fun onTextToSpeech(msgTextToSpeech: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech?.speak(msgTextToSpeech, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            textToSpeech?.speak(msgTextToSpeech, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    private fun showDownLoadIcon() {
        binding.llDownload.visibility = View.VISIBLE
    }

    private fun getBankName(bin: String?): String? {
        try {
            val bankNameJsonResponse = SharedPrefUtils.bankListData

            if (bankNameJsonResponse != null && bankNameJsonResponse != "") {

                val bankListModel =
                    Gson().fromJson(bankNameJsonResponse.toString(), BankListModel::class.java)
                val bankList = bankListModel.object_
                if (bankList != null) {
                    for (bankListData in bankList) {
                        if (bankListData.bin == bin)
                            return bankListData.bankName
                    }
                    return null
                } else {
                    return null
                }


            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    private fun setBankName(bankName: String?) {
        binding.llBankName.visibility = View.GONE
        bankName?.let {
            binding.llBankName.visibility = View.VISIBLE
            binding.textBankNameValue.text = it
        }
    }

    private fun setAmount(amount: String?, fromPage: String) {

        amount?.let {
            when (fromPage) {
                Constants.FROM_TX_HISTORY -> {
                    binding.textAmountTxHistory.text =
                        "Amount: " + getString(R.string.naira_icon) + UtilHandler.formatWalletBalance(
                            amount
                        )
                }
                Constants.FROM_MOVE_FUND_WALLET -> {
                    binding.textAmountPaidInPayment.text =
                        "Amount: " + getString(R.string.naira_icon) + UtilHandler.formatWalletBalance(
                            amount
                        )
                }
                else -> {
                    binding.textAmountPaidInPayment.text =
                        "Amount: " + getString(R.string.naira_icon) + UtilHandler.formatWalletBalance(
                            amount
                        )
                }
            }

        }
    }

    private fun setReceiverNameTitle(senderName: String?) {
        binding.textUserNameTxHistory.visibility = View.GONE
        senderName?.let {
            binding.textUserNameTxHistory.visibility = View.VISIBLE
            binding.textUserNameTxHistory.text = it
        }
    }

    private fun setSenderName(senderName: String?) {
        binding.llSenderName.visibility = View.GONE
        senderName?.let {
            binding.llSenderName.visibility = View.VISIBLE
            binding.textSenderNameValue.text = it
            if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_ACCOUNT_DETAILS || intent.getStringExtra(
                    Constants.SERVICE_NAME
                ) == Constants.SERVICE_NAME_REPLACE || intent.getStringExtra(Constants.SERVICE_NAME) == "PWT"
            )
                binding.textSenderName.text = "Account Name"
        }
    }

    private fun setCurrentDate() {
// Get current date and format it
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy | h:mma")
        val formattedDate = dateFormat.format(currentDate)
        binding.textDateTimeOfPayment.text = formattedDate
    }

    private fun setServiceType(serviceType: String?) {
        binding.llServiceType.visibility = View.GONE
        serviceType?.let {
            binding.llServiceType.visibility = View.VISIBLE
            if (it == "PWT" || it == Constants.SERVICE_NAME_REPLACE) {
                binding.textServiceTypeValue.text = getString(R.string.pay_with_bank_transfer)
                binding.textServiceTypeValue.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.pwt_service_color
                    )
                )
            } else
                binding.textServiceTypeValue.text = it
        }
    }

    private fun dataFromTxHistory(bundle: Bundle) {
        binding.imageForStatus.visibility = View.GONE
        binding.layoutOfHeader.visibility = View.VISIBLE
        binding.linearHistoryDetailAmount.visibility = View.VISIBLE
        binding.linearTransactionDetailAmount.visibility = View.GONE
        val data = bundle.getParcelable<TransactionInfo>("data")
        val txID = intent.getStringExtra(Constants.TRANSACTIONID)
        if (txID != null) {
            getTxHistoryDetailStatus(txID)
        }
    }

    private fun dataFromAccountDetails(bundle: Bundle) {
        binding.layoutOfHeader.visibility = View.VISIBLE
        binding.linearHistoryDetailAmount.visibility = View.GONE
        binding.linearTransactionDetailAmount.visibility = View.VISIBLE
        setCurrentDate()
        setCardNumber(null)
        val txID = intent.getStringExtra(Constants.TRANSACTIONID)
        if (txID != null) {
            getTxHistoryDetailStatus(txID)
        }
    }

    /*get tx history details using status API*/
    private fun getTxHistoryDetailStatus(transactionId: String) {
        try {
            if (UtilHandler.isOnline(this)) {
                showProgressDialog(getString(R.string.progress_dialog_message))
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
                        language = "ENG",
                        transactionId = transactionId
                    )

                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS) {
                    Log.d("http...transactionManagement/status", Gson().toJson(coreServiceRequest))
                }
                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.getTxHistoryFullStatus(
                    SharedPrefUtils.tokenKey, Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this), UtilHandler.getScreenDensity(this),
                    coreServiceRequest
                )
                    .enqueue(object : Callback<CoreResponseModel> {
                        @SuppressLint("LongLogTag")
                        override fun onResponse(
                            call: Call<CoreResponseModel>,
                            response: Response<CoreResponseModel>
                        ) {

                            if (response.isSuccessful) {
                                hideProgressDialog()
                                val mCoreResponseModel: CoreResponseModel? = response.body()
                                if (!BuildConfig.USE_PRODUCTION_URLS) {
                                    Log.d(
                                        "http...transactionManagement/status",
                                        Gson().toJson(mCoreResponseModel)
                                    )
                                }
                                if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status != null) {
                                    if (mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.status == "Success"
                                    ) {
                                        if (mCoreResponseModel.mfsResponseInfo != null) {
                                            /*Note:- Null check is implemented in below fun call so no need to do here*/
                                            setAmount(
                                                mCoreResponseModel.mfsResponseInfo?.transactionAmount,
                                                intent.getStringExtra(Constants.REQUEST_FROM)
                                                    .toString()
                                            )
                                            setTransactionStatus(
                                                mCoreResponseModel.mfsResponseInfo.transactionStatus,
                                                intent.getStringExtra(Constants.REQUEST_FROM)
                                                    .toString()
                                            )
                                            playSound(
                                                mCoreResponseModel.mfsResponseInfo.transactionStatus,
                                                intent.getStringExtra(Constants.REQUEST_FROM)
                                                    .toString(),
                                                mCoreResponseModel.mfsResponseInfo?.transactionAmount
                                            )
                                            setReceiverNameTitle(mCoreResponseModel.mfsResponseInfo.senderName)
                                            setTransactionId(mCoreResponseModel.mfsResponseInfo.transactionId)
                                            if (mCoreResponseModel.mfsResponseInfo?.transactionFees != null && mCoreResponseModel.mfsResponseInfo?.transactionFees != "null") {
                                                setTransactionFees(mCoreResponseModel.mfsResponseInfo?.transactionFees)
                                            }

                                            if (intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_ACCOUNT_DETAILS ||
                                                intent.getStringExtra(Constants.REQUEST_FROM) == Constants.FROM_TX_HISTORY
                                            ) {
                                                /**in case of contactless transaction this setRemark will  be replaced by statusMessage in optional info*/
                                                setRemark(mCoreResponseModel.mfsResponseInfo.transactionStatus)
                                            }


                                            if (mCoreResponseModel.mfsOptionalInfo?.info != null && mCoreResponseModel.mfsOptionalInfo?.info.isNotEmpty()) {

                                                for (items in mCoreResponseModel.mfsOptionalInfo?.info) {
                                                    when (items.key) {
                                                        "statusMessage" -> {
                                                            setRemark(items.value)
                                                        }
                                                        "pan" -> {
                                                            setCardNumber(items.value)
                                                            setCardType(
                                                                intent.getStringExtra(
                                                                    Constants.CARD_TP
                                                                )
                                                            )
                                                        }
                                                        "originatorAccountNumber" -> {
                                                            setSenderMsisdn(items.value)
                                                        }
                                                        "originatorAccountName" -> {
                                                            setSenderName(items.value)

                                                        }
                                                        "originatorBin" -> {
                                                            setBankName(getBankName(items.value))
                                                        }
                                                    }
                                                }
                                            }
                                            if (mCoreResponseModel.mfsResponseInfo.receiverMsisdn == null || TextUtils.isEmpty(
                                                    mCoreResponseModel.mfsResponseInfo.receiverMsisdn
                                                )
                                            ) {
                                                setReceiverMsisdn(null)
                                            } else {
                                                setReceiverMsisdn(mCoreResponseModel.mfsResponseInfo.receiverMsisdn)
                                            }

                                            if (mCoreResponseModel.mfsResponseInfo.receiverName == null || TextUtils.isEmpty(
                                                    mCoreResponseModel.mfsResponseInfo.receiverName
                                                )
                                            ) {
                                                setReceiverName(null)
                                            } else {
                                                setReceiverName(
                                                    UtilHandler.capitalizeFirstLetter(
                                                        mCoreResponseModel.mfsResponseInfo.receiverName
                                                    )
                                                )
                                            }


                                            if (intent.getStringExtra(Constants.SERVICE_NAME) != null && intent.getStringExtra(
                                                    Constants.SERVICE_NAME
                                                ) != ""
                                            ) {
                                                setServiceType(intent.getStringExtra(Constants.SERVICE_NAME))
                                            } else {
                                                setServiceType(intent.getStringExtra(Constants.SERVICE_TYPE))
                                            }
                                            transactionIdGlobal =
                                                mCoreResponseModel.mfsResponseInfo.transactionId
                                            if (mCoreResponseModel.mfsResponseInfo.transactionDate != null) {
                                                binding.textDateTxHistory.text =
                                                    UtilHandler.changeDateFormat(
                                                        mCoreResponseModel.mfsResponseInfo.transactionDate,
                                                        Constants.FROM_TX_DETAIL_PAGE
                                                    )
                                            }
                                            binding.textReceiverNumberValue.text =
                                                mCoreResponseModel.mfsResponseInfo.receiverMsisdn
                                            var info = mCoreResponseModel.mfsOptionalInfo?.info
                                            if (info != null && info.size > 0) {
                                                for (item in info) {
                                                    when (item.key) {
                                                        "rrn" -> {
                                                            setRRN(item.value)
                                                        }
                                                        "stan" -> {
                                                            setStan(item.value)
                                                        }
                                                    }
                                                }
                                            } else {
                                                setRRN(null)
                                                setStan(null)
                                            }
                                        }


                                    } else {
                                        hideProgressDialog()
                                        UtilHandler.showSnackBar(
                                            mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.errorDescription,
                                            binding.root
                                        )
                                    }
                                } else {
                                    hideProgressDialog()
                                    UtilHandler.showSnackBar(
                                        getString(R.string.something_went_wrong),
                                        binding.root
                                    )
                                }

                            } else {
                                hideProgressDialog()
                                UtilHandler.showSnackBar(
                                    getString(R.string.something_went_wrong),
                                    findViewById(R.id.root_layout)
                                )
                            }
                        }

                        override fun onFailure(call: Call<CoreResponseModel>, t: Throwable) {
                            hideProgressDialog()
                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                findViewById(R.id.root_layout)
                            )
                        }
                    })

            } else {
                UtilHandler.showSnackBar(
                    getString(R.string.internet_error_msg),
                    findViewById(R.id.root_layout)
                )
            }
        } catch (e: Exception) {
            hideProgressDialog()
            UtilHandler.showSnackBar(
                getString(R.string.something_went_wrong),
                findViewById(R.id.root_layout)
            )
        }
    }

    private fun playSound(
        transactionStatus: String?,
        requestFrom: String,
        transactionAmount: String?
    ) {
        if (!isShareScreenShotClicked && !soundPlayed) {
            if (requestFrom == Constants.FROM_ACCOUNT_DETAILS || requestFrom == Constants.FROM_PAYMENT_PAGE) {
                if (transactionStatus != null && transactionStatus.equals(
                        Constants.PAYMENT_SUCCESS,
                        true
                    ) && transactionAmount != null && transactionAmount != ""
                ) {
                    soundPlayed = true
                    onSoundAlert(
                        "The amount of naira $transactionAmount received successfully",
                        this
                    )
                }
            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.linearShare -> takeScreenShot()
            R.id.backArrowBtn -> onBackPressed()
            R.id.proceedToDashTxt -> backToDashboard()
            R.id.llDownload -> downloadPdf()

        }
    }

    private fun downloadPdf() {
        try {
            if (UtilHandler.isOnline(this)) {
                showProgressDialog(getString(R.string.progress_dialog_message))
                val downloadPdfRequest = DownloadPdfRequest(
                    email_id = SharedPrefUtils.email,
                    entityId = SharedPrefUtils.entityId,
                    msisdn = SharedPrefUtils.msisdn,
                    transactionId = transactionIdGlobal,
                    username = SharedPrefUtils.UserFirstName

                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http....getSingleStatement", Gson().toJson(downloadPdfRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.downloadTransactionDetails(
                    SharedPrefUtils.tokenKey, Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this), UtilHandler.getScreenDensity(this),
                    downloadPdfRequest
                )
                    .enqueue(
                        object : Callback<DownloadPdfResponse> {
                            override fun onResponse(
                                call: Call<DownloadPdfResponse>,
                                response: Response<DownloadPdfResponse>
                            ) {
                                DialogUtils.hideDialog()
                                if (response.isSuccessful) {
                                    DialogUtils.hideDialog()
                                    val downloadPdfResponse: DownloadPdfResponse? =
                                        response.body()
                                    if (!BuildConfig.USE_PRODUCTION_URLS)
                                        Log.d(
                                            "Http....getSingleStatement",
                                            Gson().toJson(downloadPdfResponse)
                                        )

                                    if (downloadPdfResponse?.status != null) {
                                        if (downloadPdfResponse.status) {
                                            downloadPdfFile(downloadPdfResponse)
                                            //UtilHandler.showSnackBar(downloadPdfResponse.message,binding.root)
                                        } else {
                                            DialogUtils.hideDialog()
                                            UtilHandler.showSnackBar(
                                                downloadPdfResponse?.message,
                                                binding.root
                                            )
                                        }
                                    } else {
                                        DialogUtils.hideDialog()
                                        UtilHandler.showSnackBar(
                                            getString(R.string.something_went_wrong),
                                            binding.root
                                        )
                                    }


                                } else {
                                    DialogUtils.hideDialog()
                                    UtilHandler.showSnackBar(
                                        getString(R.string.something_went_wrong),
                                        binding.root
                                    )

                                }

                            }

                            override fun onFailure(
                                call: Call<DownloadPdfResponse>,
                                t: Throwable
                            ) {
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

    private fun backToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }

    private fun takeScreenShot() {
        val date = Date()
        val format = DateFormat.format("MM-dd-yyyy_hh:mm:ss", date)
        try {
            val mainDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "aicortex")
            if (!mainDir.exists()) {
                val mkdir = mainDir.mkdir()
            }
            val path = "$mainDir/aicortex-$format.jpeg"

            /* binding.getRoot().setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(getWholeListViewItemsToBitmap());
            binding.getRoot().setDrawingCacheEnabled(false);*/
            isShareScreenShotClicked = true
            binding.screenShotView.setDrawingCacheEnabled(true)
            var bitmap = Bitmap.createBitmap(getWholeListViewItemsToBitmap())
            //var bitmap: Bitmap? =null
            /* binding.screenShotView.buildDrawingCache()
             bitmap = if(binding.screenShotView.getDrawingCache()==null) {
                 loadLargeBitmapFromView(binding.screenShotView)
             } else {
                // Bitmap.createBitmap(binding.screenShotView.getDrawingCache())
                 loadLargeBitmapFromView(binding.screenShotView)
             }*/

            binding.screenShotView.setDrawingCacheEnabled(false)
            val imageFile = File(path)
            val fileOutputStream = FileOutputStream(imageFile)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            shareScreenShot(imageFile)
        } catch (e: Exception) {
            UtilHandler.showSnackBar(getString(R.string.something_went_wrong), binding.root)
            //UtilHandler.showSnackBar(e.toString(), binding.root)
        }
    }

    fun getWholeListViewItemsToBitmap(): Bitmap {
        val itemscount: Int = binding.screenShotView.childCount
        var allitemsheight = 0
        val bmps: MutableList<Bitmap> = ArrayList()
        var childView: View
        for (i in 0 until itemscount) {
            childView = binding.screenShotView.getChildAt(i)
            childView.isDrawingCacheEnabled = false
            childView.measure(
                View.MeasureSpec.makeMeasureSpec(
                    binding.screenShotView.width,
                    View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            childView.layout(0, 0, childView.measuredWidth, childView.measuredHeight)
            childView.isDrawingCacheEnabled = true
            childView.buildDrawingCache()
            if (childView.drawingCache != null) {
                bmps.add(childView.drawingCache)
            }
            allitemsheight += childView.measuredHeight
        }
        val bigbitmap = Bitmap.createBitmap(
            binding.screenShotView.measuredWidth,
            allitemsheight,
            Bitmap.Config.ARGB_8888
        )
        val bigcanvas = Canvas(bigbitmap)
        bigcanvas.drawColor(Color.WHITE)
        val paint = Paint()
        var iHeight = 0
        for (i in bmps.indices) {
            var bmp: Bitmap? = bmps[i]
            bigcanvas.drawBitmap(bmp!!, 0f, iHeight.toFloat(), paint)
            iHeight += bmp.height
            bmp.recycle()
            bmp = null
        }
        return bigbitmap
    }

    private fun loadLargeBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawColor(Color.WHITE)
        v.layout(0, 0, v.layoutParams.width, v.layoutParams.height)
        v.draw(c)
        return b
    }

    /*private fun captureLinearLayoutScreenshot(view: View) :Bitmap? {
        // Create a bitmap with the size of the LinearLayout
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        // Create a Canvas with the bitmap
        val canvas = Canvas(bitmap)

        // Draw the LinearLayout onto the Canvas
        view.draw(canvas)

        // Now you have the screenshot captured in the 'bitmap' variable
        // You can save, share, or display it as needed
        return bitmap
    }*/

    private fun shareScreenShot(imageFile: File) {

        val authority = "${this.packageName}.fileprovider"

        try {
            val uri = FileProvider.getUriForFile(this, authority, imageFile)
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_TEXT, "Transaction Details")
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            try {
                startActivity(Intent.createChooser(intent, "Share With"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@SuccessActivity, "No App Available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            UtilHandler.showSnackBar(e.toString(), binding.root)
        }
    }


    private fun downloadWithDownloadManager(url: String): Boolean {
        try {

            if (!url.startsWith("http")) {
                return false
            }
            val name = getFileName(url)

            val file = File(Environment.getExternalStorageDirectory(), "Download")
            if (!file.exists()) {
                file.mkdirs()
            }
            val result = File(file.absolutePath + File.separator + name)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            request.setDestinationUri(Uri.fromFile(result))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager?.enqueue(request)
            MediaScannerConnection.scanFile(
                this@SuccessActivity, arrayOf(result.toString()), null
            ) { path, uri ->


            }
        } catch (e: java.lang.Exception) {
            Log.e(">>>>>", e.toString())

            return false
        }
        return true
    }

    private fun getFileName(url: String): String {
        val fileExtension = "pdf"
        var downloadFileName = ""
        var filename = ""

        downloadFileName = url!!.substring(url.lastIndexOf('/') + 1)

        val df = SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault())
        filename = "${downloadFileName}_${df.format(Date())}.$fileExtension"
        return filename
    }

    private fun downloadPdfFile(model: DownloadPdfResponse?) {
        //if (!TextUtils.isEmpty(model.link)) { //will change not to yes
        //    return
        //}
        try {
            if (model != null) {
                // Check if the app has the necessary permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    /* if (!Environment.isExternalStorageManager()) {
                         // Request permission from the user
                         val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                         startActivityForResult(intent, REQUEST_CODE_MANAGE_ALL_FILES_ACCESS)
                     } else {
                         // Permission already granted
                         if (TextUtils.isEmpty(model.link)) {
                             UtilHandler.showSnackBar("Link is unavailable", binding.root)
                             //DownloadTask(this,"https://incometaxindia.gov.in/forms/income-tax%20rules/2023/itr6_english.pdf")
                         } else {
                             showNotification("Downloading file", 0)
                             DownloadTask(this, model.link)
                         }
                     }*/
                    if (TextUtils.isEmpty(model.link)) {
                        UtilHandler.showSnackBar(getString(R.string.link_unavailable), binding.root)

                    } else {
                        downloadWithDownloadManager(model.link)
                    }


                } else {
                    // Older Android versions (pre-Android 11)
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission already granted
                        if (TextUtils.isEmpty(model.link)) {
                            UtilHandler.showSnackBar(
                                getString(R.string.link_unavailable),
                                binding.root
                            )
                        } else {
                            showNotification(getString(R.string.download_msg), 0)
                            DownloadTask(this, model.link)
                        }
                    } else {
                        // Request permission from the user
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            REQUEST_CODE_MANAGE_ALL_FILES_ACCESS
                        )
                    }
                }


                //if (checkAndRequestPermissions()) {
                //    showNotification("Downloading file",0)
                //    if (TextUtils.isEmpty(model.link)) {
                //        UtilHandler.showSnackBar("Link is unavailable",binding.root)
                //        //DownloadTask(this,"https://incometaxindia.gov.in/forms/income-tax%20rules/2023/itr6_english.pdf")
                //    }else{
                //        DownloadTask(this,model.link)
                //    }
                //} else {
                //    Toast.makeText(this, "Permission is pending", Toast.LENGTH_SHORT).show()
                //}
            }
        } catch (e: FileNotFoundException) {
            UtilHandler.showSnackBar(e.toString(), binding.root)
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionDeniedList = ArrayList<String>()

        for (permission in permissions) {
            val permissionResult = ContextCompat.checkSelfPermission(this, permission)
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                permissionDeniedList.add(permission)
            }
        }

        return if (permissionDeniedList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    companion object {
        private const val TAG = "Download Task"
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All requested permissions are granted, proceed with the download
                checkAndRequestPermissions()
            } else {
                // Permissions are not granted, show a message or take appropriate action
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class DownloadTask(val context: Context?, private val downloadUrl: String?) {
        private val fileExtension = "pdf"
        private var downloadFileName = ""
        private var filename = ""

        init {
            downloadFileName = downloadUrl!!.substring(downloadUrl.lastIndexOf('/') + 1)
            Log.e(TAG, downloadFileName)
            downloadPdfFile()
        }

        private fun downloadPdfFile() {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val url = URL(downloadUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.e(
                            TAG,
                            "Server returned HTTP ${connection.responseCode} ${connection.responseMessage}"
                        )
                        throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                    }

                    val apkStorage = if (isSDCardPresent()) {
                        File(Environment.getExternalStorageDirectory().toString() + "/Download")
                    } else {
                        File(context?.filesDir, "Download")
                    }

                    if (!apkStorage.exists()) {
                        apkStorage.mkdir()
                        Log.e(TAG, "Directory Created.")
                    }

                    val df = SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault())
                    filename = "${downloadFileName}_${df.format(Date())}.$fileExtension"
                    val outputFile = File(apkStorage, filename)

                    connection.inputStream.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    launch(Dispatchers.Main) {
                        //showNotification("File downloaded", 100)
                        showNotification("File downloaded", 100, null, getFileUri(outputFile))
                        Toast.makeText(context, "Download successful", Toast.LENGTH_SHORT).show()
                        //openDownloadedPdf(outputFile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "Download Error Exception: ${e.message}")
                    launch(Dispatchers.Main) {
                        showNotification("Download failed", 0)
                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun getFileUri(file: File): Uri {
            val authority = "${context?.packageName}.fileprovider"
            return FileProvider.getUriForFile(context!!, authority, file)
        }

        fun openDownloadedPdf(file: File) {
            val apkURI = FileProvider.getUriForFile(
                context!!,
                "${context.applicationContext.packageName}.provider",
                file
            )
            val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
                data = apkURI
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                pdfIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //showNotification("Downloading file",100)
        }

        /*fun showNotification(contentTitle: String, progress: Int, contentIntent: PendingIntent? = null) {
            val channelId = "notify_001"
            val notificationId = 0

            val builder = NotificationCompat.Builder(context!!, channelId)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setProgress(100, progress, false)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(contentTitle)
                .setSound(null)
                .setAutoCancel(true)

            if (contentIntent != null) {
                builder.setContentIntent(contentIntent)
            }

            val notificationManager = NotificationManagerCompat.from(context)
            //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.setSound(null, null)
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(notificationId, builder.build())
        }*/

        private fun isSDCardPresent(): Boolean {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }


    }

    fun showNotification(
        title: String,
        progress: Int,
        contentIntent: PendingIntent? = null,
        fileUri: Uri? = null
    ) {
        val channelId = "Your_channel_id"
        val mBuilder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setProgress(100, progress, false)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setSound(null)
            .setAutoCancel(true)

        if (contentIntent != null) {
            mBuilder.setContentIntent(contentIntent)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }

        if (progress >= 100 && fileUri != null) {
            // Add an action to open the downloaded file in the Download folder
            val openIntent = Intent(Intent.ACTION_VIEW)
            openIntent.data = fileUri
            openIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            var pendingOpenIntent: PendingIntent? = null
            pendingOpenIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    this,
                    0,
                    openIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    this,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
//"Open Downloaded File"
            mBuilder.addAction(
                R.drawable.logo,
                "",
                pendingOpenIntent
            )
            // Cancel the notification when the action is clicked
            pendingOpenIntent?.let {
                it.cancel()
                // notificationManager.cancel(0)
                openDownloadedFile(this, fileUri) // Call the openDownloadedFile function here
                notificationManager.cancel(0)
            }
        }

        notificationManager.notify(0, mBuilder.build())

    }

    private fun openDownloadedFile(context: Context, fileUri: Uri) {
        val openIntent = Intent(Intent.ACTION_VIEW)
        openIntent.data = fileUri
        openIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        val notificationId = 0 // Unique ID for the notification

        val pendingOpenIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        try {
            pendingOpenIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            // Handle the case where the PendingIntent was canceled or unable to send
            Toast.makeText(context, "Failed to open the file", Toast.LENGTH_SHORT).show()
        }
    }


}