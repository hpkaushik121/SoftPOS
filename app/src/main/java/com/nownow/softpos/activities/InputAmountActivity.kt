package com.nownow.softpos.activities


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.nownow.softpos.BuildConfig
import com.nownow.softpos.R
import com.nownow.softpos.api.ApiCalls
import com.nownow.softpos.databinding.ActivityInputAmountBinding
import com.nownow.softpos.models.softpos.*
import com.nownow.softpos.network.ServiceBuilderV2
import com.nownow.softpos.utils.*
import com.nownow.softpos.utils.UtilHandler.getDeviceId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class InputAmountActivity : BottomSheetDialogFragment(), OnClickListener {
    private lateinit var binding: ActivityInputAmountBinding
    private var isFormatting: Boolean = false
    var mContext: Context? = null
    private var transactionType: String? = null
    private var validateInfo: TransactionInfo? = null
    private var inputAmount: String = "0"

    override fun getTheme(): Int {
        return R.style.FullScreenBottomSheet
    }

    companion object {
        fun newInstance(transactionType: String): InputAmountActivity {
            val fragment = InputAmountActivity()
            val args = Bundle()
            args.putString(Constants.TRANSACTION_TYPE, transactionType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityInputAmountBinding.inflate(inflater, container, false)
        mContext = requireActivity()
        transactionType = arguments?.getString(Constants.TRANSACTION_TYPE)
        upDateUI()
        setClickListeners()


        return binding!!.root
    }

    fun setEditTextValue(value: String) {
        binding.edittextInputAmount.setText(value)
    }

    @SuppressLint("SetTextI18n")
    private fun upDateUI() {
        disableProceedButton()
        // binding.textMinimumAmount.text = getString(R.string.nfc_min_amount_string) + SharedPrefUtils.minTrnAmount

        setTransactionLimits()


        val textWatcher = object : TextWatcher {
            private var current: String? = null
            private val numberFormat: NumberFormat =
                NumberFormat.getNumberInstance(Locale.getDefault())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val textLength = s?.length ?: 0
                    val filter = InputFilter { source, start, end, _, _, _ ->
                        for (i in start until end) {
                            if (Character.isWhitespace(source[i]) || source[i] == ' ' || source[i] == '-') {
                                return@InputFilter ""
                            }
                        }
                        null
                    }

                    binding.edittextInputAmount.filters = arrayOf(filter)
                    val hasTextFirst1 = binding.edittextInputAmount.text?.isNotEmpty()
                    inputAmount =
                        binding.edittextInputAmount.text.toString().replace(",".toRegex(), "")

                    if (hasTextFirst1 == true) {
                        //binding.btnProceed.isEnabled = true
                        if (transactionType != null) {
                            when (transactionType) {
                                Constants.TRANSACTION_TYPE_CONTACT_LESS -> {
                                    if (inputAmount.toDouble() >= SharedPrefUtils.minTrnAmount.toString()
                                            .toDouble() && inputAmount.toDouble() <= SharedPrefUtils.maxTrnAmount.toString()
                                            .toDouble()
                                    ) {
                                        enableProceedButton()
                                    } else {
                                        disableProceedButton()
                                    }

                                }
                                Constants.TRANSACTION_TYPE_PAY_WITH_TRANSFER -> {

                                    if (inputAmount.toDouble() >= SharedPrefUtils.PWT_MIN.toString()
                                            .toDouble() && inputAmount.toDouble() <= SharedPrefUtils.PWT_MAX.toString()
                                            .toDouble()
                                    ) {
                                        enableProceedButton()
                                    } else {
                                        disableProceedButton()
                                    }


                                }
                                Constants.TRANSACTION_TYPE_QR -> {

                                }
                            }
                        }


                    } else {
                        disableProceedButton()

                        //binding.textTermsOfUse.alpha - 0.5f
                    }
                } catch (e: Exception) {
                    if (!BuildConfig.USE_PRODUCTION_URLS)
                        Log.d("Exception", e.message.toString())
                }

            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) {
                    // Prevent infinite loop
                    return
                }

                val userInput = s.toString().replace(",".toRegex(), "")
                if (userInput.isNotEmpty() && userInput != current && userInput != ".") {
                    isFormatting = true
                    val formattedString = decimalFormat.format(userInput.toLong())
                    current = formattedString
                    binding.edittextInputAmount.setText(formattedString)
                    binding.edittextInputAmount.setSelection(formattedString.length)
                    isFormatting = false
                }
                if (s?.length ?: 0 > 11) {
                    binding.edittextInputAmount.setText(s?.subSequence(0, 11))
                    binding.edittextInputAmount.setSelection(11)
                }

            }
        }
        binding.edittextInputAmount.addTextChangedListener(textWatcher)
    }

    private fun enableProceedButton() {
        binding.proceedTxt.isEnabled = true
        binding.proceedTxt.isClickable = true
        binding.proceedTxt.alpha = 1f
    }

    private fun disableProceedButton() {
        binding.proceedTxt.isEnabled = false
        binding.proceedTxt.isClickable = false
        binding.proceedTxt.alpha = 0.5f
    }

    private fun setTransactionLimits() {
        if (transactionType != null) {
            when (transactionType) {
                Constants.TRANSACTION_TYPE_CONTACT_LESS -> {
                    binding.txtMaxAmount.text =
                        getString(R.string.nfc_max_amount_string) + SharedPrefUtils.maxTrnAmount
                    binding.txtMinAmount.text =
                        getString(R.string.nfc_min_amount_string) + SharedPrefUtils.minTrnAmount

                }
                Constants.TRANSACTION_TYPE_PAY_WITH_TRANSFER -> {
                    binding.txtMaxAmount.text =
                        getString(R.string.nfc_max_amount_string) + SharedPrefUtils.PWT_MAX
                    binding.txtMinAmount.text =
                        getString(R.string.nfc_min_amount_string) + SharedPrefUtils.PWT_MIN

                }
                Constants.TRANSACTION_TYPE_QR -> {

                }
            }
        }

    }

    val decimalFormat = DecimalFormat("#,###").apply {
        maximumFractionDigits = 0
        decimalFormatSymbols = DecimalFormatSymbols.getInstance().apply {
            groupingSeparator = ','
        }
    }

    private fun setClickListeners() {
        binding.proceedTxt.setOnClickListener(this)
        binding.textN5000.setOnClickListener(this)
        binding.textN7500.setOnClickListener(this)
        binding.textN10000.setOnClickListener(this)
        binding.textN25000.setOnClickListener(this)
        binding.textN50000.setOnClickListener(this)
        binding.textN100000.setOnClickListener(this)
        binding.closeInputAmountDialog.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.proceedTxt -> openCardReader()
            R.id.closeInputAmountDialog -> cancelInputAmountDialog()
            R.id.text_n5000 -> {
                setEditTextValue("5000")
            }
            R.id.text_n7500 -> {
                setEditTextValue("7500")
            }
            R.id.text_n10000 -> {
                setEditTextValue("10000")
            }
            R.id.text_n25000 -> {
                setEditTextValue("25000")
            }
            R.id.text_n50000 -> {
                setEditTextValue("50000")
            }
            R.id.text_n100000 -> {
                setEditTextValue("100000")
            }
            //else-> navigateToProfile()
        }
    }

    private fun cancelInputAmountDialog() {
        dismiss()
    }

    private fun openCardReader() {
        if (UtilHandler.isOnline(requireActivity())) {
            inputAmount = binding.edittextInputAmount.text.toString().replace(",".toRegex(), "")
            try {
                if (transactionType != null) {
                    when (transactionType) {
                        Constants.TRANSACTION_TYPE_CONTACT_LESS -> {
                            if (inputAmount.toDouble() <= 0) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.nfc_min_amount_error) + " 0",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (inputAmount.toDouble() > SharedPrefUtils.maxTrnAmount.toString()
                                    .toDouble()
                            ) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.nfc_max_amount_error) + " " + SharedPrefUtils.maxTrnAmount,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (inputAmount.toDouble() < SharedPrefUtils.minTrnAmount.toString()
                                    .toDouble()
                            ) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.nfc_min_amount_error) + " " + SharedPrefUtils.minTrnAmount,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                redirectToNextScreen()
                            }
                        }
                        Constants.TRANSACTION_TYPE_PAY_WITH_TRANSFER -> {
                            if (inputAmount.toDouble() <= 0) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.nfc_min_amount_error) + " 0",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (inputAmount.toDouble() > SharedPrefUtils.PWT_MAX.toString()
                                    .toDouble()
                            ) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.nfc_max_amount_error) + " " + SharedPrefUtils.PWT_MAX,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (inputAmount.toDouble() < SharedPrefUtils.PWT_MIN.toString()
                                    .toDouble()
                            ) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.nfc_min_amount_error) + " " + SharedPrefUtils.PWT_MIN,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                redirectToNextScreen()
                            }
                        }
                        Constants.TRANSACTION_TYPE_QR -> {
                            Toast.makeText(context, "QR", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }


            /*  try {
                  if (inputAmount.toDouble() <= 0) {
                      Toast.makeText(
                          requireActivity(),
                          getString(R.string.nfc_min_amount_error) + " 0",
                          Toast.LENGTH_LONG
                      ).show()
                  } else if (inputAmount.toDouble() > SharedPrefUtils.maxTrnAmount.toString()
                          .toDouble()
                  ) {
                      Toast.makeText(
                          requireActivity(),
                          getString(R.string.nfc_max_amount_error) + " " + SharedPrefUtils.maxTrnAmount,
                          Toast.LENGTH_LONG
                      ).show()
                  } else if (inputAmount.toDouble() < SharedPrefUtils.minTrnAmount.toString()
                          .toDouble()
                  ) {
                      Toast.makeText(
                          requireActivity(),
                          getString(R.string.nfc_min_amount_error) + " " + SharedPrefUtils.minTrnAmount,
                          Toast.LENGTH_LONG
                      ).show()
                  } else {
                      redirectToNextScreen()
                  }
              } catch (e: Exception) {
                  Log.d(Constants.LOG_D_KEY, "" + e.message)
              }*/
        } else {
            mContext?.let {
                UtilHandler.showToast(
                    mContext!!,
                    getString(R.string.internet_error_msg)
                )
            }
        }
    }

    private fun redirectToNextScreen() {
        try {
            if (transactionType != null) {
                when (transactionType) {
                    Constants.TRANSACTION_TYPE_CONTACT_LESS -> {
                        validateTerminal()
                    }
                    Constants.TRANSACTION_TYPE_PAY_WITH_TRANSFER -> {
                        // redirect to  account details page
                        val intent = Intent(requireContext(), AccountDetailsActivity::class.java)
                        intent.putExtra(Constants.BILL_AMOUNT, inputAmount)
                        startActivity(intent)
                        // dismiss()
                    }
                    Constants.TRANSACTION_TYPE_QR -> {
                        Toast.makeText(context, "QR", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {

        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    }

    private fun validateTerminal() {
        try {
            DialogUtils.showDialog(requireActivity(), getString(R.string.progress_dialog_message))
            val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
            val date = UtilHandler.getCurrentDate()
            val transactionInfo =
                TransactionInfo(comments = "this is the device validation request")
            val requestInfo = RequestInfo(
                msisdn = SharedPrefUtils.msisdn,
                requestCts = date,
                requestType = "DeviceValidation",
                serialId = getDeviceId(mContext!!),
                terminalId = getDeviceId(mContext!!),
                transactionInfo = transactionInfo
            )

            val requestModel = CommonQposRequestModel(requestInfo = requestInfo)
            if (!BuildConfig.USE_PRODUCTION_URLS)
                Log.d("Http..../validate", Gson().toJson(requestModel))

            retrofit.validateTerminal(
                SharedPrefUtils.tokenKey,
                Constants.PLATFORM,
                UtilHandler.getPackageVersion(requireActivity()),
                UtilHandler.getScreenDensity(requireActivity()),
                requestModel
            ).enqueue(
                object : Callback<CommonQposResponseModel> {

                    override fun onFailure(call: Call<CommonQposResponseModel>, t: Throwable) {
                        DialogUtils.hideDialog()
                        Log.d(Constants.LOG_D_KEY, t.message.toString())

                        mContext?.let {
                            UtilHandler.showToast(
                                mContext!!,
                                getString(R.string.something_went_wrong)
                            )
                        }
                    }

                    override fun onResponse(
                        call: Call<CommonQposResponseModel>,
                        response: Response<CommonQposResponseModel>
                    ) {
                        DialogUtils.hideDialog()
                        if (response.isSuccessful) {
                            val responseModel: CommonQposResponseModel? = response.body()
                            if (!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http..../validate", Gson().toJson(response.body()))

                            if (responseModel != null) {

                                if (responseModel.responseInfo?.transactionInfo?.resultCode == 0) {
                                    validateInfo = responseModel.responseInfo?.transactionInfo

                                    if (responseModel.responseInfo!!.transactionId!!.length >= 6) {
                                        validateInfo?.stan =
                                            responseModel.responseInfo!!.transactionId?.substring(
                                                responseModel.responseInfo!!.transactionId!!.length - 6
                                            )
                                    } else {
                                        // Add SoftPosConstants clas and uncomment this line
                                        validateInfo?.stan = SoftPosConstants.getRandomUnique(6)
                                    }

                                    validateInfo?.rrn =
                                        responseModel.responseInfo!!.transactionInfo!!.rrn

                                    RechargeInfo(
                                        "",
                                        "",
                                        terminalValidateResponseInfo = responseModel.responseInfo!!
                                    )
                                    if (validateInfo != null) {

                                        mContext?.let {

                                            if (terminalKeysNotAvailable()) {
                                                downloadTerminalData(

                                                    this@InputAmountActivity.validateInfo!!,
                                                    it,
                                                    responseModel.responseInfo!!
                                                )

                                            } else {
                                                downloadTerminalData(

                                                    this@InputAmountActivity.validateInfo!!,
                                                    it,
                                                    responseModel.responseInfo!!
                                                )
                                                /*val intent =
                                                    Intent(
                                                        requireContext(),
                                                        CardReadActivity::class.java
                                                    )
                                                intent.putExtra(
                                                    Constants.VALIDATE_INFO,
                                                    validateInfo
                                                )
                                                intent.putExtra(
                                                    Constants.TERMINAL_VALIDATE_RESPONSE_INFO,
                                                    responseModel.responseInfo!!
                                                )
                                                intent.putExtra(Constants.BILL_AMOUNT, inputAmount)
                                                startActivity(intent)
                                                dismiss()*/
                                            }
                                        }

                                    }
                                } else {
                                    UtilHandler.showSnackBar(
                                        responseModel.responseInfo?.transactionInfo?.resultDesc,
                                        binding.root
                                    )
                                }
                            } else

                                mContext?.let {
                                    UtilHandler.showToast(
                                        mContext!!,
                                        getString(R.string.something_went_wrong)
                                    )
                                }
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_LONG
                            ).show()
                        }


                    }
                }
            )
        } catch (e: Exception) {
            mContext?.let {
                UtilHandler.showToast(
                    mContext!!,
                    getString(R.string.something_went_wrong)
                )
            }
        }
    }

    private fun terminalKeysNotAvailable(): Boolean {
        return SharedPrefUtils.TPK.equals(SharedPrefUtils.Keys.Global.PLAIN_TPK) &&
                SharedPrefUtils.TMK.equals(SharedPrefUtils.Keys.Global.PLAIN_TMK) &&
                SharedPrefUtils.TSK.equals(SharedPrefUtils.Keys.Global.PLAIN_TSK)
    }

    private fun downloadTerminalData(
        validateInfo: TransactionInfo,
        context: Context,
        terminalValidateResponseInfo: ResponseInfo
    ) {
        try {
            if (mContext?.let { UtilHandler.isOnline(it) } == true) {

                val valueMap = HashMap<String, String>()
                valueMap["cTerminalId"] = validateInfo.cTerminalId.toString()
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
                    serialId = getDeviceId(context),
                    terminalId = getDeviceId(context),
                    transactionInfo = transactionInfo
                )

                val requestModel = CommonQposRequestModel(requestInfo = requestInfo)
                DialogUtils.showDialog(
                    context,
                    requireActivity().getString(R.string.progress_dialog_message)
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http.../getTerminalData", Gson().toJson(requestModel))
                val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
                retrofit.downloadTerminalData(
                    SharedPrefUtils.tokenKey,
                    Constants.PLATFORM,
                    UtilHandler.getPackageVersion(requireActivity()),
                    UtilHandler.getScreenDensity(requireActivity()),
                    requestModel
                ).enqueue(
                    object : Callback<CommonQposResponseModel> {
                        override fun onFailure(call: Call<CommonQposResponseModel>, t: Throwable) {
                            DialogUtils.hideDialog()
                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                binding.root
                            )

                        }

                        override fun onResponse(
                            call: Call<CommonQposResponseModel>,
                            response: Response<CommonQposResponseModel>
                        ) {
                            if (response.isSuccessful) {
                                DialogUtils.hideDialog()
                                val commonQposResponseModel = response.body()
                                if (!BuildConfig.USE_PRODUCTION_URLS)
                                    Log.d(
                                        "Http.../getTerminalData",
                                        Gson().toJson(commonQposResponseModel)
                                    )
                                if (commonQposResponseModel != null) {
                                    if (commonQposResponseModel!!.responseInfo!!.transactionInfo!!.vendoreResultCode == 0 && commonQposResponseModel!!.responseInfo!!.transactionInfo!!.resultCode == 0) {

                                        SoftPosConstants.getDataFormat(commonQposResponseModel!!.responseInfo!!.transactionInfo!!.terminalData)

                                        val intent =
                                            Intent(requireContext(), CardReadActivity::class.java)
                                        intent.putExtra(Constants.VALIDATE_INFO, validateInfo)
                                        intent.putExtra(
                                            Constants.TERMINAL_VALIDATE_RESPONSE_INFO,
                                            terminalValidateResponseInfo
                                        )
                                        intent.putExtra(Constants.BILL_AMOUNT, inputAmount)
                                        startActivity(intent)
                                        dismiss()
                                    } else {

                                        DialogUtils.hideDialog()
                                        dismiss()
                                        UtilHandler.showSnackBar(
                                            "Unable to perform Master Key Injection " + commonQposResponseModel.responseInfo!!.transactionInfo!!.resultDesc,
                                            binding.root
                                        )
                                    }

                                }
                            }
                        }


                    }
                )

            }
        } catch (e: Exception) {
            DialogUtils.hideDialog()
            UtilHandler.showSnackBar(getString(R.string.something_went_wrong), binding.root)
        }


    }
    /*private fun downloadTerminalData(
        validateInfo: TransactionInfo,
        context: Context,
        terminalValidateResponseInfo: ResponseInfo) {
        try {
            if (mContext?.let { UtilHandler.isOnline(it) } == true) {

                val apiService = RestApiService()
                val valueMap = HashMap<String, String>()
                valueMap["cTerminalId"] = validateInfo.cTerminalId.toString()
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
                    serialId = getDeviceId(context),
                    terminalId = getDeviceId(context),
                    transactionInfo = transactionInfo
                )

                val requestModel = CommonQposRequestModel(requestInfo = requestInfo)
                DialogUtils.showDialog(context, "Please Wait")

                Log.d("TerminalData_Request", Gson().toJson(requestModel))

                apiService.downloadTerminalData(SharedPrefUtils.tokenKey, requestModel) {
                    Log.d("TerminalData_Response", Gson().toJson(it))
                    if (it != null) {
                        if (it!!.responseInfo!!.transactionInfo!!.vendoreResultCode == 0 &&
                            it!!.responseInfo!!.transactionInfo!!.resultCode == 0
                        ) {

                            SoftPosConstants.getDataFormat(it!!.responseInfo!!.transactionInfo!!.terminalData)
                            DialogUtils.hideDialog()
                            val intent = Intent(requireContext(), CardReadActivity::class.java)
                            intent.putExtra(Constants.CLIENT_ID, "clientID")
                            intent.putExtra(Constants.IS_NFC, true)
                            intent.putExtra(Constants.VALIDATE_INFO, validateInfo)
                            intent.putExtra(
                                Constants.TERMINAL_VALIDATE_RESPONSE_INFO,
                                terminalValidateResponseInfo
                            );
                            intent.putExtra(Constants.BILL_AMOUNT, "200")
                            intent.putExtra(Constants.ACCOUNT_TYPE_SELECTED, "Default")
                            startActivity(intent)
                            dismiss()
                        } else {
                            DialogUtils.hideDialog()
                            UtilHandler.showSnackBar(
                                "Unable to perform Master Key Injection " + it.responseInfo!!.transactionInfo!!.resultDesc,
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
            }else{
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg),binding.root)
            }
        }

        catch (e:Exception){
            DialogUtils.hideDialog()
            UtilHandler.showSnackBar(getString(R.string.something_went_wrong),binding.root)
        }
    }*/


}

