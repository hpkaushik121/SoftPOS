package com.aicortex.softpos.api

import com.aicortex.softpos.models.appConfig.AppConfigRequestModel
import com.aicortex.softpos.models.appConfig.AppConfigResponse
import com.aicortex.softpos.models.core.request.CoreServiceRequest
import com.aicortex.softpos.models.core.request.FloorLimitRequest
import com.aicortex.softpos.models.core.response.BankListModel
import com.aicortex.softpos.models.core.response.CoreResponseModel
import com.aicortex.softpos.models.dashboard.CommonDashboardResponseBaseModel
import com.aicortex.softpos.models.downloadPdfModels.DownloadPdfRequest
import com.aicortex.softpos.models.downloadPdfModels.DownloadPdfResponse
import com.aicortex.softpos.models.mapterminalmodels.MapTerminalRequestModel
import com.aicortex.softpos.models.mapterminalmodels.MapTerminalResponseModel
import com.aicortex.softpos.models.otpResponse.CoreOtpResponse
import com.aicortex.softpos.models.softpos.*
import retrofit2.Call
import retrofit2.http.*

interface ApiCalls {


    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/authManagement/get")
    fun getToken(
        @Header("Authorization") authorization: String,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>



    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/tokenManagement/validate")
    fun verifyOtp(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CommonQposRequestModel
    ): Call<CommonQposResponseModel>

    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/userManagement/getUserInfo")
    fun getUserInfo(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>


    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/loginManagement/verifyMpin")
    fun verifyEmailAndmPin(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>

    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/tokenManagement/validate")
    fun verifyOtp(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,
        @Body requestModel: CoreServiceRequest
    ): Call<CoreOtpResponse>

    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/loginManagement/validateUser")
    fun validateUser(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>

    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management//tokenManagement/get")
    fun generateOtp(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>

    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/loginManagement/manageMpin")
    fun resetPin(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>


    @Headers("Content-Type: application/json")
    @POST("/appconfig/api/v2/getSettings")
    fun getAppSettings(@Body requestModel: AppConfigRequestModel
    ): Call<AppConfigResponse>
    /*Request for Dashboard API*/
    @Headers("Content-Type: application/json")
    @POST("/softPos/dashboard")
    fun loadDashboard(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CommonDashboardResponseBaseModel>
    /*Map terminal  API*/
    @Headers("Content-Type: application/json")
    @POST("/qpos/map-terminal")
    fun mapTerminal(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: MapTerminalRequestModel
    ): Call<MapTerminalResponseModel>


    @Headers("Content-Type: application/json")
    @POST("/qpos/v2/pay")
    fun softPosPay(@Header("Authorization") token: String?,
                   @Header("platform") platform: String?,
                   @Header("appversion")  appversion: String?,
                   @Header("resolution")  resolution: String?,

                   @Body requestModel: CommonQposRequestModel
    ): Call<TerminaldataResponseModel>

    @Headers("Content-Type: application/json")
    @POST("/qpos/v1/validate")
    fun validateTerminal(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,
        @Body requestModel: CommonQposRequestModel
    ): Call<CommonQposResponseModel>

    @Headers("Content-Type: application/json")
    @POST("/qpos/v1/getTerminalData")
    fun downloadTerminalData(@Header("Authorization") token: String?,
                             @Header("platform") platform: String?,
                             @Header("appversion")  appversion: String?,
                             @Header("resolution")  resolution: String?,

                             @Body requestModel: CommonQposRequestModel
    ): Call<CommonQposResponseModel>

    @Headers("Content-Type: application/json")
    @POST("/softPos/floorLimit")
    fun floorLimit(@Header("Authorization") token: String?,
                   @Header("platform") platform: String?,
                   @Header("appversion")  appversion: String?,
                   @Header("resolution")  resolution: String?,

                   @Body requestModel: FloorLimitRequest
    ): Call<FloorLimitResponse>

    /*Request for Move Fund Wallet API*/
    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/balanceManagement/transferV3")
    fun moveFundWallet(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>

    /*API for download transaction detail pdf*/
    @Headers("Content-Type: application/json")
    @POST("/download-transaction-s3/getSingleStatement")
    fun downloadTransactionDetails(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body downloadPdfRequest: DownloadPdfRequest
    ): Call<DownloadPdfResponse>


    @Headers("Content-Type: application/json")
    @GET("/softPos/getTerminalDetailsByTerminalId")
    fun getTerminalDetails(@Header("Authorization") token: String?,
                           @Header("platform") platform: String?,
                           @Header("appversion")  appversion: String?,
                           @Header("resolution")  resolution: String?,

                           @Query("terminalId") terminalId:String?

    ): Call<GetTerminalDetails>
    /*Request for Transaction History New API*/
    @Headers("Content-Type: application/json")
    //@POST("/mfs-transaction-management/transactionManagement/newHistory")
    @POST("/mfs-transaction-management/transactionManagement/multiServiceFilteredHistory")
    fun transactionHistoryNew(
        @Header("Authorization") token: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>
    /*API for get tx history Detail status*/
    @Headers("Content-Type: application/json")
    @POST("/mfs-transaction-management/transactionManagement/status")
    fun getTxHistoryFullStatus(
        @Header("Authorization") authorization: String?,
        @Header("platform") platform: String?,
        @Header("appversion")  appversion: String?,
        @Header("resolution")  resolution: String?,

        @Body requestModel: CoreServiceRequest
    ): Call<CoreResponseModel>

    @Headers("Content-Type: application/json")
    @GET("/mfs-wallet-management/walletManagement/bankNameList")
    fun getBankNameList(@Header("Authorization") token: String?,
                           @Header("platform") platform: String?,
                           @Header("appversion")  appversion: String?,
                           @Header("resolution")  resolution: String?

    ): Call<BankListModel>
}