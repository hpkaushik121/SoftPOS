package com.nownow.softpos.services;

import android.util.Log;

import com.nownow.softpos.application.TapNpayApplication;
import com.nownow.softpos.models.softpos.CardDetails;
import com.nownow.softpos.models.softpos.TransactionInfo;
import com.nownow.softpos.utilities.UtilsHandlerPos;
import com.nownow.softpos.utils.SoftPosConstants;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;




public class ISOProcessor {
    HashMap<String,String> cardData;
    HashMap<String,String> map;
    GenericPackager packager;

    public ISOProcessor(HashMap<String,String> cardData, HashMap<String,String> map) throws IOException, ISOException {
        try {
            this.cardData = cardData;
            this.map = map;
            packager = new GenericPackager(TapNpayApplication.appInstance.getAssets().open("iso87ascii.xml"));
        }catch (Exception e) {
            String str=e.getMessage();
            System.out.println(""+str);
        }
    }
    public static byte[] prepareByteStream(byte[] isoBytes) throws ISOException {

        String content = new String(isoBytes);
        int x = content.length();

        String binlng = Integer.toBinaryString(x);

        String headerlenght = Integer.toHexString(Integer.parseInt(binlng, 2));

        headerlenght = String.format("%4s", headerlenght).replace(' ', '0');

        String contenttohex = "";
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) <= 9) {
                contenttohex = contenttohex + '0';
            }
            contenttohex = contenttohex + (Integer.toHexString(content.charAt(i)));
        }
        // Building the Message with Header and ISO Message
        /**commented by takendra as it was adding extra byte to NIBBS request.*/
        //String completemsg = headerlenght + contenttohex;
        String completemsg =  contenttohex;



        // Log.d("UniversalLibrary","Request Message: " + completemsg);

        byte[] databyte = UtilsHandlerPos.hexToByte(completemsg.trim().toUpperCase());

        return databyte;
    }

    public static void printISOMessage(ISOMsg isoMsg) {
        try {
            Log.d("MTI ::%s%n", isoMsg.getMTI());
            for (int i = 1; i <= isoMsg.getMaxField(); i++) {
                if (isoMsg.hasField(i)) {
                    Log.d("Field ("+i+") ::",  isoMsg.getString(i));
                }
            }
        } catch (ISOException e) {
            e.printStackTrace();
        }
    }

    public static String generateHash256Value(byte[] msg, String key) throws UnsupportedEncodingException {
        MessageDigest m = null;
        String hashText = null;
        byte[] actualKeyBytes = UtilsHandlerPos.hexStringToBytes(key);

        try {
            m = MessageDigest.getInstance("SHA-256");
            m.update(actualKeyBytes, 0, actualKeyBytes.length);
            m.update(msg, 0, msg.length);
            hashText = new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        if (hashText.length() < 64) {
            int numberOfZeroes = 64 - hashText.length();
            String zeroes = "";

            for (int i = 0; i < numberOfZeroes; i++)
                zeroes = zeroes + "0";

            hashText = zeroes + hashText;

            ////LOGger.info("Utility :: generateHash256Value :: HashValue with zeroes: " + hashText);
        }

        return hashText;

    }
    public static String replace(String str, int[] indexes, char[] replace){
        char[] chars = str.toCharArray();
        for (int index=0;index<indexes.length;index++) {
            if(str==null){
                return str;
            }else if(index<0 || index>=str.length()){
                return str;
            }
            chars[indexes[index]] = replace[index];
        }

        return String.valueOf(chars);
    }

    public ISOMsg createISOPurchaseRequest(CardDetails cardDetails, TransactionInfo validateInfo, String accountType) throws Exception {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);

        isoMsg.setMTI(SoftPosConstants.PURCHASE);
        isoMsg.set(2, cardData.get("realPAN"));
        String processCode=SoftPosConstants.PURCHASE_PROCESSCODE;
        processCode= replace(processCode, new int[]{2, 3},accountType.toCharArray());
        isoMsg.set(3, processCode);
        isoMsg.set(4, ISOUtil.zeropad(cardData.get("amount"), 12));
        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, validateInfo.getStan());
        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");
        String time = isoMsg.getString(7).substring(4, 10);
        String date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');
        isoMsg.set(12, time);
        isoMsg.set(13, date);
        isoMsg.set(14, cardData.get("expiryDate"));

        isoMsg.set(18,  merchantCode.substring(0, 4));

        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);
        if(!cardDetails.getPanSeq().equals("00")){
            isoMsg.set(23, "0"+cardDetails.getPanSeq());
        }


        isoMsg.set(32,SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));



        isoMsg.set(25, SoftPosConstants.FIELD_25);
        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28);


        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37, validateInfo.getRrn());
        isoMsg.set(40, SoftPosConstants.getServiceRestrictionCode(cardData.get("track2")));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));

        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));
        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));

        isoMsg.set(49, SoftPosConstants.FIELD_49);
        if(cardDetails.getPinBlock()!=null && !cardDetails.getPinBlock().equals(""))
        isoMsg.set(52, cardDetails.getPinBlock());


