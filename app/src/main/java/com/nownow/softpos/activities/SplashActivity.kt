package com.aicortex.softpos.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.aicortex.softpos.BuildConfig
import com.aicortex.softpos.R
import com.aicortex.softpos.api.ApiCalls
import com.aicortex.softpos.databinding.ActivitySplashBinding
import com.aicortex.softpos.models.appConfig.AppConfigRequestModel
import com.aicortex.softpos.models.appConfig.AppConfigResponse
import com.aicortex.softpos.network.ServiceBuilder
import com.aicortex.softpos.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.system.exitProcess

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        ParentSharedPrefObj.init(this@SplashActivity)
        getAppSettings()
    }

    private fun getAppSettings() {
        SharedPrefUtils.DeviceID = UtilHandler.getDeviceId(applicationContext)
        try {
            if (UtilHandler.isOnline(this)) {
                val appConfigRequestModel = AppConfigRequestModel(
                    configType = Constants.CONFIG_TYPE,
                    sharedSecretKeyApp = Constants.SHARED_SECRET_KEY_VALUE
                )
                if (!BuildConfig.USE_PRODUCTION_URLS)
                    Log.d("Http..../getSettings", Gson().toJson(appConfigRequestModel))
                val retrofit = ServiceBuilder.buildService(ApiCalls::class.java)
                retrofit.getAppSettings(appConfigRequestModel)
                    .enqueue(object : Callback<AppConfigResponse> {
                        override fun onResponse(
                            call: Call<AppConfigResponse>,
                            response: Response<AppConfigResponse>
                        ) {

                            if (response.isSuccessful) {
                                val appConfigResponse: AppConfigResponse? = response.body()
                                if (!BuildConfig.USE_PRODUCTION_URLS) {
                                    Log.d(
                                        "Http..../getSettings",
                                        Gson().toJson(appConfigResponse)
                                    )
                                }
                                if (appConfigResponse != null && appConfigResponse.success == true) {
                                    var configItems = appConfigResponse.result?.resultBody?.config

                                    if (configItems != null && configItems.isNotEmpty()) {
                                            for (item in configItems) {
                                            when (item.key) {
                                                "soft_pos_max" -> {
                                                    SharedPrefUtils.maxTrnAmount = item.value
                                                }
                                                "updatedVersion" -> {
                                                    SharedPrefUtils.UpdatedVersion = item.value
                                                }
                                                "forceUpdateVersion" -> {
                                                    SharedPrefUtils.ForceUpdateVersion = item.value
                                                }
                                                "maintenanceMode" -> {
                                                    SharedPrefUtils.maintenanceMode = item.value
                                                }
                                                "soft_pos_min" -> {
                                                    SharedPrefUtils.minTrnAmount = item.value
                                                }
                                                "soft_pos_max" -> {
                                                    SharedPrefUtils.maxTrnAmount = item.value
                                                }
                                                "entityType" -> {
                                                    SharedPrefUtils.EntityTypeArray = item.value
                                                }
                                                "entitySubType" -> {
                                                    SharedPrefUtils.EntitySubTypeArray = item.value
                                                }
                                                "default_account" -> {
                                                    SharedPrefUtils.DefaultAccountArray = item.value
                                                }

                                                "pay_with_transfer_min" -> {
                                                    SharedPrefUtils.PWT_MIN = item.value
                                                }
                                                "pay_with_transfer_max" -> {
                                                    SharedPrefUtils.PWT_MAX = item.value
                                                }

                                                "p_p_url" -> {
                                                    SharedPrefUtils.PrivacyPolicyURL = item.value
                                                }
                                                "t_n_c_url" -> {
                                                    SharedPrefUtils.TermAndConditionURL = item.value
                                                }

                                            }

                                        }
                                        checkAppUpdates(appConfigResponse)


                                    } else {
                                        UtilHandler.showSnackBar(
                                            getString(R.string.config_empty),
                                            binding.root
                                        )
                                    }
                                    if (appConfigResponse.result?.resultBody?.configType != null) {
                                        SharedPrefUtils.ConfigType =
                                            appConfigResponse.result.resultBody.configType
                                    }
                                } else {

                                }


                            } else {
                                UtilHandler.showSnackBar(
                                    getString(R.string.something_went_wrong),
                                    findViewById(R.id.root_layout)
                                )
                            }
                        }

                        override fun onFailure(call: Call<AppConfigResponse>, t: Throwable) {
                            UtilHandler.showSnackBar(
                                getString(R.string.something_went_wrong),
                                findViewById(R.id.root_layout)
                            )
                        }
                    })

            } else {
                UtilHandler.showSnackBar(
                    getString(R.string.internet_error_msg),
                    findViewById(R.id.root_layout)
                )
            }
        } catch (e: Exception) {
            hideProgressDialog()
            UtilHandler.showSnackBar(
                getString(R.string.something_went_wrong),
                findViewById(R.id.root_layout)
            )
        }
    }

    private fun checkAppUpdates(appConfigResponse: AppConfigResponse) {
        try {
            val version: Int = BuildConfig.VERSION_NAME.replace(".", "").toInt()
            val forceUpdateVersion: Int? =
                SharedPrefUtils.ForceUpdateVersion?.replace(".", "")?.toInt()
            val updateVersion: Int? = SharedPrefUtils.UpdatedVersion?.replace(".", "")?.toInt()

            if (appConfigResponse.result?.resultBody?.configType?.equals(Constants.CONFIG_TYPE) == true) {
                if (SharedPrefUtils.maintenanceMode.equals("1")) {
                    DialogUtils.showCommonDialog(
                        1,
                        false,
                        this,
                        getString(R.string.new_app_maintainence),
                        View.OnClickListener {
                            Process.killProcess(Process.myPid())
                            exitProcess(1)
                        })

                } else if (forceUpdateVersion != null && forceUpdateVersion > version) {

                    DialogUtils.showCommonDialog(
                        1,
                        false,
                        this,
                        getString(R.string.new_app_update),
                        View.OnClickListener {
                            openPlayStore(this, "")
                        })
                } else if (updateVersion != null && updateVersion > version) {
                    DialogUtils.showCommonDialog2(
                        2,
                        false,
                        this,
                        getString(R.string.new_app_update),
                        View.OnClickListener {
                            openPlayStore(this, "")

                        },View.OnClickListener {
                            initView(20)

                        })


                } else {
                    if (SharedPrefUtils.EntityTypeArray != null) {
                        initView(20)
                    } else {
                        UtilHandler.showSnackBar(
                            getString(R.string.config_empty),
                            binding.root
                        )
                    }

                }
            }
        } catch (e: Exception) {

            e.printStackTrace()

        }
    }

    fun openPlayStore(pContext: Context, updateLink: String?) {
        try {
            if (!TextUtils.isEmpty(updateLink)) {
                try {
                    pContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateLink)))
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            } else {
                val packageName = pContext.packageName
                if (!TextUtils.isEmpty(packageName)) {
                    try {
                        pContext.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$packageName")
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        pContext.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initView(initDelay: Int) {

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, initDelay.toLong())
    }
}