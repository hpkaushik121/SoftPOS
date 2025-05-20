package com.aicortex.softpos.activities

import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.adapters.TransactionHistoryAdapter
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.databinding.ActivityDashboardBinding
import com.aicortex.softpos.models.core.request.*
import com.aicortex.softpos.models.core.response.BankListModel
import com.aicortex.softpos.models.dashboard.CommonDashboardResponseBaseModel
import com.aicortex.softpos.models.dashboard.TransactionInfo
import com.aicortex.softpos.models.softpos.*
import com.aicortex.softpos.network.ServiceBuilder
import com.aicortex.softpos.network.ServiceBuilderV2
import com.aicortex.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : BaseActivity(), OnClickListener, OnRefreshListener {
    lateinit var binding: ActivityDashboardBinding
    private var nfcAdapter: NfcAdapter? = null
    private var wallet1Balance: String? = null

    val arrayList = ArrayList<String>()
    private lateinit var myAdapter: TransactionHistoryAdapter
    var userFirstName: String = ""
    var userLastName: String = ""
    var txHistoryList: MutableList<TransactionInfo> = mutableListOf()
    var isTerminalLinked: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        binding.textTodayDate.text = getTodayDateInFormat()
        getDashboardData()
        setClickListeners()
    }

    fun updateUI() {
        if (isNfcEnabled()) {
            if (SharedPrefUtils.isTerminalLinked) {
                var linkedMobileNumber: String? = SharedPrefUtils.msisdn
                binding.linearLinkYourWallet.visibility = View.GONE
                binding.linearNotNfc.visibility = View.GONE
                binding.linearFirstDivider.visibility = View.GONE
                binding.linearSecondDivider.visibility = View.GONE
                binding.textTerminalLinkUnlink.visibility=View.VISIBLE
                binding.textTerminalLinkUnlink.text = HtmlCompat.fromHtml(
                    " Wallet Linked <b>$linkedMobileNumber</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                binding.linearTapAndPay.alpha = 1f
                binding.linearTapAndPay.isEnabled = true
            } else {
                binding.linearLinkYourWallet.visibility = View.VISIBLE
                binding.textTerminalLinkUnlink.visibility=View.GONE
                binding.linearNotNfc.visibility = View.GONE
                binding.linearTapAndPay.alpha = 0.5f
                binding.linearTapAndPay.isEnabled = true
            }
        } else {
            binding.linearTapAndPay.alpha = 0.4f
            binding.linearTapAndPay.isEnabled = false
            binding.linearNotNfc.visibility = View.VISIBLE
            binding.linearLinkYourWallet.visibility = View.GONE
        }

    }

    private fun setClickListeners() {
        binding.profileIcon.setOnClickListener(this)
        binding.linearTapAndPay.setOnClickListener(this)
        binding.linearNotNfc.setOnClickListener(this)
        binding.linearPayWithBank.setOnClickListener(this)
        binding.linearBtnLinkYourWallet.setOnClickListener(this)
        binding.swiperefresh.setOnRefreshListener(this)
        binding.textViewMore.setOnClickListener(this)
        //binding.swiperefresh.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE) // Set custom colors
        binding.swiperefresh.setColorSchemeColors(
            resources.getColor(R.color.color_coming_soon),
            resources.getColor(R.color.green),
            resources.getColor(R.color.green)
        ) // Set custom colors
        binding.swiperefresh.setProgressBackgroundColorSchemeColor(Color.WHITE) // Set background color

    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.profileIcon -> navigateToProfile()
            R.id.linear_tapAndPay -> navigateToPayment()
            R.id.linear_not_nfc -> showTerminalMapDialog()
            R.id.text_ViewMore -> navigateToTxHistory()
            R.id.linear_btnLinkYourWallet -> linkYourWallet()
            R.id.linearPayWithBank -> payWithTransfer()
            R.id.linear_QrCode -> paywithQR()
            //else-> navigateToProfile()

        }
    }

    private fun paywithQR() {
        // here code for Qa payment
    }

    private fun payWithTransfer() {
        val b = InputAmountActivity.newInstance(Constants.TRANSACTION_TYPE_PAY_WITH_TRANSFER)
        b.show(supportFragmentManager, "Input Amount")
    }

    private fun navigateToTxHistory() {
        val intent = Intent(this, TransactionHistoryActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileSettingsActivity::class.java)
        intent.putExtra(Constants.WALLET1_BALANCE_AMOUNT, wallet1Balance)
        startActivity(intent)
    }

    private fun navigateToPayment() {

        if (SharedPrefUtils.isTerminalLinked) {
            val b = InputAmountActivity.newInstance(Constants.TRANSACTION_TYPE_CONTACT_LESS)
            b.show(supportFragmentManager, "Input Amount")
        } else {
            UtilHandler.showSnackBar("Please link your Wallet !", binding.root)
        }
    }

    private fun showTerminalMapDialog() {
        //UtilHandler.showSnackBar("showTerminalMapDialog", binding.root)
    }

    private fun linkYourWallet() {


        val b = MapTerminalDialog.newInstance(
            Constants.DASHBOARD_ACTIVITY,
            SharedPrefUtils.msisdn.toString()
        )
        b.show(supportFragmentManager, "Login Success")
    }

    private fun isNfcEnabled(): Boolean {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }
        if (nfcAdapter != null && nfcAdapter!!.isEnabled) {
            binding.linearTapAndPay.alpha = 1f
            binding.linearTapAndPay.isEnabled = true
            if (SharedPrefUtils.isTerminalLinked) {
                var linkedMobileNumber: String? = SharedPrefUtils.msisdn
                binding.linearLinkYourWallet.visibility = View.GONE
                binding.linearNotNfc.visibility = View.GONE
                binding.linearFirstDivider.visibility = View.GONE
                binding.linearSecondDivider.visibility = View.GONE
                binding.textTerminalLinkUnlink.visibility=View.VISIBLE
                binding.textTerminalLinkUnlink.text = HtmlCompat.fromHtml(
                    " Wallet Linked <b>$linkedMobileNumber</b>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            } else {
                binding.linearLinkYourWallet.visibility = View.VISIBLE
                binding.textTerminalLinkUnlink.visibility=View.GONE
                binding.linearNotNfc.visibility = View.GONE
            }
            return true
        } else if (nfcAdapter != null && !nfcAdapter!!.isEnabled) {
            //Log.d(Constants.LOG_D_RESPONSE, "NFC is not enabled.Need to enable by the user")
            //NFC is not enabled.Need to enable by the user.
            DialogUtils.showCommonDialog(2, false, this@DashboardActivity,
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
            //NFC is not supported
            //Toast.makeText(this@DashboardActivity, "NFC is not supported", Toast.LENGTH_LONG).show();
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        //isNfcEnabled()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity();

    }

    override fun onRefresh() {
        // Handle the refresh action here
        // This will be called when the user performs the swipe-to-refresh gesture
        //binding.linearRefreshProgressBar.visibility = View.VISIBLE
        getDashboardData()
        // Simulating a delay to showcase the refreshing state
        binding.swiperefresh.postDelayed({
            // Refresh your content or perform any other operations here
            binding.linearRefreshProgressBar.visibility = View.GONE
            // Once the refresh is complete, setRefreshing(false) to indicate it's finished
            binding.swiperefresh.isRefreshing = false
        }, 2000) // Delay of 2 seconds
    }

    private fun getDashboardData() {
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
                var str: String? = SharedPrefUtils.email
                val mfsCommonServiceRequest =
                    MfsCommonServiceRequest(
                        mfsSourceInfo = mfsSourceInfo,
                        mfsTransactionInfo = mfsTransactionInfo
                    )
                val mfsRequestInfo =
                    MfsRequestInfo(
                        entityId = SharedPrefUtils.entityId,
                        //entityType = SharedPrefUtils.entityType,
                        customerMsisdn = SharedPrefUtils.msisdn,
                        email = SharedPrefUtils.email,
                        historyCount = "10",
                        terminalId = UtilHandler.getDeviceId(this),
                        serviceId = Constants.SOFT_POS_SERVICE_TYPE_TAP_TAP+","+Constants.SOFT_POS_SERVICE_TYPE_PWT
                        //serviceId = Constants.SERVICE_ID
                    )
                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../dashboard", Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
                retrofit.loadDashboard(
                    SharedPrefUtils.tokenKey, Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this), UtilHandler.getScreenDensity(this),
                    coreServiceRequest
                )
                    .enqueue(
                        object : Callback<CommonDashboardResponseBaseModel> {
                            override fun onResponse(
                                call: Call<CommonDashboardResponseBaseModel>,
                                response: Response<CommonDashboardResponseBaseModel>
                            ) {
                                DialogUtils.hideDialog()
                                if (response.isSuccessful) {
                                    getFloorLimit()
                                    val mCommonDashboardResponseBaseModel: CommonDashboardResponseBaseModel? =
                                        response.body()
                                    if (!BuildConfig.USE_PRODUCTION_URLS)
                                        Log.d("Http..../dashboard", Gson().toJson(response.body()))
                                    if (mCommonDashboardResponseBaseModel?.commonDashboardResponse?.status != null) {
                                        if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.status.equals(
                                                "Success"
                                            )
                                        ) {
                                            /*We need below check because sometime api give success with null data in mfsResponse*/
                                            if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.dashboardUserInfoResponse?.mfsResponseInfo != null) {
                                                upDateDataOnUI(mCommonDashboardResponseBaseModel)
                                            } else
                                                UtilHandler.showSnackBar(
                                                    getString(R.string.something_went_wrong),
                                                    binding.root
                                                )
                                        } else {
                                            DialogUtils.hideDialog()
                                            UtilHandler.showSnackBar(
                                                mCommonDashboardResponseBaseModel?.commonDashboardResponse?.errorDescription,
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
                                call: Call<CommonDashboardResponseBaseModel>,
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

    private fun getBankNameList() {
        try {
            if (!UtilHandler.isOnline(this)) {
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
                return
            }

            val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
            retrofit.getBankNameList(
                SharedPrefUtils.tokenKey, Constants.PLATFORM,
                UtilHandler.getPackageVersion(this), UtilHandler.getScreenDensity(this)
            )
                .enqueue(object : Callback<BankListModel> {
                    override fun onResponse(
                        call: Call<BankListModel>,
                        response: Response<BankListModel>
                    ) {
                        if (response.isSuccessful) {
                            if (!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http..../bankNameList", Gson().toJson(response.body()))
                            val bankListModel: BankListModel? = response.body()
                            if (bankListModel?.status == "success" && bankListModel?.statusCode == 410) {


                                if (bankListModel?.object_ != null) {
                                    val jsonResponse = Gson().toJson(response.body())
                                    SharedPrefUtils.bankListData = jsonResponse.toString()


                                }
                            }


                        } else {

                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                binding.root
                            )
                        }

                    }

                    override fun onFailure(call: Call<BankListModel>, t: Throwable) {

                        UtilHandler.showSnackBar(
                            getString(R.string.something_went_wrong),
                            binding.root
                        )

                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTodayDateInFormat(): String {
        val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.ENGLISH)
        val currentDate = Calendar.getInstance().time
        return dateFormat.format(currentDate)
    }

    private fun formatWalletBalance(amount: String): String {
        val balance = amount.toDoubleOrNull()
        return if (balance != null) {
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)
            if (numberFormat is DecimalFormat) {
                numberFormat.applyPattern("#,##0.00")
            }
            numberFormat.format(balance)
        } else {
            amount // Return the original amount if it's not a valid number
        }
        UtilHandler
    }

    private fun getFloorLimit() {
        try {
            if (UtilHandler.isOnline(this)) {
                showProgressDialog(getString(R.string.progress_dialog_message))
                val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
                val floorLimitRequest = FloorLimitRequest(
                    SharedPrefUtils.entityType,
                    SharedPrefUtils.entitySubType
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http....floorlimit", Gson().toJson(floorLimitRequest))
                retrofit.floorLimit(
                    SharedPrefUtils.tokenKey,
                    Constants.PLATFORM,
                    UtilHandler.getPackageVersion(this),
                    UtilHandler.getScreenDensity(this),
                    floorLimitRequest
                ).enqueue(
                    object : Callback<FloorLimitResponse> {
                        override fun onResponse(
                            call: Call<FloorLimitResponse>,
                            response: Response<FloorLimitResponse>
                        ) {
                            hideProgressDialog()
                            if (response.isSuccessful) {
                                hideProgressDialog()
                                if(SharedPrefUtils.bankListData==null || SharedPrefUtils.bankListData=="") {
                                    //just need to set data once because this api data get changed rarely
                                    getBankNameList()
                                }
                                val floorLimitResponse = response.body()
                                if (!BuildConfig.USE_PRODUCTION_URLS)
                                    Log.d("Http....floorlimit", Gson().toJson(floorLimitResponse))

                                if (floorLimitResponse != null && floorLimitResponse.statusCode.equals(
                                        "100"
                                    )
                                ) {
                                    //val floorlimit=floorLimitResponse.floorLimit
                                    //Log.d("Floorlimit",Gson().toJson(response.body()))
                                    SharedPrefUtils.floorLimit = floorLimitResponse.floorLimit

                                } else {
                                    UtilHandler.showSnackBar(
                                        floorLimitResponse?.description,
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

                        override fun onFailure(call: Call<FloorLimitResponse>, t: Throwable) {
                            hideProgressDialog()
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
        }

    }

    /* private fun getEntitySubtype(): String? {
         return if (SharedPrefUtils.entitySubType.equals("0"))
             "0" else
             SharedPrefUtils.entitySubType
     }*/

    private fun upDateDataOnUI(mCommonDashboardResponseBaseModel: CommonDashboardResponseBaseModel) {
        /*Get Parent ID */
        if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.parentId != null) {
            SharedPrefUtils.parentID =
                mCommonDashboardResponseBaseModel.commonDashboardResponse?.parentId
        } else {
            SharedPrefUtils.parentID = null
        }
        /*for getting User first name and check it is null or no*/
        if (mCommonDashboardResponseBaseModel.commonDashboardResponse!!.dashboardUserInfoResponse.mfsResponseInfo.mfsEntityDetailsListInfo?.mfsEntityInfo != null) {
            userFirstName =
                mCommonDashboardResponseBaseModel.commonDashboardResponse!!.dashboardUserInfoResponse.mfsResponseInfo.mfsEntityDetailsListInfo?.mfsEntityInfo?.first()?.firstName.toString()
            SharedPrefUtils.UserFirstName = userFirstName
            binding.textUserNameDashboard.text =
                "Hi ${UtilHandler.capitalizeFirstLetter(userFirstName)},"
        } else {
            binding.textUserNameDashboard.text = "Hi,"
        }

        SharedPrefUtils.businessName =
            mCommonDashboardResponseBaseModel.commonDashboardResponse!!.dashboardUserInfoResponse.mfsResponseInfo.mfsEntityDetailsListInfo?.mfsEntityInfo?.first()?.businessName.toString()
        SharedPrefUtils.UserLastName =
            mCommonDashboardResponseBaseModel.commonDashboardResponse!!.dashboardUserInfoResponse.mfsResponseInfo.mfsEntityDetailsListInfo?.mfsEntityInfo?.first()?.lastName.toString()


        if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.terminalStatusResponse != null) {
            if (nfcAdapter != null && nfcAdapter!!.isEnabled) {
                if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.terminalStatusResponse?.status == "Success") {

                    isTerminalLinked = true
                    SharedPrefUtils.isTerminalLinked = true
                    val linkedMobileNumber: String? = SharedPrefUtils.msisdn
                    binding.linearLinkYourWallet.visibility = View.GONE
                    binding.linearNotNfc.visibility = View.GONE
                    binding.linearFirstDivider.visibility = View.GONE
                    binding.linearSecondDivider.visibility = View.GONE
                    binding.textTerminalLinkUnlink.visibility=View.VISIBLE
                    binding.textTerminalLinkUnlink.text = HtmlCompat.fromHtml(
                        " Wallet Linked <b>$linkedMobileNumber</b>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    binding.linearTapAndPay.alpha = 1f
                    binding.linearTapAndPay.isEnabled = true


                } else {
                    isTerminalLinked = false
                    SharedPrefUtils.isTerminalLinked = false
                    binding.textTerminalLinkUnlink.visibility=View.GONE
                    binding.linearLinkYourWallet.visibility = View.VISIBLE
                    binding.linearNotNfc.visibility = View.GONE
                    binding.linearTapAndPay.alpha = 0.5f
                    binding.linearTapAndPay.isEnabled = true
                }
            } else {

            }

        }
        //}

        /*for getting tx History data and check it is null or no*/
        if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.dashboardHistoryResponse?.mfsResponseInfo?.transactionListInfo?.transactionInfo != null) {
            txHistoryList =
                mCommonDashboardResponseBaseModel.commonDashboardResponse!!.dashboardHistoryResponse.mfsResponseInfo.transactionListInfo?.transactionInfo as MutableList<TransactionInfo>
            myAdapter = TransactionHistoryAdapter(
                this@DashboardActivity,
                txHistoryList
            )
            setLastTransactionId(txHistoryList)
            binding.recyclerViewTxHistory.adapter = myAdapter
            myAdapter.notifyDataSetChanged()
        } else {
            UtilHandler.showSnackBar(
                getString(R.string.no_history),
                binding.root
            )
            //DialogUtils.showCommonDialog(0,true,this@DashboardActivity,"This User have no History",OnClickListener {})
        }
        /*for getting total balance from wallet 1.0 and check it is null or no*/
        if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.dashboardUserInfoResponse?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                0
            )?.walletInfo?.wallet != null
        ) {
            var configItems =
                mCommonDashboardResponseBaseModel.commonDashboardResponse?.dashboardUserInfoResponse?.mfsResponseInfo?.mfsEntityDetailsListInfo?.mfsEntityInfo?.get(
                    0
                )?.walletInfo?.wallet
            if (configItems != null) {
                for (item in configItems) {
                    if (item.walletType == 1.0) {
                        if (item.curBalance != null) {
                            wallet1Balance = item.curBalance
                            binding.textTotalBalance.text =
                                UtilHandler.formatWalletBalance(item.curBalance)
                        }
                    }
                }
            }
        } else {
            binding.textTotalBalance.text = "00.00"
        }
        DialogUtils.hideDialog()
    }

    private fun setLastTransactionId(txHistoryList: MutableList<TransactionInfo>) {

        if (txHistoryList != null && txHistoryList.isNotEmpty()) {
            if (txHistoryList[0].transactionId != null) {
                SharedPrefUtils.lastTranId = txHistoryList[0].transactionId
            }
        }
    }

}

