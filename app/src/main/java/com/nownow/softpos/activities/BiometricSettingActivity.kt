package com.nownow.softpos.activities

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nownow.softpos.R
import com.nownow.softpos.api.CheckDialogEventInterface
import com.nownow.softpos.databinding.ActivityBiometricSettingBinding
import com.nownow.softpos.helper.BiometricHelper
import com.nownow.softpos.utils.*


class BiometricSettingActivity(param: CheckDialogEventInterface) : BottomSheetDialogFragment(),
    OnClickListener {

    private lateinit var binding: ActivityBiometricSettingBinding
    private lateinit var checkDialogEventInterface: CheckDialogEventInterface
    override fun getTheme(): Int {
        return R.style.FullScreenBottomSheet
    }

    var isDismissByButton: Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CheckDialogEventInterface) {
            checkDialogEventInterface = context as CheckDialogEventInterface
        } else {
            throw java.lang.RuntimeException("Context error")
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d("Trigged", "Dismiss method dismiss")
        if (isDismissByButton) {
        } else {
            checkDialogEventInterface.onDismiss(Constants.BIOMETRIC)
        }

        // Perform your specific code here when the dialog is dismissed
    }


    private val images = arrayOf(R.drawable.biomatric, R.drawable.biometric2, R.drawable.biometric3)
    private var currentImageIndex = 0
    private val imageSwitchInterval = 1200L  // 1 second
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityBiometricSettingBinding.inflate(inflater, container, false)
        updateUI()
        setClickListeners()
        startImageSwitchAnimation()
        //val biometricSettingActivity:BiometricSettingActivity
        //biometricSettingActivity.isCancelable = false
        binding.toggleBiometricBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textTurnOnOffLeft.text = "Turn OFF"
                binding.textTurnOnOffLeft.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray
                    )
                )
                binding.textTurnOnOffRight.text = "ON"
                binding.textTurnOnOffRight.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange_e9
                    )
                )
                binding.toggleBiometricBtn.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.orange_e9)
                binding.toggleBiometricBtn.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)

            } else {
                SharedPrefUtils.bioMetricUUID = "Disable"
                SharedPrefUtils.isBiometricEnable = false
                binding.textTurnOnOffLeft.text = " OFF"
                binding.textTurnOnOffLeft.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.textTurnOnOffRight.text = "Turn ON"
                binding.textTurnOnOffRight.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray
                    )
                )
                binding.toggleBiometricBtn.trackTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.gray)
                binding.toggleBiometricBtn.thumbTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
            }
        }

        return binding!!.root
    }

    private fun setClickListeners() {
        binding.btnProceed.setOnClickListener(this)
        binding.toggleBiometricBtn.setOnClickListener(this)
    }

    fun updateUI() {
        UtilHandler.removeMSISDNfromOtpCache(SharedPrefUtils.tempMsisdn)
        if (SharedPrefUtils.bioMetricUUID != null && SharedPrefUtils.bioMetricUUID.equals("Enable")) {
            Log.d(Constants.LOG_D_RESPONSE, "YESENABLE")
            binding.toggleBiometricBtn.isChecked = true
            binding.textTurnOnOffLeft.text = "Turn OFF"
            binding.textTurnOnOffLeft.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray
                )
            )
            binding.textTurnOnOffRight.text = "ON"
            binding.textTurnOnOffRight.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.orange_e9
                )
            )
            //val trackColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            binding.toggleBiometricBtn.trackTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.orange_e9)
            binding.toggleBiometricBtn.thumbTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
        } else if (SharedPrefUtils.bioMetricUUID != null && SharedPrefUtils.bioMetricUUID.equals("Disable")) {
            Log.d(Constants.LOG_D_RESPONSE, "NOENABLE")
            binding.toggleBiometricBtn.isChecked = false
            binding.textTurnOnOffLeft.text = " OFF"
            binding.textTurnOnOffLeft.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            binding.textTurnOnOffRight.text = "Turn ON"
            binding.textTurnOnOffRight.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray
                )
            )
            //val trackColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            binding.toggleBiometricBtn.trackTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.gray)
            binding.toggleBiometricBtn.thumbTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
        } else {
            binding.textTurnOnOffLeft.text = " OFF"
            binding.textTurnOnOffLeft.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
            binding.textTurnOnOffRight.text = "Turn ON"
            binding.textTurnOnOffRight.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray
                )
            )
            //val trackColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            binding.toggleBiometricBtn.trackTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.gray)
            binding.toggleBiometricBtn.thumbTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
        }
    }

    private fun startImageSwitchAnimation() {
        val animation = AlphaAnimation(1f, 0f)
        animation.duration = imageSwitchInterval
        animation.repeatCount = 0 // Display each image only once

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                if (currentImageIndex == images.size - 1) {
                    currentImageIndex =
                        0 // Start from the first image after displaying all images once
                } else {
                    currentImageIndex++
                }
                binding.imageBioMetricAnimation.setImageResource(images[currentImageIndex])
                binding.imageBioMetricAnimation.startAnimation(animation)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        binding.imageBioMetricAnimation.setImageResource(images[currentImageIndex])
        binding.imageBioMetricAnimation.startAnimation(animation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_proceed -> proccedButtonAction()
            //else-> navigateToProfile()

        }
    }

    private fun proccedButtonAction() {
        activity?.let { BiometricHelper.initializeBiometricScannerLogin(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity?.let { BiometricHelper.initializeBiometricScanner(it) }
        }
        if (binding.toggleBiometricBtn.isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity?.let { it1 ->
                    BiometricHelper.showPrompt(" ", object : IBiometricListener {
                        override fun onEnrolled() {
                            Log.d(Constants.LOG_D_RESPONSE, "onEnrolled")
                            //UtilHandler.showToast(getString(R.string.setup_biometric_not_enrolled))
                        }

                        override fun onNegativeClick() {
                            Log.d(Constants.LOG_D_RESPONSE, "onNegativeClick")
                            context?.let { it2 ->
                                UtilHandler.showToast(
                                    it2,
                                    "User Canceled Biometric"
                                )
                            }
                            //UtilHandler.showToast(getString(R.string.biometric_un_success))
                        }

                        override fun onSuccess() {
                            SharedPrefUtils.bioMetricUUID = "Enable"
                            SharedPrefUtils.isBiometricEnable = true
                            isDismissByButton = true
                            dismiss()
                            val b = LoginSuccessDialog.newInstance("BIOMETRIC", "Y")
                            b.show(requireActivity().supportFragmentManager, "Login Success")

                            /*val intent = Intent(activity, LoginSuccessDialog::class.java)
                            intent.putExtra(Constants.REQUEST_FROM, "BIOMETRIC")
                            intent.putExtra(Constants.IS_BIO_ACTIVE, "Y")
                            startActivity(intent)*/
                            context?.let { it2 ->
                                UtilHandler.showToast(
                                    it2,
                                    "Successfully Setup Biometric"
                                )
                            }
                            Log.d(Constants.LOG_D_RESPONSE, "onSuccess")
                            //biometricStatusUpdate()
                        }

                        override fun onCancel(errString: String) {
                            context?.let { it2 ->
                                UtilHandler.showToast(
                                    it2,
                                    "User Canceled Event Biometric"
                                )
                            }
                            Log.d(Constants.LOG_D_RESPONSE, "onCancel")
                            //UtilHandler.showToast(getString(R.string.biometric_un_success) + " " + errString)
                            //switch_biometric.isChecked = false
                            //button_setup.isEnabled = false
                        }

                        override fun onAuthError() {
                            binding.toggleBiometricBtn.isChecked = false
                            SharedPrefUtils.bioMetricUUID = "Disable"
                            SharedPrefUtils.isBiometricEnable = false
                            context?.let { it2 ->
                                UtilHandler.showToast(
                                    it2,
                                    " Biometric Auth Error"
                                )
                            }
                            Log.d(Constants.LOG_D_RESPONSE, "onAuthError")
                            // switch_biometric.isChecked = false
                            //button_setup.isEnabled = false

                        }
                    }, it1)
                }
            }
        } else {
            //checkDialogEventInterface.onDismiss("")
            isDismissByButton = true
            dismiss()
            val b = LoginSuccessDialog.newInstance("BIOMETRIC", "N")
            b.show(requireActivity().supportFragmentManager, "Login Success")
        }
        Log.d(Constants.LOG_D_RESPONSE, binding.toggleBiometricBtn.isEnabled.toString())
    }
}