//        isoMsg.set(59, SoftPosConstants.getRandomUnique(6));
        isoMsg.set(55, cardDetails.getIccData());
        isoMsg.set(123,SoftPosConstants.FIELD_123_VEREVE);

//        result = isoMsg.pack();
//        result[35]++;

        return isoMsg;

    }




    public ISOMsg createISOCashAdvanceRequest(String additionalAmount) throws Exception {

        byte[] result = null;
        ISOMsg isoMsg = new ISOMsg();

        isoMsg.setPackager(packager);
        isoMsg.setMTI(SoftPosConstants.CASHADVANCE);
        isoMsg.set(2, cardData.get("realPAN"));
        isoMsg.set(3, SoftPosConstants.CASHADVANCE_PROCESSCODE);
        isoMsg.set(4, ISOUtil.zeropad(cardData.get("amount"), 12));
        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, SoftPosConstants.getRandomUnique(6));

        String time = isoMsg.getString(7).substring(4, 10);
        String date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');
        isoMsg.set(12, time);
        isoMsg.set(13, date);
        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");

        isoMsg.set(14, cardData.get("expiryDate"));
        isoMsg.set(18, merchantCode.substring(0, 4));

        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);

        isoMsg.set(25, SoftPosConstants.FIELD_25);
        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28);
        isoMsg.set(32, SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));

        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37, SoftPosConstants.getRandomUnique(12));
        isoMsg.set(40, SoftPosConstants.getServiceRestrictionCode(cardData.get("track2")));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));

        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));
        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));

        isoMsg.set(49, SoftPosConstants.FIELD_49);
        isoMsg.set(54, "0000566D"+ISOUtil.zeropad(additionalAmount, 12));
        isoMsg.set(123,SoftPosConstants.FIELD_123_VEREVE);

//        result = isoMsg.pack();
//        result[35]++;
//
//        pos.calcMacDoubleSha(KeyInjection.byteArrayToHex(result), 0, 60);

        return isoMsg;
    }


    public ISOMsg createISOPurchaseCashbackRequest(String cashbackAmount) throws Exception {

        ISOMsg isoMsg = new ISOMsg();
        byte[] result = null;
        isoMsg.setPackager(packager);
        isoMsg.setMTI(SoftPosConstants.PURCHASE_WITHCASH);
        isoMsg.set(2, cardData.get("realPAN"));
        isoMsg.set(3, SoftPosConstants.PURCHASE_WITHCASH_PROCESSCODE);
        isoMsg.set(4, ISOUtil.zeropad(cardData.get("amount"), 12));
        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, SoftPosConstants.getRandomUnique(6));

        String time = isoMsg.getString(7).substring(4, 10);
        String date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');
        isoMsg.set(12, time);
        isoMsg.set(13, date);
        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");

        isoMsg.set(14, cardData.get("expiryDate"));
        isoMsg.set(18, merchantCode.substring(0, 4));

        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);


        isoMsg.set(25, SoftPosConstants.FIELD_25);
        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28);

        isoMsg.set(32, SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));

        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37, SoftPosConstants.getRandomUnique(12));
        isoMsg.set(40,SoftPosConstants.getServiceRestrictionCode(cardData.get("track2")));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));

        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));
        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));

        isoMsg.set(49, SoftPosConstants.FIELD_49);
