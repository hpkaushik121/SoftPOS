package com.aicortex.softpos.utils
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
/*import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener*/
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.application.TapNpayApplication


class AppsflyerEventPostback {
    private val context: Context? = TapNpayApplication.appInstance
   /* private fun setDeviceId() {
        try {
            val tMgr: TelephonyManager =
                context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var deviceId: String? = null
            if (tMgr != null) {
                deviceId = tMgr.getDeviceId()
            }
            val androidId: String? = UtilHandler.getDeviceID(context)
            AppsFlyerLib.getInstance().setImeiData(deviceId)
            AppsFlyerLib.getInstance().setAndroidIdData(androidId)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }*/


    /* fun sendOnBoardingEvent(context: Context?,) {
         try {
            //setDeviceId()
             AppsFlyerLib.getInstance().start(context!!)
            val eventParameters2: MutableMap<String, Any> = HashMap()
            //eventParameters2["p1"] = categoryId
            //eventParameters2["p1"] = categoryName


             AppsFlyerLib.getInstance().logEvent(
                context,
                Constants.onboarding_proceed_clicked,
                eventParameters2,
                object : AppsFlyerRequestListener {
                    override fun onSuccess() {
                        if(!BuildConfig.USE_PRODUCTION_URLS)
                        Log.d("onboarding_proceed_clicked", "Success")
                    }

                    override fun onError(i: Int, s: String) {
                        if(!BuildConfig.USE_PRODUCTION_URLS)
                        Log.d("onboarding_proceed_clicked", "Error Code $i Error message$s")
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/


}