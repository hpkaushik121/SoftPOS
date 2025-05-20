package com.aicortex.softpos.utils;

import android.util.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.aicortex.softpos.creditCardNfcReader.parser.TLV;
import com.aicortex.softpos.creditCardNfcReader.parser.TLVParser;
import com.aicortex.softpos.mpos.TripleDesCipher;

import org.jpos.iso.ISOMsg;
/**
 * replacement of MPOSCONSTANT in
 * validate terminal api and downlaod
 * terminal data api
 */
public class SoftPosConstants {

    public static final String PURCHASE = "0200";
    public static final String REFUND = "0200";
    public static final String CASHADVANCE = "0200";
    public static final String REVERSAL = "0420";
    public static final String PRE_AUTHRIZATION = "0100";
    public static final String PRE_AUTHRIZATION_COMPLETION = "0220";
    public static final String BALANCE = "0100";
    public static final String MAX_TRASACTION_LIMIT="250000";
    public static final String DOWNLOAD_KEY = "download_key";
    public static final String GET_TERMINAL_ID = "get_terminal_id";
    public static final String UPDATE_FIRMWARE = "update_firmware";
    public static final String VOID = "0420";
    public static final String ACTIVATION = "2200";
    public static final String PURCHASE_WITHCASH = "0200";

    public static final String PURCHASE_PROCESSCODE = "000000";
    public static final String REFUND_PROCESSCODE = "200000";
    public static final String REVERSAL_PROCESSCODE = "000000";
    public static final String PURCHASE_WITHCASH_PROCESSCODE = "090000";
    public static final String CASHADVANCE_PROCESSCODE = "010000";
    public static final String BALANCE_PROCESSCODE = "310000";
    public static final String PRE_AUTHRIZATION__PROCESSCODE = "600000";
    public static final String PRE_AUTHRIZATIONCOMP_PROCESSCODE = "610000";
    public static final String VOID_PROCESSCODE = "000000";
    public static final String ACTIVATION_PROCESSCODE = "001000";

    public static final String DEVICE_VALIDATION_REQUEST = "DEVICEVALIDATION";
    public static final String VALIDATION_NOTIFICATION_REQUEST = "VALIDATIONNOTIFICATION";
    public static final String TERMINAL_DATA_REQUEST = "TERMINALDATA";
    public static final String CALL_HOME_REQUEST = "CALLHOME";
    public static final String DAILY_TRANSACTIONS_REQUEST = "DAILYTRANSACTIONS";
    public static final String PREVIOUS_TRANSACTION_REQUEST = "PREVIOUSTRANSACTION";

    public static final String CHECK_BALANCE_REQUEST = "CHECKBALANCE";
    public static final String PURCHASE_REQUEST = "PURCHASE";
    public static final String PURCHASE_WITH_CASHBACK_REQUEST = "PURCHASEWITHCASHBACK";
    public static final String CASH_ADVANCE_REQUEST = "CASHADVANCE";
    public static final String PREAUTHORIZATION_REQUEST = "PREAUTHORIZATION";
    public static final String PREAUTH_COMPLIATION_REQUEST = "PREAUTHCOMPLIATION";
    public static final String REFUND_REQUEST = "REFUND";
    public static final String REVERSAL_REQUEST = "REVERSAL";


    public static final String VOID_RESPONSECODE = "17";
    public static final String REVERSAL_RESPONSECODE = "60";

    public static final String DATEFORMAT = "MM/dd/yyyy HH:mm:ss";
    //public static final String component = "1d";
    //public static final String data = "1f";
    public static final String component = Character.toString((char) 29);
    public static final String data = Character.toString((char) 31);
    //sale response codes
    public static final String APPROVE = "00";
    public static final String FORMAT_ERROR = "05";
    public static final String INVALID_MERCHANT = "30";
    public static final String INVALID_BIN = "31";
    public static final String CALL_ACQUIRER = "60";
    public static final String CALL_ISSUER = "70";
    public static final String BAD_BATCH_NUMBER = "80";
    public static final String BATCH_NOT_FOUND = "85";
    public static final String SYSTEM_NOT_AVAILABLE = "91";

