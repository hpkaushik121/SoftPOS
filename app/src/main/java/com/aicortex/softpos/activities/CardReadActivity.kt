package com.aicortex.softpos.activities


import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.creditCardNfcReader.CardNfcAsyncTask
import com.aicortex.softpos.creditCardNfcReader.utils.CardNfcUtils
import com.aicortex.softpos.databinding.ActivityCardReadBinding
import com.aicortex.softpos.models.softpos.*
import com.aicortex.softpos.network.ServiceBuilderV2
import com.aicortex.softpos.services.ISOProcessor
import com.aicortex.softpos.utilities.UtilsHandlerPos
import com.aicortex.softpos.utils.*
import org.jpos.iso.ISOMsg
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class CardReadActivity : BaseActivity(), CardNfcAsyncTask.CardNfcInterface,
    View.OnClickListener {
    private var clientID: String? = ""
    private var isNFC: Boolean = false
    private var amount: String? = ""
    private var cardNumber: String = ""
    private var cardType: String = ""
    private var accountTypeSelected: String? = null


    /*nfc vars*/
    private var accountType: String? = null
    private var mTurnNfcDialog: AlertDialog? = null
    private var mCardNfcAsyncTask: CardNfcAsyncTask? = null
    private var mIntentFromCreate: Boolean = false
    private var mNfcAdapter: NfcAdapter? = null
    private var mCardNfcUtils: CardNfcUtils? = null
    private var mDoNotMoveCardMessage: String? = null
    private var mUnknownEmvCardMessage: String? = null
    private var mCardWithLockedNfcMessage: String? = null
    private var mIsScanNow = false
    var isGoBackEnable = false
    var isCardDetected = false
    var mCardDetails: CardDetails? = null
    private var validateInfo: TransactionInfo? = null
    private var terminalValidateResponseInfo: ResponseInfo? = null
    private lateinit var mBinding: ActivityCardReadBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_card_read)
        amount = intent.getStringExtra(Constants.BILL_AMOUNT)
        accountTypeSelected = intent.extras!!.getString(Constants.ACCOUNT_TYPE_SELECTED)
        setClickListeners()
        upDateUI()
    }

    /*Card Read methods here*/
    private fun setTerminalValidateINfo(responseInfo: ResponseInfo) {
        terminalValidateResponseInfo = responseInfo
    }

    private fun upDateUI() {
        if (amount?.toInt()!! > 0) {
            mBinding.textAmount.text = getString(R.string.naira_icon) + " " + amount.toString()
        }
        Handler().postDelayed({ initReader() }, 1000)
        if (intent.getSerializableExtra(Constants.VALIDATE_INFO) != null) {
            setTransactionInfo(intent.getSerializableExtra(Constants.VALIDATE_INFO) as TransactionInfo)
        }

        if (intent.getSerializableExtra(Constants.TERMINAL_VALIDATE_RESPONSE_INFO) != null) {
            setTerminalValidateINfo(intent.getSerializableExtra(Constants.TERMINAL_VALIDATE_RESPONSE_INFO) as ResponseInfo)
        }
    }

    private fun setClickListeners() {
        mBinding.backIcon.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back_icon -> back()
        }
    }

    private fun back() {
        //val intent = Intent(this, DashboardActivity::class.java)
        //startActivity(intent)
        finish()
    }

    private fun initReader() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mNfcAdapter == null) {
            mBinding.candidatesArea.visibility = View.VISIBLE
        } else {
            mCardNfcUtils = CardNfcUtils(this)
            initNfcMessages()
            mIntentFromCreate = true
            onNewIntent(intent)
        }

        enableGoback(true)
        mBinding.ripple.startRippleAnimation()
        onResume()
    }

    private fun initNfcMessages() {
        mDoNotMoveCardMessage = getString(R.string.snack_doNotMoveCard)
        mCardWithLockedNfcMessage = getString(R.string.snack_lockedNfcCard)
        mUnknownEmvCardMessage = getString(R.string.snack_unknownEmv)
    }

    private fun enableGoback(isVisible: Boolean) {
        isGoBackEnable = isVisible
    }

    override fun onResume() {
        super.onResume()
        mIntentFromCreate = false
        if (mNfcAdapter != null && !mNfcAdapter!!.isEnabled) {
            isNfcEnabled()
            mBinding.candidatesArea.visibility = View.GONE
        } else if (mNfcAdapter != null && mCardNfcUtils != null) {
            if (!mIsScanNow) {
                mBinding.candidatesArea.visibility = View.VISIBLE
            }
            mCardNfcUtils!!.enableDispatch()
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (mNfcAdapter != null && mNfcAdapter!!.isEnabled) {
            mCardNfcAsyncTask = CardNfcAsyncTask.Builder(
                this,
                intent,
                mIntentFromCreate,
                getAmount()
            )
                .build()
        }

    }

    private fun getAmount(): String? {
        amount = intent.getStringExtra(Constants.BILL_AMOUNT)!!
        return amount
        //return  "55";
    }

    private fun getFloorLimit(): String? {
        return SharedPrefUtils.floorLimit.toString()
        //return "50"
    }

    private fun getAccountType(): String? {
        // 00 is default account type which will be used always in stand alone application
        accountType = "00"
        return accountType
    }

    private fun isNfcEnabled(): Boolean {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }
        if (mNfcAdapter != null && mNfcAdapter!!.isEnabled) {
            return true
        } else if (mNfcAdapter != null && !mNfcAdapter!!.isEnabled) {
            DialogUtils.showCommonDialog(2, false, this@CardReadActivity,
                "Please Enable NFC In Setting",
                View.OnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                        startActivity(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(intent)
                    }
                })
            return false
        } else {
            Toast.makeText(this@CardReadActivity, "NFC is not supported", Toast.LENGTH_LONG).show();
            return false
        }
    }

    override fun onPause() {
        super.onPause()
        if (mNfcAdapter != null && mCardNfcUtils != null) {
            mCardNfcUtils!!.disableDispatch()
        }
    }

    override fun onStart() {
        super.onStart()
        mBinding.ripple.startRippleAnimation()
    }


    override fun onStop() {
        super.onStop()
        mBinding.ripple.stopRippleAnimation()
    }

    override fun startNfcReadCard() {
        isCardDetected = true
        mIsScanNow = true
        mBinding.heading.text = getString(R.string.card_detcted)
    }

    override fun cardIsReadyToRead() {

        if (mCardNfcAsyncTask != null) {
            Log.d("Card Data", mCardNfcAsyncTask!!.cardType)
            setCardDetails(mCardNfcAsyncTask!!)
            cardNumber = mCardNfcAsyncTask!!.cardNumber
            cardType = mCardNfcAsyncTask!!.cardType
            if (getFloorLimit()!!.toDouble() <= getAmount()!!.toDouble()) {
                val b = CardDetectedDialog.newInstance(cardType)
                b.show(supportFragmentManager, "Input Amount")

                Handler().postDelayed({
                    b.dismiss()
                    navigateToCardpinActivity()

                }, 1000)

            } else {
                try {
                    mBinding.heading.text = getString(R.string.transaction_in_progress)
                    val validateInfo: TransactionInfo = getTransactionInfo()!!

                    val b = CardDetectedDialog.newInstance(cardType)
                    b.show(supportFragmentManager, "Input Amount")

                    Handler().postDelayed({
                        b.dismiss()
                        performTransaction(validateInfo, getAccountType())

                    }, 1000)

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                mCardNfcAsyncTask = null
                mIsScanNow = false
            }
        } else {
            UtilHandler.showSnackBar(getString(R.string.nfc_card_not_read), mBinding.root)
        }

    }

    private fun navigateToCardpinActivity() {
        val intent = Intent(this, CardPINActivity::class.java)
        intent.putExtra(Constants.CARD_PAN, cardNumber)
        intent.putExtra(Constants.BILL_AMOUNT, getAmount())
        startActivityForResult(intent, 240)
    }

    private fun runPostDelayed() {
        Handler().postDelayed({

            finish()

        }, 3000)
    }

    private fun performTransaction(validateInfo: TransactionInfo, accountType: String?) {
        try {
            if (!UtilHandler.isOnline(this)) {
                mBinding.heading.text = getString(R.string.no_internet_abort_trn)
                runPostDelayed()
                return
            }
            if (mCardDetails == null) {
                UtilHandler.showSnackBar("Could not get card details", mBinding.root)
                return
            }

            showProgressDialog(getString(R.string.progress_dialog_message))
            val terminalValidateResponseInfo: ResponseInfo? = terminalValidateResponseInfo


            val cardData = HashMap<String, String>()
            val map = HashMap<String, String>()
            cardData["realPAN"] = mCardDetails!!.cardNumber.toString()
            //cardData.put("realPAN", "4578060007283110"); //CHANGE HERE TO REAL CARD
            cardData["expiryDate"] = mCardDetails!!.expiryDate
            val amountLocal = getAmount()!!.toInt() * 100
            cardData["amount"] = amountLocal.toString() + ""
            cardData["track2"] = mCardDetails!!.track2
            cardData["cTerminalId"] = validateInfo.cTerminalId.toString()
            map["merchantId"] = SharedPrefUtils.MERCHANTID.toString()
            map["merchantCode"] = SharedPrefUtils.MERCHANTCODE.toString()
            map["merchantName"] = SharedPrefUtils.MERCHANTNAME.toString()
            val isoProcessor = ISOProcessor(cardData, map)
            val isoMsg: ISOMsg =
                isoProcessor.createISOPurchaseRequest(mCardDetails, validateInfo, accountType)
            val result = isoMsg.pack()
            result[35]++
            isoMsg.set(128, ISOProcessor.generateHash256Value(result, SharedPrefUtils.TSK))
            val resultHash = isoMsg.pack()
            val finalResult: ByteArray = ISOProcessor.prepareByteStream(resultHash)
            ISOProcessor.printISOMessage(isoMsg) // tks print fields logs


            val df = DateFormat()
            var date: String = "";

            try {
                date = DateFormat.format("dd-MM-yyyy HH:MM:ss", Date(System.currentTimeMillis()))
                    .toString()
            } catch (ex: Exception) {
                Log.d("Exception", ex.message.toString())
            }

            map["merchantCode"] = map["merchantCode"]!!.substring(0, 4)

            val cardDataRequest = HashMap<String, String>()
            cardDataRequest["maskedPAN"] = UtilsHandlerPos.maskPan(mCardDetails!!.cardNumber)
            //cardDataRequest.put("maskedPAN", UtilHandler.maskPan("4578060007283110"));//REPLACE WITH REAL CARD
            cardDataRequest["cTerminalId"] = validateInfo.cTerminalId.toString()
            cardDataRequest["cardHolderName"] = mCardDetails!!.cardHolderName
            cardDataRequest["mcc"] = map["merchantCode"].toString()


            val isoRequestRequest = IsoRequest(
                nibssRequest = finalResult,
                cardData = cardDataRequest,
            )

            val transactionInfo = TransactionInfo(
                amount = amountLocal,
                stan = isoMsg.getString(11),
                rrn = isoMsg.getString(37),
                authId = validateInfo.authId,
                batchId = validateInfo.batchId,
                invoiceId = validateInfo.invoiceId,
                cardType = Constants.CARD_TYPE,
                isoRequest = isoRequestRequest
            )

            val requestInfo = RequestInfo(
                msisdn = SharedPrefUtils.msisdn,
                transactionId = terminalValidateResponseInfo!!.transactionId,
                requestType = SoftPosConstants.PURCHASE_REQUEST,
                requestCts = date,
                serialId = terminalValidateResponseInfo.serialId,
                terminalId = terminalValidateResponseInfo.terminalId,
                transactionInfo = transactionInfo
            )

            val request = CommonQposRequestModel(requestInfo = requestInfo)
            if (!BuildConfig.USE_PRODUCTION_URLS)
                Log.d("Http..../pay", Gson().toJson(request))

            val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
            retrofit.softPosPay(
                SharedPrefUtils.tokenKey, Constants.PLATFORM,
                UtilHandler.getPackageVersion(this), UtilHandler.getScreenDensity(this), request
            ).enqueue(
                object : Callback<TerminaldataResponseModel> {

                    override fun onResponse(
                        call: Call<TerminaldataResponseModel>,
                        response: Response<TerminaldataResponseModel>
                    ) {
                        hideProgressDialog()
                        if (response.isSuccessful) {
                            val terminalDataResponseModel: TerminaldataResponseModel? =
                                response.body()
                            if (!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http..../pay", Gson().toJson(terminalDataResponseModel))
                            if (terminalDataResponseModel != null && terminalDataResponseModel!!.responseInfo!!.transactionInfo!!.vendoreResultCode == 0) {

                                redirectTopaymentStatusPage(
                                    terminalDataResponseModel,
                                    Constants.PAYMENT_SUCCESS
                                )

                            } else if (terminalDataResponseModel != null && terminalDataResponseModel!!.responseInfo!!.transactionInfo!!.vendoreResultCode == 55) {

                                redirectTopaymentStatusPage(
                                    terminalDataResponseModel,
                                    Constants.PAYMENT_FAILED
                                )
                            } else if (terminalDataResponseModel != null && terminalDataResponseModel!!.responseInfo!!.transactionInfo!!.vendoreResultCode == -101 &&
                                terminalDataResponseModel!!.responseInfo!!.transactionInfo!!.resultCode == 120
                            ) {

                                redirectTopaymentStatusPage(
                                    terminalDataResponseModel,
                                    Constants.PAYMENT_FAILED
                                )

                            } else {
                                UtilHandler.showSnackBar(
                                    terminalDataResponseModel!!.responseInfo!!.transactionInfo!!.vendoreResultDesc.toString(),
                                    mBinding.root
                                )
                                redirectTopaymentStatusPage(
                                    terminalDataResponseModel,
                                    Constants.PAYMENT_FAILED
                                )
                            }

                        } else {
                            if (!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http..../pay", Gson().toJson(response.body()))
                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                mBinding.root
                            )
                        }

                    }

                    override fun onFailure(call: Call<TerminaldataResponseModel>, t: Throwable) {
                        hideProgressDialog()
                        if (!BuildConfig.USE_PRODUCTION_URLS)
                            Log.d("Http..../pay", t.message.toString())
                        UtilHandler.showSnackBar(
                            getString(R.string.something_went_wrong),
                            mBinding.root
                        )
                    }
                })


        } catch (e: Exception) {
            hideProgressDialog()
            Log.d("Exception ", e.message.toString())
            UtilHandler.showSnackBar(e.message.toString(),mBinding.root)
        }
    }

    private fun redirectTopaymentStatusPage(
        terminalDataResponseModel: TerminaldataResponseModel,
        paymentStatus: String
    ) {

        try {
            val intent = Intent(this, SuccessActivity::class.java)
            if (paymentStatus.equals(Constants.PAYMENT_FAILED, ignoreCase = true)) {
                intent.putExtra(Constants.BILL_AMOUNT, getAmount())
            } else {
                intent.putExtra(
                    Constants.BILL_AMOUNT,
                    terminalDataResponseModel.responseInfo?.transactionInfo?.amount.toString()
                )
            }

            intent.putExtra(Constants.PAYMENT_STATUS, paymentStatus)
            intent.putExtra(Constants.REQUEST_FROM, Constants.FROM_PAYMENT_PAGE)
            intent.putExtra(Constants.CARD_TP, cardType)

            intent.putExtra(Constants.MSISDN, terminalDataResponseModel.responseInfo?.msisdn)
            intent.putExtra(
                Constants.TRANSACTIONID,
                terminalDataResponseModel.responseInfo?.transactionId
            )
            intent.putExtra(
                Constants.REMARK,
                terminalDataResponseModel.responseInfo?.transactionInfo?.vendoreResultDesc
            )
            intent.putExtra(
                Constants.RRN,
                terminalDataResponseModel.responseInfo?.transactionInfo?.rrn
            )
            intent.putExtra(
                Constants.STAN,
                terminalDataResponseModel.responseInfo?.transactionInfo?.stan
            )
            intent.putExtra(Constants.CARD_NUMBER, cardNumber)
            intent.putExtra(Constants.SERVICE_TYPE, Constants.SERVICE_CONTACT_LESS)
            startActivity(intent)
            finish()
            } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun cardIsReadyToRead(cardNfcAsyncTask: CardNfcAsyncTask?) {
        if (cardNfcAsyncTask != null) {
            Log.d("Card Data", cardNfcAsyncTask.cardType)
            setCardDetails(cardNfcAsyncTask)
        }
    }

    override fun doNotMoveCardSoFast() {
        mBinding.heading.text = mDoNotMoveCardMessage
    }

    override fun unknownEmvCard() {
        mBinding.heading.text = mUnknownEmvCardMessage
    }

    override fun cardWithLockedNfc() {
        mBinding.heading.text = mCardWithLockedNfcMessage
    }

    override fun finishNfcReadCard() {
        Log.d("Takendra", "Card read successfully")
    }

    private fun getTransactionInfo(): TransactionInfo? {
        return validateInfo
    }

    private fun setTransactionInfo(transactionInfo: TransactionInfo) {
        validateInfo = transactionInfo
    }

    private fun setCardDetails(mCardNfcAsyncTask: CardNfcAsyncTask) {
        try {
            val card = mCardNfcAsyncTask.cardNumber
            //String prettyCardNumber = UtilHandler.getPrettyCardNumber(card);
            val expiredDate = mCardNfcAsyncTask.cardExpireDate
            val cardHolderName = mCardNfcAsyncTask.cardHolderName
            var track2 = mCardNfcAsyncTask.track2
            val len = 36
            if (track2.length < len) {
                var appendedF = ""
                for (i in 0 until len - track2.length) {
                    appendedF = appendedF + "F"
                }
                track2 = track2 + appendedF
            } else if (track2.length > len) {
                track2 = track2.substring(0, len)
            }
            val aid = mCardNfcAsyncTask.aid
            val cardType = mCardNfcAsyncTask.cardType
            val cardDetails = CardDetails(
                aid = aid, cardHolderName = cardHolderName, cardNumber = card,
                cardType = cardType, expiryDate = expiredDate, track2 = track2, pinBlock = "",
                iccData = mCardNfcAsyncTask.iccdata, panSeq = mCardNfcAsyncTask.panSeq
            )
            // cardDetails.setAid(aid)
            //cardDetails.setCardHolderName(cardHolderName)
            //cardDetails.setCardNumber(card)
            //cardDetails.setCardType(cardType)
            //cardDetails.setTrack2(track2)
            //cardDetails.setExpiryDate(expiredDate)
            //cardDetails.setIccData(mCardNfcAsyncTask.iccdata)
            //cardDetails.setPanSeq(mCardNfcAsyncTask.panSeq)
            mCardDetails = cardDetails
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setCardPinBlock(pinBlock: String) {
        mCardDetails!!.pinBlock = pinBlock
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 240 && data != null) {
            mBinding.notice.visibility = View.VISIBLE
            mBinding.heading.text = getString(R.string.transaction_in_progress)
            //val validateInfo: TransactionInfo = getTransactionInfo()
            try {
                if (data.getStringExtra(Constants.CARD_PIN_BLOCK) != null) {
                    setCardPinBlock(data.getStringExtra(Constants.CARD_PIN_BLOCK)!!)
                    if (mCardDetails!!.cardNumber != null) {
                        val validateInfo: TransactionInfo = getTransactionInfo()!!
                        performTransaction(validateInfo, getAccountType())
                    } else
                        UtilHandler.showLongToast(getString(R.string.nfc_card_not_read))
                } else {
                    UtilHandler.showLongToast(getString(R.string.nfc_pin_not_read))
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                UtilHandler.showLongToast("" + e.message)
            }
            mCardNfcAsyncTask = null
            mIsScanNow = false
        } else {
            mBinding.heading.text = getString(R.string.simply_tap)
            UtilHandler.showSnackBar(getString(R.string.transaction_cancelled), mBinding.root)
        }
    }



}