//        isoMsg.set(54, ISOUtil.zeropad(cashbackAmount, 12));
        isoMsg.set(54, "0000566D"+ISOUtil.zeropad(cashbackAmount, 12));

        isoMsg.set(123, SoftPosConstants.FIELD_123_VEREVE);


        return isoMsg;
    }

    public ISOMsg createISOReversalRequest(String rrn) throws Exception {
        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");
        ISOMsg isoMsg = new ISOMsg();

        byte[] result = null;
        isoMsg.setPackager(packager);

        isoMsg.setMTI(SoftPosConstants.REFUND);
        isoMsg.set(2, cardData.get("realPAN"));
        isoMsg.set(3, SoftPosConstants.REFUND_PROCESSCODE);
        isoMsg.set(4, ISOUtil.zeropad(cardData.get("amount"), 12));

        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, SoftPosConstants.getRandomUnique(6));

        String time = "";
        String date = "";

        time = UtilsHandlerPos.getMPOSMMDDHHMMSS().substring(4, 10);
        date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');

        isoMsg.set(12, time);
        isoMsg.set(13, date);
        isoMsg.set(14, cardData.get("expiryDate"));
        isoMsg.set(18,  merchantCode.substring(0, 4));
        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);



        isoMsg.set(25, SoftPosConstants.FIELD_25);

        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28);
        isoMsg.set(32, SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));
        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37, rrn);
        isoMsg.set(40, SoftPosConstants.getServiceRestrictionCode(cardData.get("track2")));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));
        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));
        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));
        isoMsg.set(49, SoftPosConstants.FIELD_49);
        isoMsg.set(52, cardData.get("pinBlock"));
        isoMsg.set(55, cardData.get("decodedTLV"));

        isoMsg.set(56, "4000");

        StringBuilder nintyField = new StringBuilder();
        nintyField.append(SoftPosConstants.PURCHASE);
        nintyField.append(isoMsg.getString(11));
        nintyField.append(isoMsg.getString(7));
        nintyField.append(ISOUtil.zeropad(isoMsg.getString(32), 10));
        nintyField.append(ISOUtil.zeropad(isoMsg.getString(32), 10));

        isoMsg.set(90, nintyField.toString());


        StringBuilder nintyFiveField = new StringBuilder();
        nintyFiveField.append(isoMsg.getString(4));
        nintyFiveField.append("000000000000");
        nintyFiveField.append(isoMsg.getString(28));
        nintyFiveField.append(isoMsg.getString(28));

        isoMsg.set(95, nintyFiveField.toString());
        isoMsg.set(123, SoftPosConstants.FIELD_123_VEREVE);

//        result = isoMsg.pack();
//        result[35]++;
//        pos.calcMacDoubleSha(KeyInjection.byteArrayToHex(result), 0, 60);
        return isoMsg;
    }



