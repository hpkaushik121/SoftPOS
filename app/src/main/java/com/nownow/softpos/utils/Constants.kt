package com.aicortex.softpos.utils

import java.util.regex.Pattern


object Constants {


    /** sdk keys */
    const val APPSFLYER_PROJECT_KEY="tWQoFUbna7RVEzBiSmbEii"
    //const val APPSFLYER_STAG_KEY="tWQoFUbna7RVEzBiSmbEii"
    /** sdk keys */

    /** events title */
    const val onboarding_proceed_clicked="onboarding_proceed_clicked"
    /** events title */

    /** Api Constants*/
    const val PLATFORM="Android"
    /** Api Constants keys*/
    const val OTP_ON_SMS_OR_EMAIL = "otp_on_sms_or_email"
    const val REQUEST_FROM = "request_from"
    const val TRANSACTION_TYPE = "transaction_type"


    const val IS_BIO_ACTIVE = "is_bio_active"
    const val LOGIN_VIA = "login_via"
    const val LOGIN_VIA_EMAIL = "login_via_email"
    const val LOGIN_VIA_WALLET = "login_via_wallet"
    const val VERIFY_PIN_FROM_WALLET = "verify_pin_from_wallet"
    const val FROM_FORGET_PIN = "from_forget_pin"
    const val TERMINAL_LINK_SUCCESSFUL = "terminal_link_successful"
    const val FROM_MOVE_FUND_WALLET = "from_move_fund_wallet"

    /** application constant keys*/

    /** constant values */

    const val PAYMENT_SUCCESS = "Success"
    const val PAYMENT_FAILED = "Failed"
    const val TRANSACTION_TYPE_CONTACT_LESS = "contact_less"
    const val TRANSACTION_TYPE_PAY_WITH_TRANSFER = "pay_with_transfer"
    const val TRANSACTION_TYPE_QR = "qr_pay"

    //const val SOFT_POS_SERVICE_TYPE="506"
    const val KYC_3_USER="39"
    const val KYC_2_USER="38"
    const val KYC_1_USER="37"
    const val SOFT_POS_SERVICE_TYPE_TAP_TAP="842"
    const val SOFT_POS_SERVICE_TYPE_PWT="810"
    const val MOVE_FUND_WALLET_SERVICE_TYPE="112"
    const val SHARED_SECRET_KEY_VALUE = "eyJhbGciOiJub25lIn0.eyJzdWIiOiI3OTMyMTEyNDk5MTg0In0"
    const val CONFIG_TYPE = "softposStandAlone"
    const val OTP_LENGTH_4 = "4"
    const val OTP_LENGTH_6 = "6"
    const val LOG_D_KEY = "LOG_DATA"
    const val CORE_AUTH_KEY_US_PW = "apiClient:apiClientSecret"
    const val BEARER = "Bearer "
    const val BASIC = "Basic "
    const val CHANNEL_ID_23 = "23"
    const val CHANNEL_ID_22 = "22"
    const val SURROUND_SYSTEM_1 = "1"
    const val SURROUND_SYSTEM_3 = "3"
    const val SURROUND_SYSTEM_4 = "4"
    const val SERVICE_TYPE_ZERO = "0"
    const val WALLET_TYPE_TO_DEDUCT_CREDIT = 1
    const val TRANSACTION_COUNT_FOR_DASHBOARD = "10"
    const val WALLET = "WALLET"
    const val WALLET_NUMBER = "wallet_number"
    const val DASHBOARD_ACTIVITY = "dashboard_activity"
    const val EMAIL = "EMAIL"
    const val LOG_D_RESPONSE = "Log_d_response"
    const val LOG_D_REQUEST = "Log_d_request"
    const val ALREADYLOGIN = "Already_login"
    const val FROM_LOGIN = "from_login"
    const val FROM_PIN_VALIDATION = "from_pin_validation"
    const val FROM_OTPVALIDATE = "from_otp_valid"
    const val LOGIN_SUCCESS = "login_success"
    const val SERVICE_NAME_REPLACE ="Cash in NIBS"
    const val BIOMETRIC = "biometric"
    const val COUNTER_TIME_FIRST = (5 * 60 * 1000).toLong()
    const val COUNTER_TIME_SECOND = (10 * 60 * 1000).toLong()
    const val COUNTER_TIME_THIRD = (15 * 60 * 1000).toLong()
    /** constant values */

    const val EDITTEXTVALUE = "edittext_value"
    const val TYPEWALLETOREMAIL = "walletOrEmail"
    const val TIMER_DURATION = 10 * 1000
    const val RESET_TIMER_DURATION: Long = 30 * 1000
   // const val MASTER_KEY = "619C213428CFC2F1F0EB820478073A7F"
    const val MASTER_KEY = "3CDDE1CC6FDD225C9A8BC3EB065509A6" //given by emmanual
    /** intent keys */
    const val CLIENT_ID = "CLIENT_ID"
    const val IS_NFC = "IS_NFC"
    const val CARD_PAN = "CARD_PAN"
    //const val SERVICE_ID = "506"
    //const val SERVICE_ID = "842"
    const val CARD_TYPE = "NFC"
    const val VALIDATE_INFO = "validateInfo"
    const val TERMINAL_VALIDATE_RESPONSE_INFO="terminalValidateInfo";
    const val BILL_AMOUNT = "BILL_AMOUNT"
    const val PAYMENT_STATUS = "PAYMENT_STATUS"
    const val MSISDN = "MSISDN"
    const val TRANSACTIONID = "TRANSACTIONID"
    const val TRANSACTION_STATUS = "TRANSACTION_STATUS"
    const val TRANSACTION_DATE = "TRANSACTION_DATE"
    const val TRANSACTION_SENDER_NAME = "TRANSACTION_SENDER_NAME"
    const val TRANSACTION_RECEIVER_NAME = "TRANSACTION_SENDER_NAME"
    const val TRANSACTION_SENDER_NUMBER = "TRANSACTION_SENDER_NUMBER"
    const val TRANSACTION_RECEIVER_NUMBER = "TRANSACTION_SENDER_NUMBER"
    const val CARD_TP = "CARD_TYPE"
    const val REMARK = "REMARK"
    const val RRN = "RRN"
    const val STAN = "STAN"
    const val CARD_NUMBER = "CARDNUMBER"
    const val ACCOUNT_TYPE_SELECTED = "ACCOUNT_TYPE_SELECTED"
    const val CARD_PIN_BLOCK = "CARD_PIN_BLOCK"
    const val FROM_TX_HISTORY = "from_tx_history"
    const val FROM_ACCOUNT_DETAILS = "from_account_details"
    const val FROM_PAYMENT_PAGE = "from_payment_page"
    const val FROM_TX_DETAIL_PAGE = "from_tx_detail_page"
    const val WALLET1_BALANCE_AMOUNT = "wallet1_balance_amount"
    const val SERVICE_CONTACT_LESS = "Contactless Payment"
    const val SERVICE_PAY_VIA_QR = "Pay via QR"
    const val SERVICE_PAY_WITH_TRANSFER = "Pay with transfer"
    const val SERVICE_TYPE = "service_type"
    const val SERVICE_NAME = "service_name"
    /** intent keys */

    /** shared prefs keys */

    /** shared prefs keys */


    val EMAIL_ADDRESS_PATTERN: Pattern? = Pattern.compile(
        "[a-zA-Z0-9][a-zA-Z0-9\\.\\_\\-]{0,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
}