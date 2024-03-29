package com.nownow.softpos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nownow.softpos.R
import com.nownow.softpos.databinding.DialogLoginSuccessBinding
import com.nownow.softpos.databinding.TerminalLinkSuccessfulBinding
import com.nownow.softpos.utils.Constants
import com.nownow.softpos.utils.SharedPrefUtils

class TerminalLinkSuccessful : BottomSheetDialogFragment() {
    private lateinit var binding: TerminalLinkSuccessfulBinding
    override fun getTheme(): Int {
        return R.style.FullScreenBottomSheet
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TerminalLinkSuccessfulBinding.inflate(inflater, container, false)


        binding.btnProceedToDashboard.setOnClickListener {
            SharedPrefUtils.isTerminalLinked = true
            val intent = Intent(requireActivity(), DashboardActivity::class.java)
            intent.putExtra(Constants.REQUEST_FROM,Constants.TERMINAL_LINK_SUCCESSFUL)
            startActivity(intent)
            dismiss()
        }

        return binding!!.root
    }
}