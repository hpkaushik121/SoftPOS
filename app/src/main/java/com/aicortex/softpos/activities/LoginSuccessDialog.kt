package com.aicortex.softpos.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.aicortex.softpos.R
import com.aicortex.softpos.api.CheckDialogEventInterface
import com.aicortex.softpos.databinding.ActivityCardReadBinding
import com.aicortex.softpos.databinding.DialogLoginSuccessBinding
import com.aicortex.softpos.utils.Constants
import com.aicortex.softpos.utils.SharedPrefUtils

class LoginSuccessDialog(param: CheckDialogEventInterface) : BottomSheetDialogFragment() {
    private lateinit var checkDialogEventInterface: CheckDialogEventInterface
    companion object {
        fun newInstance(data: String,isBiometric:String): LoginSuccessDialog {
            val fragment = LoginSuccessDialog(object :
                CheckDialogEventInterface {
                override fun onDismiss(screenName: String) {

                }
            })
            val args = Bundle()
            args.putString(Constants.REQUEST_FROM, data)
            args.putString(Constants.IS_BIO_ACTIVE, isBiometric)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is CheckDialogEventInterface){
            checkDialogEventInterface= context as CheckDialogEventInterface
        }else{
            throw java.lang.RuntimeException("Context error")
        }
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d("Trigged", "Dismiss method dismiss")

        checkDialogEventInterface.onDismiss(Constants.LOGIN_SUCCESS)
        // Perform your specific code here when the dialog is dismissed
    }
    private lateinit var binding: DialogLoginSuccessBinding
    override fun getTheme(): Int {
        return R.style.FullScreenBottomSheet
    }
    private var fromActivity : String? = null
    private var isBiometric : String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogLoginSuccessBinding.inflate(inflater, container, false)
       fromActivity =  arguments?.getString(Constants.REQUEST_FROM)
        isBiometric = arguments?.getString(Constants.IS_BIO_ACTIVE)
        if (fromActivity != null){
            if (fromActivity.equals("OTPVALIDATE")){
                binding.heading.text = getString(R.string.biometric_login_inactive)
            }else{
                if (isBiometric != null){
                    if (isBiometric.equals("Y")){
                        binding.heading.text = getString(R.string.biometric_login_active)
                    }else{
                        binding.heading.text = getString(R.string.biometric_login_inactive)
                    }
                }

           }
        }else{

        }

        binding.proceedToDashTxt.setOnClickListener {
            dismiss()
            SharedPrefUtils.msisdn=SharedPrefUtils.tempMsisdn //final number user has logged in with tks
            val intent = Intent(requireActivity(), DashboardActivity::class.java)
            startActivity(intent)
        }

        return binding!!.root
    }
    private fun updateUI(){

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}
