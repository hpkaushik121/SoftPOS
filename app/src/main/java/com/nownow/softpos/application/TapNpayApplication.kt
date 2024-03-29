package com.nownow.softpos.application

import android.app.Application
import android.util.Log
/*import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener*/
import com.nownow.softpos.BuildConfig
import com.nownow.softpos.utils.Constants
import java.util.*


class TapNpayApplication : Application() {
    val LOG_TAG = "AppsFlyer"
    var conversionData: Map<String, Any>? = null
    override fun onCreate() {
        super.onCreate()
        // android.os.Debug.waitForDebugger()
        appInstance = applicationContext as TapNpayApplication
       // initAppsFlyer()
    }

    /*private fun initAppsFlyer() {

        if (!BuildConfig.USE_PRODUCTION_URLS)
            AppsFlyerLib.getInstance().setDebugLog(true)


        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionDataMap: Map<String, Any>) {
                for (attrName in conversionDataMap.keys) {
                    if(!BuildConfig.USE_PRODUCTION_URLS) {
                        Log.d(
                            LOG_TAG,
                            "Conversion attribute: " + attrName + " = " + conversionDataMap[attrName]
                        )
                    }
                }
                val status: String =
                    Objects.requireNonNull(conversionDataMap["af_status"]).toString()
                if (status == "Non-organic") {
                    if (Objects.requireNonNull(conversionDataMap["is_first_launch"])
                            .toString() == "true"
                    ) {
                        if(!BuildConfig.USE_PRODUCTION_URLS)
                        Log.d(LOG_TAG, "Conversion: First Launch")
                    } else {
                        if(!BuildConfig.USE_PRODUCTION_URLS)
                        Log.d(LOG_TAG, "Conversion: Not First Launch")
                    }
                } else {
                    if(!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d(LOG_TAG, "Conversion: This is an organic install.")
                }
                conversionData = conversionDataMap
            }

            override fun onConversionDataFail(errorMessage: String) {
                if(!BuildConfig.USE_PRODUCTION_URLS)
                Log.d(LOG_TAG, "error getting conversion data: $errorMessage")
            }

            override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                if(!BuildConfig.USE_PRODUCTION_URLS)
                Log.d(LOG_TAG, "onAppOpenAttribution: This is fake call.")
            }

            override fun onAttributionFailure(errorMessage: String) {
                if(!BuildConfig.USE_PRODUCTION_URLS)
                Log.d(LOG_TAG, "error onAttributionFailure : $errorMessage")
            }
        }



        AppsFlyerLib.getInstance().init(Constants.APPSFLYER_PROJECT_KEY, conversionListener, this)


        AppsFlyerLib.getInstance().start(this, Constants.APPSFLYER_PROJECT_KEY, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(LOG_TAG, "Launch sent successfully")
            }

            override fun onError(errorCode: Int, errorDesc: String) {
                Log.d(
                    LOG_TAG, "Launch failed to be sent:\n" +
                            "Error code: " + errorCode + "\n"
                            + "Error description: " + errorDesc
                )
            }
        })


    }*/

    companion object {
        lateinit var appInstance: TapNpayApplication
    }
}