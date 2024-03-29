package com.nownow.softpos.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nownow.softpos.R
import com.nownow.softpos.databinding.DialogPinSuccessBinding
import com.nownow.softpos.utils.Constants

class PinSuccessDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogPinSuccessBinding

    companion object {
        fun newInstance(data: String): PinSuccessDialog {
            val fragment = PinSuccessDialog()
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
        binding = DialogPinSuccessBinding.inflate(inflater, container, false)

        binding.proceedToDashTxt.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, DashboardActivity::class.java)
            startActivity(intent)

        })
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}