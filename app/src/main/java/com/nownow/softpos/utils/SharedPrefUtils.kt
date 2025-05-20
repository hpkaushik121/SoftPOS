package com.aicortex.softpos.utils

class SharedPrefUtils : ParentSharedPrefObj() {

    interface Keys {
        interface Global {
            companion object {
                const val config: String = "Global.Config.stale"
                const val DEVICE_ID = "Global.deviceId"
                const val FLOOR_LIMIT = "Global.floorlimit"
                const val TOKEN_KEY = "Global.tokenKey"
                const val ENTITY_TYPE = "Global.entity_type"
                const val ENTITY_SUB_TYPE = "Global.entity_sub_type"
                const val ENTITY_ID = "Global.entity_id"
                const val BusinessName = "Global.business_name"
                const val UpdatedVersion = "Global.updatedVersion"
                const val ForceUpdateVersion = "Global.forceUpdateVersion"
                const val MaintenanceMode = "Global.maintenanceMode"
                const val PrivacyPolicyURL = "Global.p_p_url"
                const val TermAndConditionURL = "Global.t_n_c_url"
                const val SoftPosMinBalance = "Global.soft_pos_min"
                const val SoftPosMaxBalance = "Global.soft_pos_max"
                const val PWT_MIN = "Global.pwt_min"
                const val PWT_MAX = "Global.pwt_max"
                const val ConfigType = "Global.configType"
                const val LastTranId = "Global.last_tran_id"
                const val BankListData = "Global.bank_list_data"
                const val EntityTypeArray = "Global.EntityTypeArray"
                const val EntitySubTypeArray = "Global.EntitySubTypeArray"
                const val DefaultAccountArray = "Global.DefaultAccountArray"
                const val isTerminalLinked = "Global.isTerminalLinked"
                const val TMK = "Global.TMK"
                const val TPK = "Global.TPK"
                const val TSK = "Global.TSK"
                const val MERCHANTCODE = "Global.merchant_code"
                const val MERCHANTID = "Global.merchant_Id"
                const val MERCHANTNAME = "Global.merchant_Name"
                /*default values for TPK TMK and etc*/
                const val PLAIN_TSK = "6EFB5713FBE5D50229490D8C67BF45C7"
                const val PLAIN_TPK = "45573749F2DCD38C92341531CB3DFDB5"
                const val PLAIN_TMK = "2FA25E9D6E76BF024F13B09EE0FEB085"
                const val MERCHANT_ID_NFC = "2058LA021437700"
                const val MERCHANT_CODE_NFC = "6012"
                const val MERCHANT_NAME_NFC = "aicortex DIGITAL SYSTEMS LA           LANG"

            }
        }

