package com.aicortex.softpos.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import com.aicortex.softpos.application.TapNpayApplication
import com.spice.paypro.Security
import org.json.JSONObject
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


object UtilHandler {
    fun showToast(context: Context, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun getDeviceID(context: Context?): String? {
        //return Settings.Secure.getString(context!!.contentResolver, Settings.Secure.ANDROID_ID).toString().uppercase()
        return Settings.Secure.getString(context!!.contentResolver, Settings.Secure.ANDROID_ID)

    }

    fun generateRequestId(mobile: String): String? {
        var s = ""
        val random = Random()
        val high = 99999
        val low = 11111
        s =
            mobile + System.currentTimeMillis() / 1000 + (random.nextInt(high - low) + low) + (random.nextInt(
                high - low
            ) + low)
        return s
    }

    fun showLongToast(message: String?) {
        Toast.makeText(TapNpayApplication.appInstance, message, Toast.LENGTH_LONG)
            .show()
    }

    fun showSnackBar(message: String?, view: View?) {
        try {
            if (view != null && view !is ScrollView && !TextUtils.isEmpty(message)) {
                val snackbar = Snackbar.make(view, message!!, Snackbar.LENGTH_LONG)
                val snackBarView = snackbar.view
                snackBarView.setBackgroundColor(Color.DKGRAY)
                val textView = snackBarView.findViewById<TextView>(R.id.snackbar_text)
                textView.setTextColor(Color.WHITE)
                textView.maxLines = 5
                snackbar.duration = 5000
                snackbar.show()
            } else if (!TextUtils.isEmpty(message)) {
                showLongToast(message)
            }
        } catch (e: Exception) {

        }
    }

    fun getCurrentDate(): String {
        return DateFormat.format("dd-MM-yyyy HH:MM:ss", Date(System.currentTimeMillis())).toString()
    }

    fun enCryptPassword(accessCode: String): String {
        var accessCode = accessCode
        try {
            accessCode = Security.encrypt(accessCode)
        } catch (e: NoSuchAlgorithmException) {
        }
        return accessCode
    }

    fun getDeviceId(context: Context): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun changeDateFormat(inputDate: String,fromScreen:String): String {
        //val str1 = "2023-06-04 12:25:14.356747 +1:00"
        if (!inputDate.isNullOrEmpty()) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            if (fromScreen == Constants.FROM_TX_HISTORY){
                val outputFormat = SimpleDateFormat("EEE, dd/MM", Locale.getDefault())
                val trimmedStr = inputDate.substring(0, inputDate.indexOf('.'))
                val date = inputFormat.parse(trimmedStr)
                val formattedDate = outputFormat.format(date)
                return formattedDate
            }else if (fromScreen == Constants.FROM_TX_DETAIL_PAGE){
                val outputFormat = SimpleDateFormat("EEEE, MMM d, yyyy | h:mma", Locale.getDefault())
                val trimmedStr = inputDate.substring(0, inputDate.indexOf('.'))
                val date = inputFormat.parse(trimmedStr)
                val formattedDate = outputFormat.format(date)
                return formattedDate
            }else{
                val outputFormat = SimpleDateFormat("EEE, dd/MM", Locale.getDefault())
                val trimmedStr = inputDate.substring(0, inputDate.indexOf('.'))
                val date = inputFormat.parse(trimmedStr)
                val formattedDate = outputFormat.format(date)
                return formattedDate
            }

        } else {
            return " "
        }

    }

    fun getDateMonth(yymmdd: Char): String? {
        val ca1 = Calendar.getInstance()
        var yearmonday = ""
        val iDay = ca1[Calendar.DATE]
        val iMonth = ca1[Calendar.MONTH] + 1
        val iYear = ca1[Calendar.YEAR]
        if (yymmdd == 'M') {
            yearmonday = if ((iMonth.toString() + "").length == 1) {
                "0$iMonth"
            } else {
                "" + iMonth
            }
        }
        if (yymmdd == 'Y') {
            yearmonday = "" + iYear
        }
        if (yymmdd == 'D') {
            yearmonday = if ((iDay.toString() + "").length == 1) {
                "0$iDay"
            } else {
                "" + iDay
            }
        }
        return yearmonday
    }

