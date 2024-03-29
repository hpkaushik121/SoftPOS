package com.nownow.softpos.creditCardNfcReader.terminalData;


import com.nownow.softpos.creditCardNfcReader.iso7816emv.EmvTags;
import com.nownow.softpos.creditCardNfcReader.iso7816emv.TagAndLength;
import com.nownow.softpos.creditCardNfcReader.iso7816emv.TerminalTransactionQualifiers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;

//import id.paypro.nfc.creditCardNfcReader.iso7816emv.EmvTags;
//import id.paypro.nfc.creditCardNfcReader.iso7816emv.TagAndLength;
//import id.paypro.nfc.creditCardNfcReader.iso7816emv.TerminalTransactionQualifiers;

/**
 * Representation of a Point of Sale (POS)
 *
 * There is only 1 Terminal
 *
 * @author sasc
 */
public class EMVTerminal {

    private final static Properties defaultTerminalProperties = new Properties();
    private final static Properties runtimeTerminalProperties = new Properties();
    private final static TerminalVerificationResults terminalVerificationResults = new TerminalVerificationResults();

    private static CallbackHandler pinCallbackHandler;

    private static boolean doVerifyPinIfRequired = false;
    private static boolean isOnline = true;

    static {

        try {
            //Default properties
            defaultTerminalProperties.load(EMVTerminal.class.getResourceAsStream("raw/terminal.properties"));
            for (String key : defaultTerminalProperties.stringPropertyNames()) {
                //Sanitize
                String sanitizedKey = Util.byteArrayToHexString(Util.fromHexString(key)).toLowerCase();
                String sanitizedValue = Util.byteArrayToHexString(Util.fromHexString(defaultTerminalProperties.getProperty(key))).toLowerCase();
                defaultTerminalProperties.setProperty(sanitizedKey, sanitizedValue);
            }
            //Runtime/overridden properties
            String runtimeTerminalPropertiesFile = System.getProperty("terminal.properties");
            if (runtimeTerminalPropertiesFile != null) {
                runtimeTerminalProperties.load(new FileInputStream(runtimeTerminalPropertiesFile));
                for(String key : runtimeTerminalProperties.stringPropertyNames()) {
                    //Sanitize
                    String sanitizedKey   = Util.byteArrayToHexString(Util.fromHexString(key)).toLowerCase();
                    String sanitizedValue = Util.byteArrayToHexString(Util.fromHexString(runtimeTerminalProperties.getProperty(key))).toLowerCase();
                    if(defaultTerminalProperties.contains(sanitizedKey) && sanitizedValue.length() != defaultTerminalProperties.getProperty(key).length()) {
                        //Attempt to set different length for a default value
                        throw new RuntimeException("Attempted to set a value with unsupported length for key: "+sanitizedKey + " (value: "+sanitizedValue+")");
                    }
                    runtimeTerminalProperties.setProperty(sanitizedKey, sanitizedValue);
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

   /* //PDOL (Processing options Data Object List)
    //DDOL (*Default* Dynamic Data Authentication Data Object List)
    //     (Default to be used for constructing the INTERNAL AUTHENTICATE command if the DDOL in the card is not present)
    //TDOL (*Default* Transaction Certificate Data Object List)
    //     (Default to be used for generating the TC Hash Value if the TDOL in the card is not present)

    //PDOL example (Visa Electron, contactless)
//    9f 38 18 == Processing Options Data Object List (PDOL)
//         9f 66 04 == Terminal Transaction Qualifiers
//         9f 02 06 == Amount, Authorised (Numeric)
//         9f 03 06 == Amount, Other (Numeric)
//         9f 1a 02 == Terminal Country Code
//         95 05 == Terminal Verification Results (TVR)
//         5f 2a 02 == Transaction Currency Code
//         9a 03 == Transaction Date
//         9c 01 == Transaction Type
//         9f 37 04 == Unpredictable Number*/
    private static byte[] getTerminalResidentData(TagAndLength tal) {
        //Check if the value is specified in the runtime properties file
        String propertyValueStr = runtimeTerminalProperties.getProperty(Util.byteArrayToHexString(tal.getTag().getTagBytes()).toLowerCase());

        if(propertyValueStr != null) {
            byte[] propertyValue = Util.fromHexString(propertyValueStr);

            if (propertyValue.length == tal.getLength()) {
                return propertyValue;
            }
        }

        if (tal.getTag().equals(EmvTags.TERMINAL_COUNTRY_CODE) && tal.getLength() == 2) {
            return getCurrencyAndCountryCode();
        } else if (tal.getTag().equals(EmvTags.TRANSACTION_CURRENCY_CODE) && tal.getLength() == 2) {
            return getCurrencyAndCountryCode();
        }

        //Now check for default values
        propertyValueStr = defaultTerminalProperties.getProperty(Util.byteArrayToHexString(tal.getTag().getTagBytes()).toLowerCase());

        if(propertyValueStr != null) {
            byte[] propertyValue = Util.fromHexString(propertyValueStr);

            if (propertyValue.length == tal.getLength()) {
                return propertyValue;
            }
        }

        if (tal.getTag().equals(EmvTags.UNPREDICTABLE_NUMBER)) {
            return Util.generateRandomBytes(tal.getLength());
        } else if (tal.getTag().equals(EmvTags.TERMINAL_TRANSACTION_QUALIFIERS) && tal.getLength() == 4) {
            //This seems only to be used in contactless mode. Construct accordingly
            TerminalTransactionQualifiers ttq = new TerminalTransactionQualifiers();
            ttq.setContactlessEMVmodeSupported(true);
            ttq.setReaderIsOfflineOnly(true);
            return ttq.getBytes();
        } else if (tal.getTag().equals(EmvTags.TERMINAL_VERIFICATION_RESULTS) && tal.getLength() == 5) {
            //All bits set to '0'
            return terminalVerificationResults.toByteArray();
        } else if (tal.getTag().equals(EmvTags.TRANSACTION_DATE) && tal.getLength() == 3) {
            return Util.getCurrentDateAsNumericEncodedByteArray();
        } else if (tal.getTag().equals(EmvTags.TRANSACTION_TYPE) && tal.getLength() == 1) {
            //transactionTypes = {     0:  "Payment",     1:  "Withdrawal", }
            //http://www.codeproject.com/Articles/100084/Introduction-to-ISO-8583
            return new byte[]{0x00};
        } else {
            Log.debug("Terminal Resident Data not found for " + tal);
        }
        byte[] defaultResponse = new byte[tal.getLength()];
        Arrays.fill(defaultResponse, (byte) 0x00);
        return defaultResponse;
    }

    public static TerminalVerificationResults getTerminalVerificationResults() {
        return terminalVerificationResults;
    }

    public static void resetTVR(){
        terminalVerificationResults.reset();
    }

//    public static void setProperty(String tagHex, String valueHex) {
//        setProperty(new TagImpl(tagHex, TagValueType.BINARY, "", ""), Util.fromHexString(valueHex));
//    }
//
//    public static void setProperty(Tag tag, byte[] value){
//        runtimeTerminalProperties.setProperty(Util.byteArrayToHexString(tag.getTagBytes()).toLowerCase(Locale.US), Util.byteArrayToHexString(value));
//    }

//    public static boolean isCDASupported(EMVApplication app) {
//        return false;
//    }
//
//    public static boolean isDDASupported(EMVApplication app) {
//        return true;
//    }
//
//    public static boolean isSDASupported(EMVApplication app) {
//        return true;
//    }

    public static boolean isATM() {
        return false;
    }

    public static Date getCurrentDate() {
        return new Date();
    }

//    public static int getSupportedApplicationVersionNumber(EMVApplication app) {
//        //TODO
//        //For now, just return the version number maintained in the card
//        return app.getApplicationVersionNumber();
//    }

//    public static boolean isCVMRecognized(EMVApplication app, CVRule rule) {
//        switch(rule.getRule()) {
//            case RESERVED_FOR_USE_BY_THE_INDIVIDUAL_PAYMENT_SYSTEMS:
//                //app.getAID().getRIDBytes();
//                //TODO check if RID specific rule is supported
//                //if(supported) {
//                //    return true;
//                //}
//            case RESERVED_FOR_USE_BY_THE_ISSUER:
//
//                if(app.getIssuerIdentificationNumber() != null){
//                    //TODO check if issuer specific rule is supported
//                    //if(supported){
//                    //  return true;
//                    //}
//                }
//            case NOT_AVAILABLE_FOR_USE:
//            case RFU:
//                return false;
//        }
//        return true;
//    }

    public static boolean isCVMSupported(CVRule rule) {
        switch(rule.getRule()) {
            //TODO support enciphered PIN
            case ENCIPHERED_PIN_VERIFIED_BY_ICC:
            case ENCIPHERED_PIN_VERIFIED_BY_ICC_AND_SIGNATURE_ON_PAPER:
                return false;
            case PLAINTEXT_PIN_VERIFIED_BY_ICC_AND_SIGNATURE_ON_PAPER:
            case PLAINTEXT_PIN_VERIFIED_BY_ICC:
                return hasPinInputCapability();
            case SIGNATURE_ON_PAPER:
                return false;
            case ENCIPHERED_PIN_VERIFIED_ONLINE:
                return isOnline();
            case FAIL_PROCESSING:
            case NO_CVM_REQUIRED:
                return true;
        }
        return false;
    }

    public static boolean isOnline() {
        return isOnline;
    }

    public static void setIsOnline(boolean value){
        isOnline = value;
    }

    public static boolean isCVMConditionSatisfied(CVRule rule) {
        if(rule.getConditionAlways()) {
            return true;
        }
        if(rule.getConditionCode() <= 0x05){
            //TODO
            return true;
        }else if(rule.getConditionCode() < 0x0A) {
            //TODO
            //Check for presence Application Currency Code or Amount, Authorised in app records?
            return true;
        } else { //RFU and proprietary
            return false;
        }
    }

    public static boolean verifyEncipheredPinOnline() {
        if(!isOnline()) {
            return false;
        }
        //TODO
        return true;
    }

    public static boolean hasSignatureOnPaper() {
        return true;
    }

    public static void setDoVerifyPinIfRequired(boolean value) {
        doVerifyPinIfRequired = value;
    }

    public static boolean getDoVerifyPinIfRequired() {
        return doVerifyPinIfRequired;
    }

    /**
     *
     * @return true if a Pin CallbackHandler has be set
     */
    public static boolean hasPinInputCapability() {
        return doVerifyPinIfRequired && pinCallbackHandler != null;
    }

    public static void setPinCallbackHandler(CallbackHandler callbackHandler) {
        pinCallbackHandler = callbackHandler;
    }

//    public static PasswordCallback getPinInput() {
//        CallbackHandler callBackHandler = pinCallbackHandler;
//        if(callBackHandler == null){
//            return null;
//        }
//        PasswordCallback passwordCallback = new PasswordCallback("Type PIN", false);
//        try{
//            callBackHandler.handle(new Callback[]{passwordCallback});
//        }catch(IOException ex){
//            Log.info(Util.getStackTrace(ex));
//        }catch(UnsupportedCallbackException ex){
//            Log.info(Util.getStackTrace(ex));
//        }
//        return passwordCallback;
//    }

    public static boolean getPerformTerminalRiskManagement() {
        return false;
    }

//    public static byte[] constructDOLResponse(DOL dol, EMVApplication app) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        for (TagAndLength tagAndLength : dol.getTagAndLengthList()) {
//            byte[] data = getTerminalResidentData(tagAndLength, app);
//            stream.write(data, 0, data.length);
//        }
//        return stream.toByteArray();
//    }

    //The ICC may contain the DDOL, but there shall be a default DDOL in the terminal,
    //specified by the payment system, for use in case the DDOL is not present in the ICC.
//    public static byte[] getDefaultDDOLResponse(EMVApplication app) {
//        //It is mandatory that the DDOL contains the Unpredictable Number generated by the terminal (tag '9F37', 4 bytes binary).
//        byte[] unpredictableNumber = Util.generateRandomBytes(4);
//
//        //TODO add other DDOL data specified by the payment system
//        //if(app.getAID().equals(SOMEAID))
//
//        return unpredictableNumber;
//    }

    //Ex Banco BRADESCO (f0 00 00 00 03 00 01) failes GPO with wrong COUNTRY_CODE !
//    private static byte[] findCountryCode(EMVApplication app) {
//        if(app != null){
//            if(app.getIssuerCountryCode() != -1){
//                byte[] countryCode = Util.intToBinaryEncodedDecimalByteArray(app.getIssuerCountryCode());
//                return Util.resizeArray(countryCode, 2);
//            }
//        }
//
//        Log.debug("No Issuer Country Code found in app. Using default Terminal Country Code");
//
//        String countryCode = defaultTerminalProperties.getProperty(Util.byteArrayToHexString(EmvTags.TERMINAL_COUNTRY_CODE.getTagBytes()));
//        if(countryCode != null){
//            return Util.fromHexString(countryCode);
//        }
//
//        return new byte[]{0x08, 0x26};
//    }

    public static byte[] getCurrencyAndCountryCode(){
        return Util.resizeArray(Util.intToBinaryEncodedDecimalByteArray(Util.COUNTRY_CODE), 2);


    }

//    public static void main(String[] args) {
//        for(String key : defaultTerminalProperties.stringPropertyNames()){
//            System.out.println(key+"="+defaultTerminalProperties.getProperty(key));
//        }
//
//        {
//            TagAndLength tagAndLength = new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 6);
//            DOL dol = new DOL(DOL.Type.PDOL, tagAndLength.getBytes());
//            System.out.println(Util.prettyPrintHexNoWrap(constructDOLResponse(dol, null)));
//        }
//        {
//            TagAndLength tagAndLength = new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 2);
//            DOL dol = new DOL(DOL.Type.PDOL, tagAndLength.getBytes());
//            System.out.println(Util.prettyPrintHexNoWrap(constructDOLResponse(dol, null)));
//        }
//
//        {
//            //Test country code 076 (Brazil)
//            TagAndLength tagAndLength = new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 2);
//            DOL dol = new DOL(DOL.Type.PDOL, tagAndLength.getBytes());
//            EMVApplication app = new EMVApplication();
//            app.setIssuerCountryCode(76); //Brazil
//            byte[] dolResponse = constructDOLResponse(dol, app);
//            System.out.println(Util.prettyPrintHexNoWrap(dolResponse));
//            if (!Arrays.equals(new byte[]{0x00, (byte) 0x76}, dolResponse)) {
//                throw new AssertionError("Country code was wrong");
//            }
//        }
//
//        {
//            //Test currency code 986 (Brazilian Real)
//            TagAndLength tagAndLength = new TagAndLength(EmvTags.TRANSACTION_CURRENCY_CODE, 2);
//            DOL dol = new DOL(DOL.Type.PDOL, tagAndLength.getBytes());
//            EMVApplication app = new EMVApplication();
//            app.setApplicationCurrencyCode(986);
//            byte[] dolResponse = constructDOLResponse(dol, app);
//            System.out.println(Util.prettyPrintHexNoWrap(dolResponse));
//            if (!Arrays.equals(new byte[]{0x09, (byte) 0x86}, dolResponse)) {
//                throw new AssertionError("Currency code was wrong");
//            }
//        }
//
//        {
//            //Test currency code 986 (Brazilian Real) from Locale
//            TagAndLength tagAndLength = new TagAndLength(EmvTags.TRANSACTION_CURRENCY_CODE, 2);
//            DOL dol = new DOL(DOL.Type.PDOL, tagAndLength.getBytes());
//            EMVApplication app = new EMVApplication();
//            app.setLanguagePreference(new LanguagePreference(Util.fromHexString("70 74 65 6e 65 73 69 74"))); // (=ptenesit)
//            byte[] dolResponse = constructDOLResponse(dol, app);
//            System.out.println(Util.prettyPrintHexNoWrap(dolResponse));
////            if(!Arrays.equals(new byte[]{0x09, (byte)0x86}, dolResponse)){
////                throw new AssertionError("Currency code was wrong");
////            }
//        }
//    }
}