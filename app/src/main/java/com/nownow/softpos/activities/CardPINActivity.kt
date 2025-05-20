package com.aicortex.softpos.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import com.aicortex.softpos.R
import com.aicortex.softpos.databinding.ActivityCardPinBinding
import com.aicortex.softpos.utils.Constants
import com.aicortex.softpos.utils.SharedPrefUtils
import com.aicortex.softpos.utils.SoftPosConstants
import com.aicortex.softpos.utils.UtilHandler
import org.jpos.iso.ISOUtil


class CardPINActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivityCardPinBinding
    var stringPin: String? = null
    private var PAN = "5516422217375116"
    private var PINBLOCK: String? = null
    private var amount: String? = null
    private val pinBuilder = StringBuilder()
    private var max_pin_length: Int = 4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_card_pin)
        getCardIntentData()
        updateUI()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnConfirmPin.setOnClickListener(this)
        binding.backArrowBtn.setOnClickListener(this)
        binding.cancelPaymentLLayout.setOnClickListener(this)

    }

    private fun getCardIntentData() {
        if (!intent.hasExtra(Constants.CARD_PAN)) {
            throw java.lang.RuntimeException("PAN not provided")
        }
        if (TextUtils.isEmpty(intent.getStringExtra(Constants.CARD_PAN))
            || intent.getStringExtra(Constants.CARD_PAN)!!.length < 16
        ) {
            throw java.lang.RuntimeException("Invalid PAN")
        }
        PAN = substringPAN(intent.getStringExtra(Constants.CARD_PAN)!!)!!
        amount = intent.getStringExtra(Constants.BILL_AMOUNT)
    }

    private fun updateUI() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding.edt1Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.edt2Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.edt3Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.edt4Pin.transformationMethod = UtilHandler.CustomPasswordSymbol()
        binding.btnConfirmPin.isEnabled = false
        binding.btnConfirmPin.isClickable = false
        binding.btnConfirmPin.alpha = 0.5f
        binding.edt2Pin.isEnabled = false
        binding.edt3Pin.isEnabled = false
        binding.edt4Pin.isEnabled = false

        binding.edt1Pin.addTextChangedListener(textWatcher)
        binding.edt2Pin.addTextChangedListener(textWatcher)
        binding.edt3Pin.addTextChangedListener(textWatcher)
        binding.edt4Pin.addTextChangedListener(textWatcher)
        // Allow deleting the digits in reverse order
        binding.edt1Pin.setOnKeyListener(keyListener)
        binding.edt2Pin.setOnKeyListener(keyListener)
        binding.edt3Pin.setOnKeyListener(keyListener)
        binding.edt4Pin.setOnKeyListener(keyListener)

        amount.let {
            binding.txtPayableAmount.visibility = View.VISIBLE
            binding.txtAmount.text = getString(R.string.naira_icon) + " $it"
        }
    }

    private val keyListener = View.OnKeyListener { view, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            // Move focus to the previous EditText when the Backspace key is pressed
            when (view) {
                is EditText -> {
                    when (view) {
                        binding.edt4Pin -> {
                            edt4KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt3Pin -> {
                            edt3KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        binding.edt2Pin -> {
                            edt2KeyAction(keyCode, event)
                            return@OnKeyListener true
                        }
                        else -> {
                            // First EditText, do nothing
                        }
                    }
                }
            }
        }
        false
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (binding.edt1Pin.text.isNotEmpty() && binding.edt2Pin.text.isNotEmpty() && binding.edt3Pin.text.isNotEmpty() && binding.edt4Pin.text.isNotEmpty()) {
                binding.btnConfirmPin.isEnabled = true
                binding.btnConfirmPin.isClickable = true
                binding.btnConfirmPin.alpha = 1f
                UtilHandler.hideKeyboard(this@CardPINActivity)
                stringPin =
                    binding.edt1Pin.text.toString() + binding.edt2Pin.text.toString() + binding.edt3Pin.text.toString() + binding.edt4Pin.text.toString()
                appendPIN(stringPin!!)

            } else {
                binding.btnConfirmPin.isEnabled = false
                binding.btnConfirmPin.isClickable = false
                binding.btnConfirmPin.alpha = 0.5f
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (s.length == 1) {
                // Move focus to the next EditText when a digit is entered
                when (s.hashCode()) {
                    binding.edt1Pin.text.hashCode() -> edt1PinAction(s)
                    binding.edt2Pin.text.hashCode() -> edt2PinAction(s)
                    binding.edt3Pin.text.hashCode() -> edt3PinAction(s)
                    binding.edt4Pin.text.hashCode() -> edt4PinAction(s)

                }
            }
        }
    }

    /*Edittext text watcher action on every edittext*/
    private fun edt1PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt2Pin.isEnabled = true
            binding.edt2Pin.requestFocus()
            binding.edt1Pin.isEnabled = false
            val a = s.toString()
            binding.edt1Pin.removeTextChangedListener(textWatcher)
            binding.edt1Pin.setText(a)
            binding.edt1Pin.setSelection(a.length)
            binding.edt1Pin.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt2Pin.isEnabled = false
        }
    }

    private fun edt2PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt3Pin.isEnabled = true
            binding.edt3Pin.requestFocus()
            binding.edt2Pin.isEnabled = false
            val a = s.toString()
            binding.edt2Pin.removeTextChangedListener(textWatcher)
            binding.edt2Pin.setText(a)
            binding.edt2Pin.setSelection(a.length)
            binding.edt2Pin.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt3Pin.isEnabled = false
        }
    }

    private fun edt3PinAction(s: Editable) {
        if (s?.length == 1) {
            binding.edt4Pin.isEnabled = true
            binding.edt4Pin.requestFocus()
            binding.edt3Pin.isEnabled = false
            val a = s.toString()
            binding.edt3Pin.removeTextChangedListener(textWatcher)
            binding.edt3Pin.setText(a)
            binding.edt3Pin.setSelection(a.length)
            binding.edt3Pin.addTextChangedListener(textWatcher)
            //Toast.makeText(applicationContext, "Entered digit: $a", Toast.LENGTH_SHORT).show()
        } else {
            binding.edt4Pin.isEnabled = false
        }
    }

    private fun edt4PinAction(s: Editable) {
        if (s.length == 1) {
            binding.edt3Pin.requestFocus()
            val enteredDigit = s.toString()
            binding.edt4Pin.removeTextChangedListener(textWatcher)
            binding.edt4Pin.setText(enteredDigit)
            binding.edt4Pin.setSelection(enteredDigit.length)
            binding.edt4Pin.addTextChangedListener(textWatcher)
            //checkAllEdittextValue(finalResul)
            //Toast.makeText(applicationContext, "Entered digit: $finalResul", Toast.LENGTH_SHORT).show()
        }
    }

    /* key Listener for all edittext for deleting text from edittext*/
    private fun edt2KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt2Pin.text)) {
                binding.edt2Pin.text?.clear()
            } else {
                binding.edt1Pin.text?.clear()
                binding.edt1Pin.isEnabled = true
                binding.edt1Pin.requestFocus()
                binding.edt2Pin.clearFocus()
                binding.edt2Pin.isEnabled = false
            }
            binding.edt3Pin.isEnabled = false
            binding.edt4Pin.isEnabled = false
            //deletePIN()
        } else {
            binding.edt1Pin.isEnabled = false
        }
    }

    private fun edt3KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt3Pin.text)) {
                binding.edt3Pin.text?.clear()
            } else {
                binding.edt2Pin.text?.clear()
                binding.edt2Pin.isEnabled = true
                binding.edt2Pin.requestFocus()
                binding.edt3Pin.clearFocus()
                binding.edt3Pin.isEnabled = false
            }
            binding.edt1Pin.isEnabled = false
            binding.edt4Pin.isEnabled = false
            //  deletePIN()
        } else {
            binding.edt2Pin.isEnabled = false
        }
    }

    private fun edt4KeyAction(keyCode: Int, event: KeyEvent) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(binding.edt4Pin.text)) {
                binding.edt4Pin.text?.clear()
            } else {
                binding.edt3Pin.text?.clear()
                binding.edt3Pin.isEnabled = true
                binding.edt3Pin.requestFocus()
                binding.edt4Pin.clearFocus()
                binding.edt4Pin.isEnabled = false
            }
            binding.edt1Pin.isEnabled = false
            binding.edt2Pin.isEnabled = false
            //   deletePIN()

        } else {
            binding.edt3Pin.isEnabled = false
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_confirm_pin -> btnConfirmAction()
            R.id.backArrowBtn -> onBackPressed()
            R.id.cancelPaymentLLayout -> cancelTransaction()
            //else-> navigateToProfile()

        }
    }

    private fun cancelTransaction() {
        finish()
    }

    private fun btnConfirmAction() {
        /*if (stringPin != null && stringPin!!.length == 4) {
            //val intent = Intent(this, SuccessActivity::class.java)
            //intent.putExtra("PIN",stringPin)
            //Log.d("FullPIN", stringPin!!)
            //startActivity(intent)
        } else {
            UtilHandler.showSnackBar("Please fill valid PIN", binding.root)
        }*/
            stringPin =
                binding.edt1Pin.text.toString() + binding.edt2Pin.text.toString() + binding.edt3Pin.text.toString() + binding.edt4Pin.text.toString()
            if (stringPin!!.length == max_pin_length) {
                val PIN: String = stringPin as String
                PINBLOCK = generatePINBlock(PIN)
                val intent = Intent()
                intent.putExtra(Constants.CARD_PIN_BLOCK, PINBLOCK)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                UtilHandler.showSnackBar("Please enter $max_pin_length digit pin", binding.root)
            }


    }

    private fun substringPAN(pan: String): String? {
        return if (!TextUtils.isEmpty(pan) && pan.length >= 16) {
            pan.substring(3, pan.length - 1)
        } else {
            throw RuntimeException("Invalid PAN")
        }
    }

    private fun appendPIN(num: String) {
        pinBuilder.append(num)
        if (pinBuilder.length > max_pin_length!!) {
            pinBuilder.deleteCharAt(pinBuilder.length - 1)
            return
        }
    }

    private fun deletePIN() {
        if (pinBuilder.isNotEmpty()) {
            pinBuilder.deleteCharAt(pinBuilder.length - 1)
        }

    }

    private fun generatePINBlock(pin: String): String? {
        val len = pin.length
        val length = if (len < 10) "0$len" else "" + len
        val partOne = ISOUtil.strpadf(length + pin, 16)
        val partTwo = "0000$PAN"
        val pinBlock = ISOUtil.hexor(partOne, partTwo)
        return try {
            SoftPosConstants.getEncryption(SharedPrefUtils.TPK, pinBlock)
            //SoftPosConstants.getEncryption("0BF25EDA2FFDAB43AEE6B5BC9497BA43", pinBlock)
        } catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.RuntimeException("Unable to encrypt PINBLOCK")
        }
    }

    private fun getCurrentPIN(): String {
        return pinBuilder.toString()
    }


    override fun onResume() {
        super.onResume()
        binding.btnConfirmPin.isEnabled = false
        binding.btnConfirmPin.isClickable = false
        binding.btnConfirmPin.alpha = 0.5f
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        //val intent = Intent(this, DashboardActivity::class.java)
        //startActivity(intent)
    }



}