    fun canOtpSend(tenDigitValidNumber: String?): Boolean {
        return try {
            val msisdnOtpData: String = SharedPrefUtils.MSISDN_OTP.toString()
            if (msisdnOtpData == null) {
                true
            } else {
                val jsonObject = JSONObject(msisdnOtpData)
                if (jsonObject.has(tenDigitValidNumber)) {
                    val value = jsonObject.getString(tenDigitValidNumber)
                    val s = value.split("_".toRegex()).toTypedArray()
                    val lastOtpSend = s[0].toLong()
                    val step = s[1]
                    if (step.equals("1", ignoreCase = true)) {
                        System.currentTimeMillis() - lastOtpSend > Constants.COUNTER_TIME_FIRST
                    } else if (step.equals("2", ignoreCase = true)) {
                        System.currentTimeMillis() - lastOtpSend > Constants.COUNTER_TIME_SECOND
                    } else if (step.equals("3", ignoreCase = true)) {
                        System.currentTimeMillis() - lastOtpSend > Constants.COUNTER_TIME_THIRD
                    } else {
                        true
                    }
                } else {
                    true
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            true
        }
    }

    fun removeMSISDNfromOtpCache(tenDigitValidNumber: String?) {
        try {
            val msisdnOtpData: String = SharedPrefUtils.MSISDN_OTP.toString()
            if (msisdnOtpData != null) {
                val jsonObject = JSONObject(msisdnOtpData)
                if (jsonObject.has(tenDigitValidNumber)) {
                    jsonObject.remove(tenDigitValidNumber)
                    SharedPrefUtils.MSISDN_OTP = jsonObject.toString()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentStep(tenDigitValidNumber: String?): String? {
        return try {
            val msisdnOtpData: String = SharedPrefUtils.MSISDN_OTP.toString()
            val jsonObject = JSONObject(msisdnOtpData)
            if (jsonObject.has(tenDigitValidNumber)) {
                val value = jsonObject.getString(tenDigitValidNumber)
                val s = value.split("_".toRegex()).toTypedArray()
                (s[1].toInt() + 1).toString()
            } else "1"
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            "1"
        }
    }

    fun millisLeft(tenDigitValidNumber: String?, update: Boolean): Long {
        return try {
            val msisdnOtpData: String? = SharedPrefUtils.MSISDN_OTP
            if (msisdnOtpData == null) {
                val jsonObject = JSONObject()
                jsonObject.put(tenDigitValidNumber, "" + System.currentTimeMillis() + "_" + "1")
                SharedPrefUtils.MSISDN_OTP = jsonObject.toString()
                Constants.COUNTER_TIME_FIRST
            } else {
                val jsonObject = JSONObject(msisdnOtpData)
                if (jsonObject.has(tenDigitValidNumber)) {
                    val value = jsonObject.getString(tenDigitValidNumber)
                    val s = value.split("_".toRegex()).toTypedArray()
                    val lastOtpSend = s[0].toLong()
                    val step = s[1]
                    if (step.equals("1", ignoreCase = true)) {
                        if (System.currentTimeMillis() - lastOtpSend > Constants.COUNTER_TIME_FIRST) {
                            if (update) {
                                jsonObject.put(
                                    tenDigitValidNumber,System.currentTimeMillis().toString() + "_" + "2"
                                )
                                SharedPrefUtils.MSISDN_OTP = jsonObject.toString()
                                Constants.COUNTER_TIME_SECOND
                            } else 0
                        } else Constants.COUNTER_TIME_FIRST - (System.currentTimeMillis() - lastOtpSend)
                    } else if (step.equals("2", ignoreCase = true)) {
                        if (System.currentTimeMillis() - lastOtpSend > Constants.COUNTER_TIME_SECOND) {
                            if (update) {
                                jsonObject.put(
                                    tenDigitValidNumber, System.currentTimeMillis().toString() + "_" + "3"
                                )
                                SharedPrefUtils.MSISDN_OTP = jsonObject.toString()
                                Constants.COUNTER_TIME_THIRD
                            } else 0
                        } else Constants.COUNTER_TIME_SECOND - (System.currentTimeMillis() - lastOtpSend)
                    }  else {
                        jsonObject.put(
                            tenDigitValidNumber,
                            System.currentTimeMillis().toString() + "_" + "1"
                        )
                        SharedPrefUtils.MSISDN_OTP = jsonObject.toString()
                        Constants.COUNTER_TIME_FIRST
                    }
                } else {
                    jsonObject.put(tenDigitValidNumber, "" + System.currentTimeMillis() + "_" + "1")
                    SharedPrefUtils.MSISDN_OTP = jsonObject.toString()
                    Constants.COUNTER_TIME_FIRST
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Constants.COUNTER_TIME_FIRST
        }
    }

    fun formatWalletBalance(amount: String): String {
        val balance = amount.toDoubleOrNull()
        return if (balance != null) {
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)
            if (numberFormat is DecimalFormat) {
                numberFormat.applyPattern("#,##0")
            }
            numberFormat.format(balance)
        } else {
            amount // Return the original amount if it's not a valid number
        }
    }

    class CustomPasswordSymbol : PasswordTransformationMethod() {
        override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
            return PasswordCharSequence(source)
        }

        class PasswordCharSequence(source: CharSequence?) : CharSequence {
            private val mSource = source
            override val length: Int
                get() = mSource?.length ?: 0

            override fun get(index: Int): Char {
                return '*'
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return mSource?.subSequence(startIndex, endIndex) ?: ""
            }
        }
    }

    fun getPackageVersion(context: Context): String? {
        var pInfo: PackageInfo? = null
        var version: String? = ""
        try {
            pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return version
    }
    fun getScreenDensity(context: Context): String? {
        var den = 0f
        return try {
            den = context.resources.displayMetrics.density
            if (den.toDouble() == 0.75) {
                //return 0.75 if it's LDPI
                "mdpi"
            } else if (den.toDouble() == 1.0) {
                // return 1.0 if it's MDPI
                "mdpi"
            } else if (den.toDouble() == 1.5) {
                // return 1.5 if it's HDPI
                "hdpi"
            } else if (den.toDouble() == 2.0) {
                // return 2.0 if it's XHDPI
                "xhdpi"
            } else if (den.toDouble() == 3.0) {
                // return 3.0 if it's XXHDPI
                "xxhdpi"
            } else if (den.toDouble() == 4.0) {
                // return 4.0 if it's XXXHDPI
                "xxhdpi"
            } else {
                "xxhdpi"
            }
        } catch (e:Exception) {
            "xxhdpi"
        }
    }
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }
    fun capitalizeFirstLetter(str: String?): String? {
        //return str?.replaceFirstChar {  it.titlecase(Locale.ROOT)  } ?: str
        return if (str != null && str.length>1) {
            str[0].uppercaseChar()?.plus(str.substring(1).lowercase(Locale.ROOT))
        } else if (str !=null && str.length==1)
            str[0].uppercaseChar().toString()
        else
            ""
    }
}