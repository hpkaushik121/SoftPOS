package com.nownow.softpos.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.nownow.softpos.R
import com.nownow.softpos.api.ApiCalls
import com.nownow.softpos.databinding.ActivityResetPinBinding
import com.nownow.softpos.models.core.request.*
import com.nownow.softpos.models.core.response.CoreResponseModel
import com.nownow.softpos.network.ServiceBuilder
import com.nownow.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPINActivity : BottomSheetDialogFragment(),OnClickListener {

    private lateinit var binding: ActivityResetPinBinding
    companion object {
        fun newInstance(data: String,): ResetPINActivity {
            val fragment = ResetPINActivity()
            val args = Bundle()
            args.putString(Constants.REQUEST_FROM, data)
            fragment.arguments = args
            return fragment
        }
    }
    override fun getTheme(): Int {
        return R.style.FullScreenBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityResetPinBinding.inflate(inflater, container, false)



        binding.proceedForgetPin.setOnClickListener {

        }
        setClickListeners()
        return binding!!.root
    }
    private fun setClickListeners() {
        binding.checkBoxEmail.setOnClickListener(this)
        binding.checkBoxSms.setOnClickListener(this)
        binding.proceedForgetPin.setOnClickListener(this)
        binding.buttonCancel.setOnClickListener(this)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.checkBoxEmail-> checkBoxMailClick()
            R.id.checkBoxSms-> checkBoxSmsClick()
            R.id.proceedForgetPin-> proceedToOtp()
            R.id.button_cancel->cancelPinReset()
            //else-> navigateToProfile()

        }
    }

    private fun cancelPinReset() {
        dismiss()
    }

    fun checkBoxMailClick(){

    }
    fun checkBoxSmsClick(){

    }
    fun proceedToOtp(){
        sendRequestToGenerateOtp()
    }
    private fun sendRequestToGenerateOtp() {
        try {
            var mfsRequestInfo: MfsRequestInfo? =null
            if (UtilHandler.isOnline(requireActivity())) {
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
                mfsRequestInfo = if(binding.checkBoxEmail.isChecked && !binding.checkBoxSms.isChecked)
                    MfsRequestInfo(msisdn = SharedPrefUtils.msisdn, email = SharedPrefUtils.email, notificationFlag = "0", length =Constants.OTP_LENGTH_6)
                else  if(!binding.checkBoxEmail.isChecked && binding.checkBoxSms.isChecked)
                    MfsRequestInfo(msisdn = SharedPrefUtils.msisdn, email = SharedPrefUtils.email, notificationFlag = "1", length =Constants.OTP_LENGTH_6)
                else
                    MfsRequestInfo(msisdn = SharedPrefUtils.msisdn, email = SharedPrefUtils.email, notificationFlag = "1", length =Constants.OTP_LENGTH_6)




                val coreServiceRequest = CoreServiceRequest(
                    mfsCommonServiceRequest = mfsCommonServiceRequest,
                    mfsRequestInfo = mfsRequestInfo
                )

                Log.d(Constants.LOG_D_RESPONSE, Gson().toJson(coreServiceRequest))

                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.generateOtp(SharedPrefUtils.tokenKey,Constants.PLATFORM,
                    UtilHandler.getPackageVersion(requireActivity()),UtilHandler.getScreenDensity(requireActivity()), coreServiceRequest).enqueue(
                    object : Callback<CoreResponseModel> {
                        override fun onResponse(
                            call: Call<CoreResponseModel>,
                            response: Response<CoreResponseModel>
                        ) {
                            DialogUtils.hideDialog()
                            if (response.isSuccessful) {
                                val mCoreResponseModel: CoreResponseModel? = response.body()
                                if(mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo != null) {
                                    if (mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.status.equals(
                                            "Success",
                                            ignoreCase = true
                                        )
                                    ) {
                                        val intent =
                                            Intent(
                                                requireActivity(),
                                                OtpValidateActivity::class.java
                                            )
                                        intent.putExtra(
                                            Constants.REQUEST_FROM,
                                            Constants.FROM_FORGET_PIN
                                        )
                                        if (binding.checkBoxEmail.isChecked)
                                            intent.putExtra(Constants.OTP_ON_SMS_OR_EMAIL, "email")
                                        else
                                            intent.putExtra(Constants.OTP_ON_SMS_OR_EMAIL, "sms")
                                        startActivity(intent)
                                    } else {
                                        UtilHandler.showSnackBar(
                                            mCoreResponseModel?.mfsCommonServiceResponse?.mfsStatusInfo?.errorDescription,
                                            binding.root
                                        )
                                    }
                                }else{
                                    UtilHandler.showSnackBar(getString(R.string.something_went_wrong),binding.root)
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
                UtilHandler.showToast(requireActivity(),getString(R.string.internet_error_msg))
            }
        } catch (e: Exception) {
            e.message
            DialogUtils.hideDialog()
        }
    }

    fun showProgressDialog(msg:String){
        DialogUtils.showDialog(requireActivity(),msg)
    }
    fun hideProgressDialog(){
        DialogUtils.hideDialog()
    }
}
