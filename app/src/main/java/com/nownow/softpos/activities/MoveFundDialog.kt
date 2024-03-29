package com.nownow.softpos.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nownow.softpos.R
import com.nownow.softpos.databinding.DialogMoveFundBinding
import com.nownow.softpos.utils.*
import java.text.NumberFormat
import java.util.*

class MoveFundDialog : BottomSheetDialogFragment(), OnClickListener {

    private lateinit var binding: DialogMoveFundBinding
    //var fullAmount: String = "2,333,304"
    private var isFormatting: Boolean = false
    private var Wallet1BalanceAmount:String? = "000"
    private val numberFormat: NumberFormat =
        NumberFormat.getNumberInstance(Locale.getDefault())
    companion object {
        fun newInstance(data: String?): MoveFundDialog {
            val fragment = MoveFundDialog()
            val args = Bundle()
            args.putString(Constants.WALLET1_BALANCE_AMOUNT, data)
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
        binding = DialogMoveFundBinding.inflate(inflater, container, false)
        Wallet1BalanceAmount =  arguments?.getString(Constants.WALLET1_BALANCE_AMOUNT)
        upDateUI()
        setClickListeners()
        return binding!!.root
    }

    private fun setClickListeners() {
        binding.textProceed.setOnClickListener(this)
        binding.textMoveAllAndPreferredAmount.setOnClickListener(this)
        binding.backIcon.setOnClickListener(this)
    }

    private fun upDateUI() {
        try {
            val formattedString = numberFormat.format(Wallet1BalanceAmount?.toLong())
            binding.textProceed.isEnabled = false
            binding.textProceed.alpha = 0.5f
            binding.textFullWalletBalance.text =
                "Wallet Balance: " + getString(R.string.naira_icon) + formattedString
            binding.textNairaIcon.visibility = View.GONE
            val textWatcher = object : TextWatcher {
                private var current: String? = null


                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (binding.editWalletBalance.length() == 1) {
                        if (binding.editWalletBalance.text.toString() == ".") {
                            binding.editWalletBalance.setText("")
                        }
                    }
                    val hasTextFirst1 = binding.editWalletBalance.text?.isNotEmpty()
                    if (hasTextFirst1 == true) {
                        binding.textProceed.isEnabled = true
                        binding.textProceed.isClickable = true
                        binding.textProceed.alpha = 1f
                        binding.textNairaIcon.visibility = View.VISIBLE
                    } else {
                        binding.textProceed.isEnabled = false
                        binding.textProceed.isClickable = false
                        binding.textProceed.alpha = 0.5f
                        binding.textNairaIcon.visibility = View.GONE
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
                        val formattedString = numberFormat.format(userInput.toLong())
                        current = formattedString
                        binding.editWalletBalance.setText(formattedString)
                        binding.editWalletBalance.setSelection(formattedString.length)
                        isFormatting = false
                    }

                }
            }
            binding.editWalletBalance.addTextChangedListener(textWatcher)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textMoveAllAndPreferredAmount -> switchText()
            R.id.backIcon -> dismiss()
            R.id.textProceed -> proceedToNext()

        }
    }

    private fun proceedToNext() {
        val intent = Intent(requireActivity(), ChangeAndConfirmPinActivity::class.java)
        intent.putExtra(Constants.REQUEST_FROM, Constants.FROM_MOVE_FUND_WALLET)
        intent.putExtra(Constants.BILL_AMOUNT, binding.editWalletBalance.text.toString().replace(",".toRegex(), "").toInt())
        startActivity(intent)
        //UtilHandler.showLongToast("Fund is transferred")
        dismiss()

    }

    private fun switchText() {
        if (binding.textMoveAllAndPreferredAmount.text.equals(getString(R.string.move_all_text))) {
            binding.textMoveAllAndPreferredAmount.text = getString(R.string.input_preferred_amount)
            binding.editWalletBalance.setText(Wallet1BalanceAmount)
            binding.editWalletBalance.isEnabled = false
            binding.editWalletBalance.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.edittext_text_color_gray
                )
            )
            binding.textNairaIcon.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.edittext_text_color_gray
                )
            )
        } else {
            binding.textMoveAllAndPreferredAmount.text = getString(R.string.move_all_text)
            binding.editWalletBalance.isEnabled = true
            binding.editWalletBalance.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
            binding.textNairaIcon.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }
    }

}