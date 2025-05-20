package com.aicortex.softpos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.aicortex.softpos.R
import com.aicortex.softpos.databinding.ActivityCardDetectedDialogBinding
import com.aicortex.softpos.databinding.DialogPinSuccessBinding
import com.aicortex.softpos.utils.Constants

class CardDetectedDialog : BottomSheetDialogFragment() {
    lateinit var binding: ActivityCardDetectedDialogBinding
    private var cardType : String? = null
    companion object {
        fun newInstance(cardType: String): CardDetectedDialog {
            val fragment = CardDetectedDialog()
            val args = Bundle()
            args.putString(Constants.CARD_TP, cardType)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityCardDetectedDialogBinding.inflate(inflater, container, false)
        cardType =  arguments?.getString(Constants.CARD_TP)
        if (cardType != null){
            if (cardType?.contains("MASTER") == true) {
                binding.imgCardIcon.setImageResource(R.drawable.ic_mastercard)
                binding.cardTypeText.text="MasterCard"
            } else if (cardType?.contains("VISA") == true) {
                binding.imgCardIcon.setImageResource(R.drawable.ic_visa)
                binding.cardTypeText.text="VisaCard"
            }
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}