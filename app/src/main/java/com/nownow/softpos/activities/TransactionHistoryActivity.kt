package com.nownow.softpos.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nownow.softpos.BuildConfig
import com.nownow.softpos.R
import com.nownow.softpos.adapters.TransactionHistoryAdapterForPagination
import com.nownow.softpos.api.ApiCalls
import com.nownow.softpos.databinding.ActivityTransactionHistoryBinding
import com.nownow.softpos.models.core.request.*
import com.nownow.softpos.models.core.response.CoreResponseModel
import com.nownow.softpos.models.dashboard.TransactionInfo
import com.nownow.softpos.network.ServiceBuilder
import com.nownow.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TransactionHistoryActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityTransactionHistoryBinding
    val arrayList = ArrayList<String>()
    private lateinit var myAdapter: TransactionHistoryAdapterForPagination
    var txHistoryList: MutableList<TransactionInfo> = mutableListOf()
    private var currentPage = 1
    private val itemsPerPage = 10
    private var isLoadingData = false
    var firstVisibleItem = 0
    var visibleItemCount:Int = 0
    var totalItemCount:Int = 0
    private val visibleThreshold = 5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_history)
        setClickListeners()
        //fetchDashboardData()
        updateUI()
        fetchTransactionHistory(totalItemCount, itemsPerPage)

    }
    private fun setClickListeners(){
        binding.backArrow.setOnClickListener(this)
    }
    fun updateUI(){
        myAdapter = TransactionHistoryAdapterForPagination(
            this@TransactionHistoryActivity,
            txHistoryList
        )
        binding.recyclerView.adapter = myAdapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.itemCount
                var totalItemCount = layoutManager.itemCount

                visibleItemCount = recyclerView.childCount
                totalItemCount = layoutManager.getItemCount()
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (!isLoadingData && totalItemCount <= totalItemCount && totalItemCount != 0
                ) {
                    currentPage++
                    if (binding.editSearchHistory.text != null){
                        if (binding.editSearchHistory.text.toString().isNotEmpty()){

                        }else{
                            fetchTransactionHistory(totalItemCount, itemsPerPage)
                        }

                    }
                    isLoadingData = true
                }
                // Check if the user is near the end of the list and no network request is in progress
                /*if (lastVisibleItemPosition == totalItemCount - 1 && !isLoadingData) {
                    currentPage++
                    fetchDashboardDataNew(currentPage, itemsPerPage)
                }*/
            }
        })

        binding.editSearchHistory.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

        } )
    }
    fun onLoadMore(current_page:Int, totalItemCount:Int){

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> backToDashboard()
        }
    }
    /* private fun fetchDashboardData() {
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
                         historyCount = "50",
                         terminalId = UtilHandler.getDeviceId(this),
                         serviceId = Constants.SOFT_POS_SERVICE_TYPE
                         //serviceId = Constants.SERVICE_ID
                     )

                 val coreServiceRequest = CoreServiceRequest(
                     mfsCommonServiceRequest = mfsCommonServiceRequest,
                     mfsRequestInfo = mfsRequestInfo
                 )
                if(!BuildConfig.USE_PRODUCTION_URLS)
                 Log.d("Http..../dashboard", Gson().toJson(coreServiceRequest))

                 val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
                 retrofit.loadDashboard(
                     SharedPrefUtils.tokenKey,Constants.PLATEFORM,
                     UtilHandler.getPackageVersion(this),UtilHandler.getScreenDensity(this),
                     coreServiceRequest
                 )
                     .enqueue(
                         object : Callback<CommonDashboardResponseBaseModel> {
                             override fun onResponse(
                                 call: Call<CommonDashboardResponseBaseModel>,
                                 response: Response<CommonDashboardResponseBaseModel>
                             ) {

                                 val datta: String = response.body().toString()
                                 if (response.isSuccessful) {
                                     val mCommonDashboardResponseBaseModel: CommonDashboardResponseBaseModel? =
                                         response.body()
                                     if(!BuildConfig.USE_PRODUCTION_URLS)
                                         Log.d("Http..../dashboard", Gson().toJson(mCommonDashboardResponseBaseModel))
                                     if (mCommonDashboardResponseBaseModel?.commonDashboardResponse?.status != null) {
                                         if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.status.equals(
                                                 "Success"
                                             )
                                         ) {
                                             //*for getting tx History data and check it is null or no*//
                                             if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.dashboardHistoryResponse?.mfsResponseInfo?.transactionListInfo?.transactionInfo != null){
                                                 txHistoryList = mCommonDashboardResponseBaseModel.commonDashboardResponse!!.dashboardHistoryResponse.mfsResponseInfo.transactionListInfo?.transactionInfo
                                                 myAdapter = TransactionHistoryAdapter(
                                                     this@TransactionHistoryActivity,
                                                     txHistoryList
                                                 )
                                                 binding.recyclerView.adapter = myAdapter
                                                 myAdapter.notifyDataSetChanged()
                                                 hideProgressDialog()
                                             }else{
                                                 hideProgressDialog()
                                                 UtilHandler.showSnackBar("This User have no History",binding.root)
                                             }
                                         } else {
                                             hideProgressDialog()
                                             UtilHandler.showSnackBar(
                                                 mCommonDashboardResponseBaseModel?.commonDashboardResponse?.errorDescription,
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
                                         binding.root
                                     )
                                 }
                             }
                             override fun onFailure(
                                 call: Call<CommonDashboardResponseBaseModel>,
                                 t: Throwable
                             ) {
                                 hideProgressDialog()
                                 Log.d(Constants.LOG_D_KEY, t.message.toString())
                             }
                         }
                     )
             } else {
                 hideProgressDialog()
                 UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
             }
         } catch (e: Exception) {
             hideProgressDialog()
             e.message
             hideProgressDialog()
         }
     }*/

    private fun backToDashboard(){
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
    private fun fetchTransactionHistory(pageNumber: Int, itemsPerPage: Int) {
        if (pageNumber == 0) {
            try {
                if (txHistoryList != null && myAdapter != null) {
                    txHistoryList.clear()
                    myAdapter.notifyDataSetChanged()
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
            isLoadingData = false
        }
        // Set isLoadingData to true to prevent concurrent requests
        isLoadingData = true
        try {
            if (UtilHandler.isOnline(this)) {
                //showProgressDialog(getString(R.string.progress_dialog_message))
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
                        serviceId = Constants.SOFT_POS_SERVICE_TYPE_TAP_TAP+","+Constants.SOFT_POS_SERVICE_TYPE_PWT,
                        count = "5",
                        offSet = pageNumber.toString()
                        //serviceId = "851"
                    )

                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../newHistory", Gson().toJson(coreServiceRequest))
                //Logger.d(Constants.LOG_D_RESPONSE, Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.transactionHistoryNew(
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
                                isLoadingData = false
                                val datta: String = response.body().toString()
                                if (response.isSuccessful) {
                                    val mCoreResponseModel: CoreResponseModel? =
                                        response.body()
                                    if(!BuildConfig.USE_PRODUCTION_URLS) {
                                        Log.d(
                                            "Http..../newHistory",
                                            Gson().toJson(mCoreResponseModel)
                                        )
                                    }
                                    if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status != null) {
                                        if (mCoreResponseModel.mfsCommonServiceResponse.mfsStatusInfo.status.equals(
                                                "Success"
                                            )
                                        ) {
                                            /*for getting tx History data and check it is null or no*/
                                            if (mCoreResponseModel.mfsResponseInfo?.transactionListInfo?.transactionInfo != null) {
                                                val txHistoryList = mCoreResponseModel.mfsResponseInfo.transactionListInfo.transactionInfo

                                                //myAdapter.notifyDataSetChanged()
                                                if (txHistoryList != null) {
                                                    myAdapter.addTransactions(txHistoryList)
                                                }

                                                // Append the new transactions to the existing list in the adapter
                                                myAdapter.notifyDataSetChanged()

                                                hideProgressDialog()
                                            } else {
                                                hideProgressDialog()
                                                //UtilHandler.showSnackBar("This User has no History", binding.root)
                                            }
                                            //if (mCommonDashboardResponseBaseModel.commonDashboardResponse?.dashboardHistoryResponse?.mfsResponseInfo?.transactionListInfo?.transactionInfo != null){
                                            //    txHistoryList = mCommonDashboardResponseBaseModel.commonDashboardResponse!!.dashboardHistoryResponse.mfsResponseInfo.transactionListInfo?.transactionInfo
                                            //    myAdapter = TransactionHistoryAdapter(
                                            //        this@TransactionHistoryActivity,
                                            //        txHistoryList
                                            //    )
                                            //    binding.recyclerView.adapter = myAdapter
                                            //    myAdapter.notifyDataSetChanged()
                                            //    hideProgressDialog()
                                            //}else{
                                            //    hideProgressDialog()
                                            //    UtilHandler.showSnackBar("This User have no History",binding.root)
                                            //}
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
                                        binding.root
                                    )
                                }
                            }
                            override fun onFailure(
                                call: Call<CoreResponseModel>,
                                t: Throwable
                            ) {
                                isLoadingData = false
                                hideProgressDialog()
                                Logger.d(Constants.LOG_D_KEY, t.message)
                            }
                        }
                    )
            } else {
                hideProgressDialog()
                UtilHandler.showSnackBar(getString(R.string.internet_error_msg), binding.root)
            }
        } catch (e: Exception) {
            hideProgressDialog()
            e.message
            hideProgressDialog()
        }
    }
    private fun loadNextPage() {
        currentPage++
        fetchTransactionHistory(currentPage, itemsPerPage)
    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<TransactionInfo> = ArrayList()

        // running a for loop to compare elements.
        for (item in txHistoryList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.transactionStatus.toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
           //Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            myAdapter.filterList(filteredlist)
        }
    }
}