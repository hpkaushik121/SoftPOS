package com.nownow.softpos.helper

import android.content.Context
import android.content.Context.FINGERPRINT_SERVICE
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricPrompt
import androidx.biometric.FingerprintDialogFragment
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nownow.softpos.utils.IBiometricListener
import com.nownow.softpos.utils.UtilHandler
import java.util.concurrent.Executor

object BiometricHelper {
    private lateinit var mExecutor: Executor
    private lateinit var mBiometricPrompt: BiometricPrompt
    private lateinit var mPromptInfo: BiometricPrompt.PromptInfo
    private lateinit var mManager: BiometricManager
    private lateinit var mLoginExecutor: Executor
    private lateinit var mLoginBiometricPrompt: BiometricPrompt
    private lateinit var mLoginPromptInfo: BiometricPrompt.PromptInfo
    private lateinit var mLoginManager: BiometricManager
    private lateinit var iBiometricListener: IBiometricListener

    @RequiresApi(Build.VERSION_CODES.N)
    fun initializeBiometricScanner(mContext: Context) {
        mManager = from(mContext)
        mExecutor = ContextCompat.getMainExecutor(mContext)
        mPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authentication is required to continue")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
            .build()
    }
    fun initializeBiometricScannerLogin(mContext: Context) {
        mLoginManager = from(mContext)
        mLoginExecutor = ContextCompat.getMainExecutor(mContext)
        mLoginPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authentication is required to continue")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()
    }

    //@RequiresApi(Build.VERSION_CODES.M)
    fun showPrompt(fromScreen:String,iBiometricListener: IBiometricListener, mContext: FragmentActivity) {
        BiometricHelper.iBiometricListener = iBiometricListener

        if (this::mManager.isInitialized) {
            when (mManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)) {
                BIOMETRIC_SUCCESS -> {
                    if (fromScreen.equals("OTP")){
                        iBiometricListener.onSuccess()
                    }else{
                        mBiometricPrompt = BiometricPrompt(mContext, mExecutor, mBiometricCallback)
                        mBiometricPrompt.authenticate(mPromptInfo)
                    }

                    //mBiometricPrompt.authenticate(mPromptInfo,BiometricPrompt.CryptoObject(cipher))
                }
                BIOMETRIC_ERROR_NO_HARDWARE -> {
                  // UtilHandler.showToast(mContext,"this is message")
                    iBiometricListener.onNegativeClick()
                }
                BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    //mBiometricCallback
//                    UtilHandler.showToast("Oops! it seems you have not yet enrolled biometric on your device.")
                    iBiometricListener.onEnrolled()
                }
               else -> iBiometricListener.onNegativeClick()
            }
        } else {
            iBiometricListener.onNegativeClick()
        }
    }

    private val mBiometricCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            iBiometricListener.onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            when (errorCode) {
                BiometricPrompt.ERROR_LOCKOUT -> {
                    iBiometricListener.onAuthError()
                    //UtilHandler.showToast(context = Context,"Locked out due too many failed attempts. Please try after 30 seconds to unlock.")
                }
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                    iBiometricListener.onAuthError()
                    //UtilHandler.showToast("Biometric authentication is disabled until the user unlocks with their device credential.")
                }
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                    iBiometricListener.onAuthError()
                    //UtilHandler.showToast("Biometric cancelled by user.")
                }
                BiometricPrompt.ERROR_TIMEOUT -> {
                    iBiometricListener.onAuthError()
                    //UtilHandler.showToast("Biometric authentication timeout.")
                }
                BiometricPrompt.ERROR_USER_CANCELED -> {
                    iBiometricListener.onAuthError()
                    //UtilHandler.showToast("Biometric cancelled by user.")
                }
                else -> iBiometricListener.onCancel(errString.toString())
            }
        }
    }
}