    public static final Map<String, String> SWITCHRESULTDESCRIPTION = new HashMap<String, String>() {{

        put("00", "Approved or completed successfully");
        put("01", "Refer to card issuer");
        put("02", "Refer to card issuer, special condition");
        put("03", "Invalid merchant");
        put("04", "Pick-up card");
        put("05", "Do not honor");
        put("06", "Error");
        put("07", "Pick-up card, special condition");
        put("08", "Honor with identification");
        put("09", "Request in progress");
        put("10", "Approved, partial");
        put("11", "Approved, VIP");
        put("12", "Invalid transaction");
        put("13", "Invalid amount");
        put("14", "Invalid card number");
        put("15", "No such issuer");
        put("16", "Approved, update track 3");
        put("17", "Customer cancellation");
        put("18", "Customer dispute");
        put("19", "Re-enter transaction");
        put("20", "Invalid response");
        put("21", "No action taken");
        put("22", "Suspected malfunction");
        put("24", "File update not supported");
        put("25", "Unable to locate record");
        put("26", "Duplicate record");
        put("27", "File update field edit error");
        put("28", "File update file locked");
        put("29", "File update failed");
        put("30", "Format error");
        put("31", "Bank not supported");
        put("32", "Completed partially");
        put("33", "Expired card, pick-up");
        put("34", "Suspected fraud, pick-up");
        put("35", "Contact acquirer, pick-up");
        put("36", "Restricted card, pick-up");
        put("37", "Call acquirer security, pick-up");
        put("38", "PIN tries exceeded, pick-up");
        put("39", "No credit account");
        put("40", "Function not supported");
        put("41", "Lost card, pick-up");
        put("42", "No universal account");
        put("43", "Stolen card, pick-up");
        put("44", "No investment account");
        put("45", "Account closed");
        put("46", "Identification required");
        put("47", "Identification cross-check required");
        put("48", "No customer record");
        put("49", "Reserved for future Realtime use");
        put("50", "Reserved for future Realtime use");
        put("51", "Not sufficient funds");
        put("52", "No check account");
        put("53", "No savings account");
        put("54", "Expired card");
        put("55", "Incorrect PIN");
        put("56", "No card record");
        put("57", "Transaction not permitted to cardholder");
        put("58", "Transaction not permitted on terminal");
        put("59", "Suspected fraud");
        put("60", "Contact acquirer");
        put("61", "Exceeds withdrawal limit");
        put("62", "Restricted card");
        put("63", "Security violation");
        put("64", "Original amount incorrect");
        put("65", "Exceeds withdrawal frequency");
        put("66", "Call acquirer security");
        put("67", "Hard capture");
        put("68", "Response received too late");
        put("69", "Advice received too late");
        put("70", "to 74 Reserved for future Realtime use");
        put("75", "PIN tries exceeded");
        put("76", "Reserved for future Realtime use");
        put("77", "Intervene, bank approval required");
        put("78", "Intervene, bank approval required for partial amount");
        put("79", "Reserved for client-specific use (declined)");
        put("80", "Reserved for client-specific use (declined)");
        put("81", "Reserved for client-specific use (declined)");
        put("82", "Reserved for client-specific use (declined)");
        put("83", "Reserved for client-specific use (declined)");
        put("84", "Reserved for client-specific use (declined)");
        put("85", "Reserved for client-specific use (declined)");
        put("86", "Reserved for client-specific use (declined)");
        put("87", "Reserved for client-specific use (declined)");
        put("88", "Reserved for client-specific use (declined)");
        put("89", "Reserved for client-specific use (declined)");
        put("90", "Cut-off in progress");
        put("91", "Issuer or switch inoperative");
        put("92", "Routing error");
        put("93", "Violation of law");
        put("94", "Duplicate transaction");
        put("95", "Reconcile error");
        put("96", "System malfunction");
        put("97", "Reserved for future Realtime use");
        put("98", "Exceeds cash limit");

    }};


    public static final Map<String, String> CORERESULTDESCRIPTIONRESULTCODE = new HashMap<String, String>() {{
        put("00", "00");
        put("7004", "7004");
        put("7005", "7005");
        put("7002", "7002");
        put("1000", "1000");
        put("1000", "1000");
    }};
    public static final String POS_FIRMWARE_PATH = "pos_firmware_path";


    public static void printISOMessage(ISOMsg isoMsg) {
        try {
            Log.d("MTI ::%s%n", isoMsg.getMTI());
            for (int i = 1; i <= isoMsg.getMaxField(); i++) {
                if (isoMsg.hasField(i)) {
                    Log.d("Field (" + i + ") ::", isoMsg.getString(i));
                }
            }
        } catch (ISOException e) {
            e.printStackTrace();
        }
    }

    public static String getRandomUnique(int length) {
        long timeStamp = System.currentTimeMillis();
        String randomNum = RandomStringUtils.randomNumeric(length / 2);
        String timNum = (timeStamp + "").substring(length / 2);
        String str = timNum + "" + randomNum;
        str = str.substring(0, length);
        return str;
    }

    public static String getIdentificationCode(String realPAN) {
        return realPAN.substring(0, 6);
    }

    public static String getServiceRestrictionCode(String track2) {
        int index = track2.indexOf("D") + 5;
        return track2.substring(index, index + 3);

    }


