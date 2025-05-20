package com.aicortex.softpos.activities


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.databinding.MapTerminalDialogBinding
import com.aicortex.softpos.models.mapterminalmodels.MapTerminalRequestModel
import com.aicortex.softpos.models.mapterminalmodels.MapTerminalResponseModel
import com.aicortex.softpos.network.ServiceBuilderV2
import com.aicortex.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapTerminalDialog : BottomSheetDialogFragment(), OnClickListener {
    private lateinit var binding: MapTerminalDialogBinding

    companion object {
        fun newInstance(data: String, walletIDNumber: String): MapTerminalDialog {
            val fragment = MapTerminalDialog()
            val args = Bundle()
            args.putString(Constants.REQUEST_FROM, data)
            args.putString(Constants.WALLET_NUMBER, walletIDNumber)
            fragment.arguments = args
            return fragment
        }
    }

    var mContext: Context? = null

    override fun getTheme(): Int {
        return R.style.FullScreenBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MapTerminalDialogBinding.inflate(inflater, container, false)

        updateUI()
        setClickListeners()
        hideProgress()
        return binding!!.root
    }

    fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }
    private fun updateUI() {
        mContext = requireActivity()
        binding.edittextInputAmount.setText(SharedPrefUtils.msisdn)
        binding.edittextInputAmount.isEnabled = false
        /*binding.btnSubmit.isEnabled = false
        binding.btnSubmit.isClickable = false
        binding.btnSubmit.alpha = 0.5f*/
       /* if (arguments?.getString(Constants.REQUEST_FROM) != null) {
            if (arguments?.getString(Constants.WALLET_NUMBER) != null) {
            }
        }*/
        val textWatcher = object : TextWatcher {
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
                binding.edittextInputAmount.filters = arrayOf(filter)
                val hasTextFirst1 = binding.edittextInputAmount.text.isNotEmpty()


                if (hasTextFirst1) {
                    //binding.btnProceed.isEnabled = true
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.isClickable = true
                    binding.btnSubmit.alpha = 1f

                } else {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.isClickable = false
                    binding.btnSubmit.alpha = 0.5f

                    //binding.textTermsOfUse.alpha - 0.5f
                }

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 11) {
                    binding.edittextInputAmount.setText(s?.subSequence(0, 11))
                    binding.edittextInputAmount.setSelection(11)
                }

            }
        }
        binding.edittextInputAmount.addTextChangedListener(textWatcher)

    }

    private fun setClickListeners() {
        binding.btnSubmit.setOnClickListener(this)
        binding.closeLinkWalletDialog.setOnClickListener(this)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSubmit -> mapTerminal()
            R.id.closeLinkWalletDialog -> cancelMapTerminalDialog()
            //else-> navigateToProfile()

        }
    }

    private fun cancelMapTerminalDialog() {
        dismiss()
    }

    private fun openTerminalSuccessful() {
        dismiss()
        val b = TerminalLinkSuccessful()
        b.show(parentFragmentManager, "Terminal Link Successful")
    }

    private fun mapTerminal() {
        showProgress()
        try {
            if (mContext?.let { UtilHandler.isOnline(it) } == true) {
                //showProgressDialog("")
                val mapTerminalRequestModel = MapTerminalRequestModel(
                    msisdn = SharedPrefUtils.msisdn,
                    entityId = SharedPrefUtils.entityId,
                    serial = UtilHandler.getDeviceId(requireActivity()),
                    serviceType = Constants.SOFT_POS_SERVICE_TYPE_TAP_TAP.toInt()
                )
                if(!BuildConfig.USE_PRODUCTION_URLS) {
                    Log.d("Http..../map-terminal", Gson().toJson(mapTerminalRequestModel))
                }

                val retrofit = ServiceBuilderV2.buildService(ApiCalls::class.java)
                retrofit.mapTerminal(SharedPrefUtils.tokenKey,Constants.PLATFORM,
                    UtilHandler.getPackageVersion(requireActivity()),UtilHandler.getScreenDensity(requireActivity()), mapTerminalRequestModel)
                    .enqueue(object : Callback<MapTerminalResponseModel> {
                        override fun onResponse(
                            call: Call<MapTerminalResponseModel>,
                            response: Response<MapTerminalResponseModel>
                        ) {

                            if (response.isSuccessful) {
                                val mapTerminalResponseModel: MapTerminalResponseModel? =
                                    response.body()
                                if(!BuildConfig.USE_PRODUCTION_URLS)
                                Log.d("Http..../map-terminal", Gson().toJson(mapTerminalResponseModel))
                                if (mapTerminalResponseModel != null && mapTerminalResponseModel.status == true && mapTerminalResponseModel.message.equals(
                                        "Success"
                                    )
                                ) {
                                    hideProgress()
                                    openTerminalSuccessful()
                                }else{
                                    hideProgress()
                                    UtilHandler.showSnackBar(
                                        getString(R.string.something_went_wrong),
                                        binding.root.findViewById(R.id.root_layout)
                                    )
                                }


                            } else {
                                hideProgress()
                                UtilHandler.showSnackBar(
                                    getString(R.string.something_went_wrong),
                                    binding.root.findViewById(R.id.root_layout)
                                )
                            }
                        }

                        override fun onFailure(call: Call<MapTerminalResponseModel>, t: Throwable) {
                            hideProgress()
                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                binding.root.findViewById(R.id.root_layout)
                            )
                        }
                    })

            } else {
                hideProgress()
                UtilHandler.showSnackBar(
                    getString(R.string.internet_error_msg),
                    binding.root.findViewById(R.id.root_layout)
                )
            }
        } catch (e: Exception) {
            hideProgress()
            UtilHandler.showSnackBar(
                getString(R.string.something_went_wrong),
                binding.root.findViewById(R.id.root_layout)
            )
        }
    }


}