        interface User {
            companion object {
                const val billAmount = "User.billAmount"
                const val clientID = "User.clientID"
                const val isBiometricEnableKey = "User.isBiometricEnable"
                const val biometricUUID = "User.biometricUUID"
                const val isFirstTimeLogin = "User.isFirstTimeLogin"
                const val LoggedOut = "User.isLoggedOut"
                const val msisdn = "User.msisdn"
                const val tempMsisdn = "User.temp_msisdn"
                const val email = "User.email"
                const val userFirstName = "User.userFirstName"
                const val userLastName = "User.userLastName"
                const val MSISDN_OTP = "MSISDN_OTP"
                const val PARENT_ID = "User.parentId"
            }
        }
    }
    companion object{
        //private val MSISDN_OTP = "MSISDN_OTP"
        var billAmount: Int?
            get() = prefs?.getInt(Keys.User.billAmount, 0)
            set(billAmount) {
                put(Keys.User.billAmount, billAmount)?.apply()
            }
        var tokenKey:String?
            get() = prefs?.getString(Keys.Global.TOKEN_KEY,"Bearer 2613d23b-042a-41b2-98a8-ed709cfdc0c4")
            set(token) {
                put(Keys.Global.TOKEN_KEY,token)?.apply()
            }

        var entityType:String?
            get() = prefs?.getString(Keys.Global.ENTITY_TYPE,"82")
            set(entityType) {
                put(Keys.Global.ENTITY_TYPE,entityType)?.apply()
            }

        var entitySubType:String?
            get() = prefs?.getString(Keys.Global.ENTITY_SUB_TYPE,"0")
            set(entitySubType) {
                put(Keys.Global.ENTITY_SUB_TYPE,entitySubType)?.apply()
            }
        var entityId:String?
            get() = prefs?.getString(Keys.Global.ENTITY_ID,"")
            set(entityType) {
                put(Keys.Global.ENTITY_ID,entityType)?.apply()
            }

        var businessName:String?
            get() = prefs?.getString(Keys.Global.BusinessName,"")
            set(businessName) {
                put(Keys.Global.BusinessName,businessName)?.apply()
            }

        var msisdn:String?
            get() = prefs?.getString(Keys.User.msisdn,"")
            set(msisdn) {
                put(Keys.User.msisdn,msisdn)?.apply()
            }
        var tempMsisdn:String?
            get() = prefs?.getString(Keys.User.tempMsisdn,"")
            set(tempMsisdn) {
                put(Keys.User.tempMsisdn,tempMsisdn)?.apply()
            }

        var email:String?
            get() = prefs?.getString(Keys.User.email,"")
            set(email) {
                put(Keys.User.email,email)?.apply()
            }

        var bioMetricUUID: String?
            get() = prefs?.getString(Keys.User.biometricUUID, "")
            set(isBiometricEnable) {
                put(Keys.User.biometricUUID, isBiometricEnable)?.apply()
            }
        var isBiometricEnable: Boolean
            get() = prefs?.getBoolean(Keys.User.isBiometricEnableKey, false)!!
            set(flag: Boolean) {
                put(Keys.User.isBiometricEnableKey, flag)?.apply()
            }
        var isFirstTimeLogin: Boolean
            get() = prefs?.getBoolean(Keys.User.isFirstTimeLogin, false)!!
            set(flag) {
                put(Keys.User.isFirstTimeLogin, flag)?.apply()
            }

        var floorLimit: Int
            get() = prefs?.getInt(Keys.Global.FLOOR_LIMIT, 0)!!
            set(floorLimit) {
                put(Keys.Global.FLOOR_LIMIT, floorLimit)?.apply()
            }

        /*getSetting API parameters*/

        var UpdatedVersion: String?
            get() = prefs?.getString(Keys.Global.UpdatedVersion, "0")
            set(UpdatedVersion) {
                put(Keys.Global.UpdatedVersion, UpdatedVersion)?.apply()
            }
        var ForceUpdateVersion: String?
            get() = prefs?.getString(Keys.Global.ForceUpdateVersion, "0")
            set(ForceUpdateVersion) {
                put(Keys.Global.ForceUpdateVersion, ForceUpdateVersion)?.apply()
            }
        var maintenanceMode: String?
            get() = prefs?.getString(Keys.Global.MaintenanceMode, "0")
            set(maintenanceMode) {
                put(Keys.Global.MaintenanceMode, maintenanceMode)?.apply()
            }
        var PrivacyPolicyURL: String?
            get() = prefs?.getString(Keys.Global.PrivacyPolicyURL, "")
            set(PrivacyPolicyURL) {
                put(Keys.Global.PrivacyPolicyURL, PrivacyPolicyURL)?.apply()
            }
        var TermAndConditionURL: String?
            get() = prefs?.getString(Keys.Global.TermAndConditionURL, "")
            set(TermAndConditionURL) {
                put(Keys.Global.TermAndConditionURL, TermAndConditionURL)?.apply()
            }
        var minTrnAmount: String?
            get() = prefs?.getString(Keys.Global.SoftPosMinBalance, "50")
            set(minTrnAmount) {
                put(Keys.Global.SoftPosMinBalance, minTrnAmount)?.apply()
            }
        var maxTrnAmount: String?
            get() = prefs?.getString(Keys.Global.SoftPosMaxBalance, "0")
            set(maxTrnAmount) {
                put(Keys.Global.SoftPosMaxBalance, maxTrnAmount)?.apply()
            }
        var PWT_MAX: String?
            get() = prefs?.getString(Keys.Global.PWT_MAX, "0")
            set(PWT_MAX) {
                put(Keys.Global.PWT_MAX, PWT_MAX)?.apply()
            }
        var PWT_MIN: String?
            get() = prefs?.getString(Keys.Global.PWT_MIN, "0")
            set(PWT_MIN) {
                put(Keys.Global.PWT_MIN, PWT_MIN)?.apply()

            }
        var ConfigType: String?
            get() = prefs?.getString(Keys.Global.ConfigType, "")
            set(SoftPosMinBalance) {
                put(Keys.Global.ConfigType, ConfigType)?.apply()
            }
        var DeviceID: String?
            get() = prefs?.getString(Keys.Global.DEVICE_ID, "")
            set(deviceid) {
                put(Keys.Global.DEVICE_ID, deviceid)?.apply()
            }
        var EntityTypeArray: String?
            get() = prefs?.getString(Keys.Global.EntityTypeArray, null)
            set(EntityTypeArray) {
                put(Keys.Global.EntityTypeArray, EntityTypeArray)?.apply()
            }

        var EntitySubTypeArray: String?
            get() = prefs?.getString(Keys.Global.EntitySubTypeArray, null)
            set(EntitySubTypeArray) {
                put(Keys.Global.EntitySubTypeArray, EntitySubTypeArray)?.apply()
            }

        var DefaultAccountArray: String?
            get() = prefs?.getString(Keys.Global.DefaultAccountArray, "")
            set(DefaultAccountArray) {
                put(Keys.Global.DefaultAccountArray, DefaultAccountArray)?.apply()
            }
        var UserFirstName: String?
            get() = prefs?.getString(Keys.User.userFirstName, "")
            set(UserFirstName) {
                put(Keys.User.userFirstName, UserFirstName)?.apply()
            }
        var UserLastName: String?
            get() = prefs?.getString(Keys.User.userLastName, "")
            set(UserFirstName) {
                put(Keys.User.userLastName, UserFirstName)?.apply()
            }
        var isTerminalLinked: Boolean
            get() = prefs?.getBoolean(Keys.Global.isTerminalLinked, false)!!
            set(flag: Boolean) {
                put(Keys.Global.isTerminalLinked, flag)?.apply()
            }
        var TMK:String?
            get() = prefs?.getString(Keys.Global.TMK,Keys.Global.PLAIN_TMK)
            set(TMK) {
                put(Keys.Global.TMK,TMK)?.apply()
            }
        var TPK:String?
            get() = prefs?.getString(Keys.Global.TPK,Keys.Global.PLAIN_TPK)
            set(TPK) {
                put(Keys.Global.TPK,TPK)?.apply()
            }
        var TSK:String?
            get() = prefs?.getString(Keys.Global.TSK,Keys.Global.PLAIN_TSK)
            set(TSK) {
                put(Keys.Global.TSK,TSK)?.apply()
            }
        var MERCHANTCODE:String?
            get() = prefs?.getString(Keys.Global.MERCHANTCODE,Keys.Global.MERCHANT_CODE_NFC)
            set(MERCHANTCODE) {
                put(Keys.Global.MERCHANTCODE,MERCHANTCODE)?.apply()
            }

        var MERCHANTID:String?
            get() = prefs?.getString(Keys.Global.MERCHANTID,Keys.Global.MERCHANT_ID_NFC)
            set(MERCHANTID) {
                put(Keys.Global.MERCHANTID,MERCHANTID)?.apply()
            }

        var MERCHANTNAME:String?
            get() = prefs?.getString(Keys.Global.MERCHANTNAME,Keys.Global.MERCHANT_NAME_NFC)
            set(MERCHANTNAME) {
                put(Keys.Global.MERCHANTNAME,MERCHANTNAME)?.apply()
            }
        var MSISDN_OTP: String?
            get() = prefs?.getString(Keys.User.MSISDN_OTP, null)
            set(msisdn_otp) {
                put(Keys.User.MSISDN_OTP, msisdn_otp)?.apply()
            }
        var parentID:String?
            get() = prefs?.getString(Keys.User.PARENT_ID,null)
            set(parentID) {
                put(Keys.User.PARENT_ID,parentID)?.apply()
            }
        var lastTranId: String?
            get() = prefs?.getString(Keys.Global.LastTranId, "0")
            set(lastTranId) {
                put(Keys.Global.LastTranId, lastTranId)?.apply()
            }

        var bankListData: String?
            get() = prefs?.getString(Keys.Global.BankListData, "")
            set(bankListData) {
                put(Keys.Global.BankListData, bankListData)?.apply()
            }

        var loggedOut: Boolean
            get() = prefs?.getBoolean(Keys.User.LoggedOut, false)!!
            set(loggedOut) {
                put(Keys.User.LoggedOut, loggedOut)?.apply()
            }

    }
}