//    public ISOMsg createISOReverseRequest(String mti) throws ISOException {
//        byte[] result = null;
//
//        isoMsg.setMTI(SoftPosConstants.REVERSAL);
//        isoMsg.set(3, SoftPosConstants.REVERSAL_PROCESSCODE);
//        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
//        isoMsg.set(40, getServiceRestrictionCode(cardData.get("track2")));
//        isoMsg.set(56, "4000");
//        StringBuilder nintyField = new StringBuilder();
//        nintyField.append(mti);
//        nintyField.append(this.isoMsg.getString(11));
//        nintyField.append(this.isoMsg.getString(7));
//        nintyField.append(ISOUtil.zeropad(this.isoMsg.getString(32), 10));
//        nintyField.append(ISOUtil.zeropad(this.isoMsg.getString(32), 10));
//
//        isoMsg.set(90, nintyField.toString());
//
//
//        StringBuilder nintyFiveField = new StringBuilder();
//        nintyFiveField.append(this.isoMsg.getString(4));
//        nintyFiveField.append("000000000000");
//        nintyFiveField.append(this.isoMsg.getString(28));
//        nintyFiveField.append(this.isoMsg.getString(28));
//
//        isoMsg.set(95, nintyFiveField.toString());
//        result = this.isoMsg.pack();
//        result[35]++;
//        pos.calcMacDoubleSha(KeyInjection.byteArrayToHex(result), 0, 60);
//    }



    public ISOMsg createISOBalanceRequest() throws Exception {

        ISOMsg isoMsg = new ISOMsg();

        byte[] result = null;
        isoMsg.setPackager(packager);

        isoMsg.setMTI(SoftPosConstants.BALANCE);
        isoMsg.set(2, cardData.get("realPAN"));
        isoMsg.set(3, SoftPosConstants.BALANCE_PROCESSCODE);
        isoMsg.set(4, ISOUtil.zeropad("000000000000", 12));
        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, SoftPosConstants.getRandomUnique(6));

        String time = "";
        String date = "";
        // Test Purpose only
        time = isoMsg.getString(7).substring(4, 10);
        date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');

        // System.out.println("ISO : 12 == " + time);
        isoMsg.set(12, time);
        isoMsg.set(13, date);
//            System.out.println("ISO : 13 == " + date);
        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");
        isoMsg.set(14, cardData.get("expiryDate"));
        isoMsg.set(18, merchantCode.substring(0, 4));

        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);


        isoMsg.set(25, SoftPosConstants.FIELD_25);
        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28);

        isoMsg.set(32, SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));

        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37, SoftPosConstants.getRandomUnique(12));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));

        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));

        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));

        isoMsg.set(49, SoftPosConstants.FIELD_49);

//        isoMsg.set(52, cardData.get("pinBlock"));
        isoMsg.set(123, SoftPosConstants.FIELD_123_VEREVE);
//        result = isoMsg.pack();
//        result[35]++;
//
//        pos.calcMacDoubleSha(KeyInjection.byteArrayToHex(result), 0, 60);
        return  isoMsg;

    }

    public ISOMsg createISOPreAuthRequest() throws ISOException {


        ISOMsg isoMsg = new ISOMsg();

        byte[] result = null;
        isoMsg.setPackager(packager);
        isoMsg.setMTI(SoftPosConstants.REFUND);
        isoMsg.set(2, cardData.get("realPAN"));
        isoMsg.set(3, SoftPosConstants.REFUND_PROCESSCODE);
        isoMsg.set(4, ISOUtil.zeropad(cardData.get("amount"), 12));
        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, SoftPosConstants.getRandomUnique(6));

        String time = isoMsg.getString(7).substring(4, 10);
        String date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');
        isoMsg.set(12, time);
        isoMsg.set(13, date);
        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");

        isoMsg.set(14, cardData.get("expiryDate"));
        isoMsg.set(18, merchantCode.substring(0, 4));

        isoMsg.set(25, SoftPosConstants.FIELD_25);
        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28);
        isoMsg.set(32, SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));

        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37, SoftPosConstants.getRandomUnique(12));
        isoMsg.set(40,SoftPosConstants.getServiceRestrictionCode(cardData.get("track2")));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));

        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));
        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));


        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);
        isoMsg.set(49, SoftPosConstants.FIELD_49);
//        isoMsg.set(54, cashbackAmount);
        isoMsg.set(123,SoftPosConstants.FIELD_123_VEREVE);