    public static final String FIELD_EMV_22 = "051";
    public static final String FIELD_MCR_22 = "021";
    public static final String FIELD_DEF_22 = "051";
    public static final String FIELD_25 = "00";
    public static final String FIELD_23 = "001";
    public static final String FIELD_40 = "221";
    public static final String FIELD_26 = "12";
    /**
     * tks  this should be of 8 digit as per segun
     */
    public static final String FIELD_28 = "D00000000";
    public static final String FIELD_28_REFUND = "C00000000";
    public static final String FIELD_42 = "2032LA000020256";
    public static final String FIELD_43 = "FreedomNetwork LANG3FA0AD88B712656D00AD6";
    //    public static final String FIELD_43 = "UNION DEDICATED MERC   LA           LANG";
    public static final String FIELD_49 = "566";
    public static final String FIELD_123_PREFIX = "511101512344101";
    public static final String FIELD_123_VEREVE = "510101711344101";
    //    public static final String FIELD_123_VEREVE = "A10101711344101";
    public static final String MERCHANT_ID = "2302SO000056452";

    public static List<TLV> list=new ArrayList<>();
    public static void getDataFormat(String terminalData) {

        try {
            String[] arr = terminalData.split("\\|");
            String tmk = arr[0].trim();
            String tmkKCV = tmk.substring(tmk.length() - 6);
            tmk = tmk.substring(0, tmk.length() - 6);

            String tpk = arr[1].trim();
            String tpkKCV = tpk.substring(tpk.length() - 6);
            tpk = tpk.substring(0, tpk.length() - 6);


            String tsk = arr[2].trim();
            String tskKCV = tsk.substring(tsk.length() - 6);
            tsk = tsk.substring(0, tsk.length() - 6);


            String deviceData = arr[3].trim();
            try {
                parseDeviceData(deviceData);


            } catch (Exception e) {
                e.printStackTrace();
            }

//            map.put(Constants.TMK, tmk);
//            map.put(Constants.TMK_KCV, tmkKCV);
            String PLAIN_TMK = getDecryption(Constants.MASTER_KEY, tmk);

            String PLAIN_TPK = getDecryption(PLAIN_TMK, tpk);
            String PLAIN_TSK = getDecryption(PLAIN_TMK, tsk);
//            map.put(Constants.DEVICE_DATA, deviceData);
//            Log.d("PLAIN_TMK====>", map.get(Constants.PLAIN_TMK));
//            Log.d("PLAIN_TPK====>", map.get(Constants.PLAIN_TPK));
//            Log.d("PLAIN_TSK====>", map.get(Constants.PLAIN_TSK));
            TLV merchantId = TLVParser.searchTLV(list, "03");
            TLV merchantName = TLVParser.searchTLV(list, "52");
            TLV merchantCode = TLVParser.searchTLV(list, "08");
            Log.d("PLAIN_MERCHANT_ID====>", merchantId.value);
            Log.d("PLAIN_MERCHANT_CODE===>", merchantCode.value);
            Log.d("PLAIN_MERCHANT_NAME===>", merchantName.value);
//            AppSharedPreferences.writeString(Constants.PLAIN_TSK, map.get(Constants.PLAIN_TSK));
//            String caKeys = arr[4].trim();
//            String aid = arr[5].trim();

//            map.put(Constants.CA_KEYS, caKeys);
//            map.put(Constants.AID, aid);
            SharedPrefUtils.Companion.setTMK(PLAIN_TMK);
            SharedPrefUtils.Companion.setTPK(PLAIN_TPK);
            SharedPrefUtils.Companion.setTSK(PLAIN_TSK);

            SharedPrefUtils.Companion.setMERCHANTCODE(merchantCode.value);
            SharedPrefUtils.Companion.setMERCHANTID(merchantId.value);
            SharedPrefUtils.Companion.setMERCHANTNAME(merchantName.value);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void parseDeviceData(String deviceData) {
        if(deviceData.length()!=0){
            String tag=deviceData.substring(0,2);
            int lengthOfString=Integer.parseInt(deviceData.substring(2,5));
            String data="";
            try{
                data=deviceData.substring(5,5+lengthOfString);
            }catch (Exception e){
                e.printStackTrace();
                data=deviceData.substring(5);
            }
            TLV tlv=new TLV();
            tlv.tag=tag;
            tlv.length= String.valueOf(lengthOfString);
            tlv.value=data;
            list.add(tlv);
            parseDeviceData(deviceData.substring(5+lengthOfString));
        }


    }
    public static String getEncryption(String key, String data) throws Exception {
        TripleDesCipher tripleDesCipher = new TripleDesCipher(hexStringToByteArray(key));
        byte[] bytes = tripleDesCipher.encrypt(hexStringToByteArray(data));
        String encData = byteArrayToHex(bytes);
        return encData;
    }

    public static String getDecryption(String key, String data) throws Exception {
        TripleDesCipher tripleDesCipher = new TripleDesCipher(hexStringToByteArray(key));
        byte[] bytes = tripleDesCipher.decrypt(hexStringToByteArray(data));
        String encData = byteArrayToHex(bytes);
        return encData;
    }

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString().toUpperCase();
    }


}