//            result = isoMsg.pack();
//            result[35]++;
//
//            pos.calcMacDoubleSha(KeyInjection.byteArrayToHex(result), 0, 60);

        return isoMsg;
    }


    public ISOMsg createISOPreAuthCompletionRequest(String rrn) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();

        byte[] result = null;
        isoMsg.setPackager(packager);
        isoMsg.setMTI(SoftPosConstants.PRE_AUTHRIZATION_COMPLETION);
        isoMsg.set(2, cardData.get("realPAN"));
        isoMsg.set(3, SoftPosConstants.PRE_AUTHRIZATIONCOMP_PROCESSCODE);
        isoMsg.set(4, ISOUtil.zeropad(cardData.get("amount"), 12));
        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, SoftPosConstants.getRandomUnique(6));

        String time = isoMsg.getString(7).substring(4, 10);
        String date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');
        isoMsg.set(12, time);
        isoMsg.set(13, date);
        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");

        isoMsg.set(14, cardData.get("expiryDate"));
        isoMsg.set(18, merchantCode.substring(0, 4));

        isoMsg.set(25, SoftPosConstants.FIELD_25);
        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28);
        isoMsg.set(32, SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));

        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37, rrn);
        isoMsg.set(40,SoftPosConstants.getServiceRestrictionCode(cardData.get("track2")));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));

        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));
        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));


        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);
        isoMsg.set(49, SoftPosConstants.FIELD_49);
        isoMsg.set(54, "0000566D"+ISOUtil.zeropad("1000", 12));
        isoMsg.set(123,SoftPosConstants.FIELD_123_VEREVE);
        return isoMsg;
    }

    public ISOMsg createISORefundRequest(String rrn,String stan) throws ISOException {

        String merchantId = map.get("merchantId");
        String merchantCode = map.get("merchantCode");
        String merchantname = map.get("merchantName");
        ISOMsg isoMsg = new ISOMsg();

        byte[] result = null;
        isoMsg.setPackager(packager);

        isoMsg.setMTI(SoftPosConstants.REFUND);
        isoMsg.set(2, cardData.get("realPAN"));
        isoMsg.set(3, SoftPosConstants.REFUND_PROCESSCODE);
        isoMsg.set(4, ISOUtil.zeropad(cardData.get("amount"), 12));

        isoMsg.set(7, UtilsHandlerPos.getMPOSMMDDHHMMSS());
        isoMsg.set(11, SoftPosConstants.getRandomUnique(6));

        String time = "";
        String date = "";

        time = UtilsHandlerPos.getMPOSMMDDHHMMSS().substring(4, 10);
        date = UtilsHandlerPos.getDateMonth('M') + UtilsHandlerPos.getDateMonth('D');

        isoMsg.set(12, time);
        isoMsg.set(13, date);
        isoMsg.set(14, cardData.get("expiryDate"));
        isoMsg.set(18,  merchantCode.substring(0, 4));
        isoMsg.set(22, SoftPosConstants.FIELD_DEF_22);

        isoMsg.set(25,"17");

        isoMsg.set(26, SoftPosConstants.FIELD_26);
        isoMsg.set(28, SoftPosConstants.FIELD_28_REFUND);
        isoMsg.set(32, SoftPosConstants.getIdentificationCode(cardData.get("realPAN")));
        isoMsg.set(35, ISOUtil.unPadRight(cardData.get("track2"),'F'));
        isoMsg.set(37,SoftPosConstants.getRandomUnique(12));
        isoMsg.set(40, SoftPosConstants.getServiceRestrictionCode(cardData.get("track2")));
        isoMsg.set(41, ISOUtil.zeropad(cardData.get("cTerminalId"), 8));
        isoMsg.set(42, ISOUtil.zeropad(merchantId, 15));
        isoMsg.set(43, ISOUtil.strpad(merchantname, 40));
        isoMsg.set(49, SoftPosConstants.FIELD_49);
        isoMsg.set(123, SoftPosConstants.FIELD_123_VEREVE);

//        result = isoMsg.pack();
//        result[35]++;
//        pos.calcMacDoubleSha(KeyInjection.byteArrayToHex(result), 0, 60);
        return isoMsg;
    }
}
