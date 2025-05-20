package com.aicortex.softpos.creditCardNfcReader.parser;


import android.text.TextUtils;

import com.aicortex.softpos.creditCardNfcReader.enums.CommandEnum;
import com.aicortex.softpos.creditCardNfcReader.enums.EmvCardScheme;
import com.aicortex.softpos.creditCardNfcReader.enums.SwEnum;
import com.aicortex.softpos.creditCardNfcReader.exception.CommunicationException;
import com.aicortex.softpos.creditCardNfcReader.iso7816emv.EmvTags;
import com.aicortex.softpos.creditCardNfcReader.iso7816emv.EmvTerminal;
import com.aicortex.softpos.creditCardNfcReader.iso7816emv.TLV;
import com.aicortex.softpos.creditCardNfcReader.iso7816emv.TagAndLength;
import com.aicortex.softpos.creditCardNfcReader.model.Afl;
import com.aicortex.softpos.creditCardNfcReader.model.EmvCard;
import com.aicortex.softpos.creditCardNfcReader.model.EmvTransactionRecord;
import com.aicortex.softpos.creditCardNfcReader.model.enums.CurrencyEnum;
import com.aicortex.softpos.creditCardNfcReader.terminalData.AID;
import com.aicortex.softpos.creditCardNfcReader.terminalData.CA;
import com.aicortex.softpos.creditCardNfcReader.terminalData.CVMList;
import com.aicortex.softpos.creditCardNfcReader.terminalData.CVRule;
import com.aicortex.softpos.creditCardNfcReader.terminalData.EMVTerminal;
import com.aicortex.softpos.creditCardNfcReader.terminalData.ICCPublicKeyCertificate;
import com.aicortex.softpos.creditCardNfcReader.terminalData.IssuerPublicKeyCertificate;
import com.aicortex.softpos.creditCardNfcReader.terminalData.Log;
import com.aicortex.softpos.creditCardNfcReader.terminalData.TerminalVerificationResults;
import com.aicortex.softpos.creditCardNfcReader.terminalData.Util;
import com.aicortex.softpos.creditCardNfcReader.utils.BytesUtils;
import com.aicortex.softpos.creditCardNfcReader.utils.CommandApdu;
import com.aicortex.softpos.creditCardNfcReader.utils.ResponseUtils;
import com.aicortex.softpos.creditCardNfcReader.utils.TlvUtil;
import com.aicortex.softpos.creditCardNfcReader.utils.TrackUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * Emv Parser.<br/>
 * Class used to read and parse EMV card
 */
public class EmvParser {

    /**
     * Class Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmvParser.class);

    /**
     * PPSE directory "2PAY.SYS.DDF01"
     */
    private static final byte[] PPSE = "2PAY.SYS.DDF01".getBytes();

    /**
     * PSE directory "1PAY.SYS.DDF01"
     */
    private static final byte[] PSE = "1PAY.SYS.DDF01".getBytes();

    /**
     * Unknow response
     */
    public static final int UNKNOW = -1;

    /**
     * Card holder name separator
     */
    public static final String CARD_HOLDER_NAME_SEPARATOR = "/";

    /**
     * Provider
     */
    private IProvider provider;

    /**
     * use contact less mode
     */
    private boolean contactLess;

    /**
     * Card data
     */
    private EmvCard card;

    /**
     * amount in Kobo
     */
    private String amount;

    /**
     * Constructor
     *
     * @param pProvider    provider to launch command
     * @param pContactLess boolean to indicate if the EMV card is contact less or not
     */
    public EmvParser(final IProvider pProvider, final boolean pContactLess, final String amount) {
        provider = pProvider;
        contactLess = pContactLess;
        card = new EmvCard();
        this.amount = amount;
    }

    /**
     * Method used to read public data from EMV card
     *
     * @return data read from card or null if any provider match the card type
     */
    public EmvCard readEmvCard() throws CommunicationException {
        // use PSE first
        if (!readWithPSE()) {
            // Find with AID
            readWithAID();
        }
        return card;
    }

    /**
     * Method used to select payment environment PSE or PPSE
     *
     * @return response byte array
     * @throws CommunicationException
     */
    protected byte[] selectPaymentEnvironment() throws CommunicationException {
        if (LOGGER.isDebugEnabled()) {
            Log.debug("Select " + (contactLess ? "PPSE" : "PSE") + " Application");
        }
        // Select the PPSE or PSE directory
        return provider.transceive(new CommandApdu(CommandEnum.SELECT, contactLess ? PPSE : PSE, 0).toBytes());
    }


    /**
     * Method used to get the number of pin try left
     *
     * @return the number of pin try left
     * @throws CommunicationException
     */
    protected int getLeftPinTry() throws CommunicationException {
        int ret = UNKNOW;
        if (LOGGER.isDebugEnabled()) {
            Log.debug("Get Left PIN try");
        }
        // Left PIN try command
        byte[] data = provider.transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x17, 0).toBytes());
        if (ResponseUtils.isSucceed(data)) {
            // Extract PIN try counter
            byte[] val = TlvUtil.getValue(data, EmvTags.PIN_TRY_COUNTER);
            if (val != null) {
                ret = BytesUtils.byteArrayToInt(val);
            }
        }
        return ret;
    }

    /**
     * Method used to parse FCI(file Control Information) Proprietary Template
     *
     * @param pData data to parse
     * @return
     * @throws CommunicationException
     */
    protected byte[] parseFCIProprietaryTemplate(final byte[] pData) throws CommunicationException {
        // Get SFI
        byte[] data = TlvUtil.getValue(pData, EmvTags.SFI);

        // Check SFI
        if (data != null) {
            int sfi = BytesUtils.byteArrayToInt(data);
            if (LOGGER.isDebugEnabled()) {
                Log.debug("SFI found:" + sfi);
            }
            data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, 0).toBytes());
            // If LE is not correct
            if (ResponseUtils.isEquals(data, SwEnum.SW_6C)) {
                data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, data[data.length - 1]).toBytes());
            }
            return data;
        }
        if (LOGGER.isDebugEnabled()) {
            Log.debug("(FCI) Issuer Discretionary Data is already present");
        }
        return pData;
    }

    /**
     * Method used to extract application label
     *
     * @return decoded application label or null
     */
    protected String extractApplicationLabel(final byte[] pData) {
        if (LOGGER.isDebugEnabled()) {
            Log.debug("Extract Application label");
        }
        String label = null;
        byte[] labelByte = TlvUtil.getValue(pData, EmvTags.APPLICATION_LABEL);
        if (labelByte != null) {
            label = new String(labelByte);
        }
        return label;
    }

    /**
     * Read EMV card with Payment System Environment or Proximity Payment System
     * Environment
     *
     * @return true is succeed false otherwise
     */
    protected boolean readWithPSE() throws CommunicationException {
        boolean ret = false;
        if (LOGGER.isDebugEnabled()) {
            Log.debug("Try to read card with Payment System Environment");
        }
        // Select the PPSE or PSE directory
        byte[] data = selectPaymentEnvironment();
        if (ResponseUtils.isSucceed(data)) {
            // Parse FCI Template
            data = parseFCIProprietaryTemplate(data);
            // Extract application label
            if (ResponseUtils.isSucceed(data)) {
                // Get Aids
                List<byte[]> aids = getAids(data);
                for (byte[] aid : aids) {
                    ret = extractPublicData(aid, extractApplicationLabel(data));
                    if (ret == true) {
                        break;
                    }
                }
                if (!ret) {
                    card.setNfcLocked(true);
                }
            }
        } else if (LOGGER.isDebugEnabled()) {
            Log.debug((contactLess ? "PPSE" : "PSE") + " not found -> Use kown AID");
        }

        return ret;
    }

    private byte[] generateUnpredictableNumber() throws CommunicationException {
        return provider.transceive(new CommandApdu(CommandEnum.GET_CHALLENGE, 0x00, 0x00, 0x00).toBytes());
    }

    public static String padleft(String s, int len, char c) throws RuntimeException {
        s = s.trim();
        if (s.length() > len) {
            throw new RuntimeException("invalid len " + s.length() + "/" + len);
        } else {
            StringBuffer d = new StringBuffer(len);
            int var4 = len - s.length();

            while (var4-- > 0) {
                d.append(c);
            }

            d.append(s);
            return d.toString();
        }
    }


    private byte[] fetchCardActionAnalysis(byte[] unp, String amt,TerminalVerificationResults tvr) throws CommunicationException {

//		byte[] generateAC = new byte[] {
//				(byte)0x80, // CLA = proprietary
//				(byte)0xAE, // INS = COMPUTE CRYPTOGRAPHIC CHECKSUM
//				(byte)0x80, // P1
//				(byte)0x00, // P2
//				(byte)0x42, // Lc
//				// 9f6a 04      Unpredictable number (numeric)
//				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, // two digits according to UN/ATC bit map and number of ATC digits: 6 - 4 = 2
//				// 9f7e 01      Mobile support indicator
//				(byte)0x00, // no offline PIN required, no mobile support
//				// 9f02 06      Amount authorized (numeric)
//				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // 1.00
//				// 5f2a 02      Transaction currency code
//				(byte)0x09, (byte)0x78, // Euro
//				// 9f1a 02      Terminal country code
//				(byte)0x00, (byte)0x40, // Austria
//				(byte)0x00, // Le
//		};

//		8C | len:27    Card Risk Management Data Object List 1 (CDOL1)
//		9F02 | len:06   Amount, Authorised (Numeric)
//		9F03 | len:06   Amount, Other (Numeric)
//		9F1A | len:02   Terminal Country Code
//		95 | len:05   Terminal Verification Results
//		5F2A | len:02   Transaction Currency Code
//		9A | len:03   Transaction Date
//		9C | len:01   Transaction Type
//		9F37 | len:04   Unpredictable Number
//		9F35 | len:01   Terminal Type
//		9F45 | len:02   Data Authentication Code
//		9F4C | len:08   ICC Dynamic Number
//		9F34 | len:03   Cardholder Verification Method (CVM) Results
//		9F21 | len:03   Transaction Time HHMMSS
//		9F7C | len:20   Customer Exclusive Data (CED)
        int year = Integer.parseInt(getMPOSMMDDHHMMSS('y'), 16);
        int month = Integer.parseInt(getMPOSMMDDHHMMSS('m'), 16);
        int date = Integer.parseInt(getMPOSMMDDHHMMSS('d'), 16);
        int hour = Integer.parseInt(getMPOSMMDDHHMMSS('H'), 16);
        int minute = Integer.parseInt(getMPOSMMDDHHMMSS('M'), 16);
        int seconds = Integer.parseInt(getMPOSMMDDHHMMSS('S'), 16);
        amt = padleft(amt + "00", 12, '0');
        byte[] amount = new byte[6];
        for (int i = 0; i < 11; i = i + 2) {
            amount[i / 2] = (byte) Integer.parseInt(amt.charAt(i) + "" + amt.charAt(i + 1), 16);
        }

        byte[] TVR=tvr.toByteArray();


        byte[] generateAC = new byte[]{
                (byte) 0x80,   // CLA = proprietary
                (byte) 0xAE,   // INS = COMPUTE CRYPTOGRAPHIC CHECKSUM
                (byte) 0x80,   // P1
                (byte) 0x00,   // P2
                (byte) 0x42,   // Lc
                amount[0],    // Amount authorized (numeric)
                amount[1],    // Amount authorized (numeric)
                amount[2],    // Amount authorized (numeric)
                amount[3],    // Amount authorized (numeric)
                amount[4],    // Amount authorized (numeric)
                amount[5],    // Amount authorized (numeric)
                (byte) 0x00,   // Amount authorized Other (numeric)
                (byte) 0x00,   // Amount authorized Other (numeric)
                (byte) 0x00,   // Amount authorized Other (numeric)
                (byte) 0x00,   // Amount authorized Other (numeric)
                (byte) 0x00,   // Amount authorized Other (numeric)
                (byte) 0x00,   // Amount authorized Other (numeric)
                (byte) 0x05,   // Transaction currency code
                (byte) 0x66,   // Transaction currency code
                TVR[0],   // TVR (Terminal Verification Result)
                TVR[1],   // TVR (Terminal Verification Result)
                TVR[2],   // TVR (Terminal Verification Result)
                TVR[3],   // TVR (Terminal Verification Result)
                TVR[4],   // TVR (Terminal Verification Result)
                (byte) 0x05,   // Terminal country code
                (byte) 0x66,   // Terminal country code
                (byte) year,   // Year
                (byte) month,   // Moth
                (byte) date,   // Date
                (byte) 0x00,   // Transaction type
                unp[0],       // unpredictable number
                unp[1],       // unpredictable number
                unp[2],       // unpredictable number
                unp[3],       // unpredictable number
                (byte) 0x22,   // Terminal Type
                (byte) 0x00,   // Data Authentication Code
                (byte) 0x00,   // Data Authentication Code
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x00,   // ICC Dynamic Number
                (byte) 0x1F,   // CVM (Cardholder Verification Method)
                (byte) 0x03,   // CVM (Cardholder Verification Method)
                (byte) 0x02,   // CVM (Cardholder Verification Method)
                (byte) hour,   // Transaction Time
                (byte) minute,   // Transaction Time
                (byte) seconds,   // Transaction Time
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00,   // Customer Exclusive Data (CED)
                (byte) 0x00    // End
        };
        return provider.transceive(generateAC);

    }

    /**
     * Method used to get the aid list, if the Kernel Identifier is defined, <br/>
     * this value need to be appended to the ADF Name in the data field of <br/>
     * the SELECT command.
     *
     * @param pData FCI proprietary template data
     * @return the Aid to select
     */
    protected List<byte[]> getAids(final byte[] pData) {
        List<byte[]> ret = new ArrayList<byte[]>();
        List<TLV> listTlv = TlvUtil.getlistTLV(pData, EmvTags.AID_CARD, EmvTags.KERNEL_IDENTIFIER);
        for (TLV tlv : listTlv) {
            if (tlv.getTag() == EmvTags.KERNEL_IDENTIFIER && ret.size() != 0) {
                ret.add(ArrayUtils.addAll(ret.get(ret.size() - 1), tlv.getValueBytes()));
            } else {
                ret.add(tlv.getValueBytes());
            }
        }
        return ret;
    }

    /**
     * Read EMV card with AID
     */
    protected void readWithAID() throws CommunicationException {
        if (LOGGER.isDebugEnabled()) {
            Log.debug("Try to read card with AID");
        }
        // Test each card from know EMV AID
        for (EmvCardScheme type : EmvCardScheme.values()) {
            for (byte[] aid : type.getAidByte()) {
                if (extractPublicData(aid, type.getName())) {
                    return;
                }
            }
        }
    }

    /**
     * Select application with AID or RID
     *
     * @param pAid byte array containing AID or RID
     * @return response byte array
     * @throws CommunicationException
     */
    protected byte[] selectAID(final byte[] pAid) throws CommunicationException {
        if (LOGGER.isDebugEnabled()) {
            Log.debug("Select AID: " + BytesUtils.bytesToString(pAid));
        }
        return provider.transceive(new CommandApdu(CommandEnum.SELECT, pAid, 0).toBytes());
    }

    /**
     * Read public card data from parameter AID
     *
     * @param pAid              card AID in bytes
     * @param pApplicationLabel application scheme (Application label)
     * @return true if succeed false otherwise
     */
    protected boolean extractPublicData(final byte[] pAid, final String pApplicationLabel) throws CommunicationException {
        boolean ret = false;
        // Select AID
        byte[] data = selectAID(pAid);
        // check response
        if (ResponseUtils.isSucceed(data)) {
            // Parse select response
            ret = parse(data, provider);
            if (ret) {
                // Get AID
                String aid = BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
                if (LOGGER.isDebugEnabled()) {
                    Log.debug("Application label:" + pApplicationLabel + " with Aid:" + aid);
                }
                card.setAid(aid);
                card.setType(findCardScheme(aid, card.getCardNumber()));
                card.setApplicationLabel(pApplicationLabel);
                card.setLeftPinTry(getLeftPinTry());
            }
        }
        return ret;
    }

    public static String getMPOSMMDDHHMMSS(char format) {

        Calendar ca1 = Calendar.getInstance(Locale.getDefault());
        String yearmonday = "";
//        ca1.add(Calendar.HOUR, -4);
//        ca1.add(Calendar.MINUTE, -30);
        int DD = ca1.get(Calendar.DAY_OF_MONTH);
        int YY = ca1.get(Calendar.YEAR);
        int MM = ca1.get(Calendar.MONTH);
        int iHour = ca1.get(Calendar.HOUR_OF_DAY);
        int iMinute = ca1.get(Calendar.MINUTE);
        int iSeconds = ca1.get(Calendar.SECOND);

        if (format == 'm') {
            if (((MM + 1) + "").length() == 1) {
                return "0" + (MM + 1);
            } else {
                return "" + (MM + 1);
            }
        }
        if (format == 'y') {
            return (YY - 2000) + "";
        }

        if (format == 'd') {
            if ((DD + "").length() == 1) {
                return "0" + DD;
            } else {
                return "" + DD;
            }
        }


        if (format == 'H') {
            if ((iHour + "").length() == 1) {
                return "0" + iHour;

            } else {
                return "" + iHour;
            }
        }

        if (format == 'M') {
            if ((iMinute + "").length() == 1) {
                return "0" + iMinute;

            } else {
                return "" + iMinute;
            }
        }

        if (format == 'S') {
            if ((iSeconds + "").length() == 1) {
                return "0" + iSeconds;

            } else {
                return "" + iSeconds;
            }
        }

        // System.out.println("yearmonday"+yearmonday);
        return yearmonday;
    }


    /**
     * Method used to find the real card scheme
     *
     * @param pAid        card complete AID
     * @param pCardNumber card number
     * @return card scheme
     */
    protected EmvCardScheme findCardScheme(final String pAid, final String pCardNumber) {
        EmvCardScheme type = EmvCardScheme.getCardTypeByAid(pAid);
        // Get real type for french card
        if (type == EmvCardScheme.CB) {
            type = EmvCardScheme.getCardTypeByCardNumber(pCardNumber);
            if (type != null) {
                Log.debug("Real type:" + type.getName());
            }
        }
        return type;
    }

    /**
     * Method used to extract Log Entry from Select response
     *
     * @param pSelectResponse select response
     * @return byte array
     */
    protected byte[] getLogEntry(final byte[] pSelectResponse) {
        return TlvUtil.getValue(pSelectResponse, EmvTags.LOG_ENTRY, EmvTags.VISA_LOG_ENTRY);
    }

    /**
     * Method used to parse EMV card
     */
    protected boolean parse(final byte[] pSelectResponse, final IProvider pProvider) throws CommunicationException {
        boolean ret = false;
        // Get TLV log entry
        byte[] logEntry = getLogEntry(pSelectResponse);
        //get unpredictable Number
        byte[] unp = generateUnpredictableNumber();

        TerminalVerificationResults tvr = new TerminalVerificationResults();
        tvr.setICCAndTerminalHaveDifferentApplicationVersions(true);
        tvr.setMerchantForcedTransactionOnline(true);
        tvr.setOnlinePINEntered(true);


        if (unp == null || unp.length < 4) {
            unp = new byte[4];
            for (int i = 0; i < 4; i++) {
                Random r = new Random();
                int low = 10;
                int high = 100;
                int result = r.nextInt(high - low) + low;
                unp[i] = (byte) Integer.parseInt(result + "", 16);
            }

        }
        // Get PDOL
        byte[] pdol = TlvUtil.getValue(pSelectResponse, EmvTags.PDOL);
        // Send GPO Command
        byte[] gpo = getGetProcessingOptions(pdol, pProvider,unp,tvr);


        // Check empty PDOL
        if (!ResponseUtils.isSucceed(gpo)) {
            gpo = getGetProcessingOptions(null, pProvider,unp,tvr);
            // Check response
            if (!ResponseUtils.isSucceed(gpo)) {
                return false;
            }
        }

        // Extract commons card data (number, expire date, ...)
        if (extractCommonsCardData(gpo)) {

            // Extract log entry
            card.setListTransactions(extractLogEntry(logEntry));
            ret = true;
        }

//        performCardholderVerification(unp);




//        tvr.setICCAndTerminalHaveDifferentApplicationVersions(true);
//        tvr.setTransactionExceedsFloorLimit(true);

        byte[] step9 = fetchCardActionAnalysis(unp, amount,tvr);
        String rspStep9 = BytesUtils.bytesToStringNoSpace(step9);
        String rspUnp = BytesUtils.bytesToStringNoSpace(unp);

        if (rspStep9.endsWith("9000")) {
            extractICCData(step9, rspUnp.replace("9000", ""), gpo,tvr);
        } else {
//			byte unpn[] = new byte[4];
//			new SecureRandom().nextBytes(unpn);
            extractICCData(gpo, BytesUtils.bytesToStringNoSpace(unp), gpo,tvr);
        }


        return ret;
    }

    /**
     * Method used to extract commons card dataapp
     *
     * @param pGpo global processing options response
     */
    protected boolean extractCommonsCardData(final byte[] pGpo) throws CommunicationException {
        boolean ret = false;
        // Extract data from Message Template 1
        byte data[] = TlvUtil.getValue(pGpo, EmvTags.RESPONSE_MESSAGE_TEMPLATE_1);
        if (data != null) {
            data = ArrayUtils.subarray(data, 2, data.length);
        } else { // Extract AFL data from Message template 2
            ret = TrackUtils.extractTrack2Data(card, pGpo);
            if (!ret) {
                data = TlvUtil.getValue(pGpo, EmvTags.APPLICATION_FILE_LOCATOR);
            } else {
                extractCardHolderName(pGpo);
            }
        }


        if (data != null) {
            // Extract Afl
            List<Afl> listAfl = extractAfl(data);
            // for each AFL
            for (Afl afl : listAfl) {
                // check all records
                for (int index = afl.getFirstRecord(); index <= afl.getLastRecord(); index++) {
                    byte[] info = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4, 0).toBytes());
                    if (ResponseUtils.isEquals(info, SwEnum.SW_6C)) {
                        info = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4,
                                info[info.length - 1]).toBytes());
                    }

                    // Extract card data
                    if (ResponseUtils.isSucceed(info)) {
                        extractCardHolderName(info);
                        if (TrackUtils.extractTrack2Data(card, info)) {
                            return true;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Method used to get log format
     *
     * @return list of tag and length for the log format
     * @throws CommunicationException
     */
    protected List<TagAndLength> getLogFormat() throws CommunicationException {
        List<TagAndLength> ret = new ArrayList<TagAndLength>();
        if (LOGGER.isDebugEnabled()) {
            Log.debug("GET log format");
        }
        // Get log format
        byte[] data = provider.transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x4F, 0).toBytes());
        if (ResponseUtils.isSucceed(data)) {
            ret = TlvUtil.parseTagAndLength(TlvUtil.getValue(data, EmvTags.LOG_FORMAT));
        }
        return ret;
    }

    /**
     * Method used to extract log entry from card
     *
     * @param pLogEntry log entry position
     */
    protected List<EmvTransactionRecord> extractLogEntry(final byte[] pLogEntry) throws CommunicationException {
        List<EmvTransactionRecord> listRecord = new ArrayList<EmvTransactionRecord>();
        // If log entry is defined
        if (pLogEntry != null) {
            List<TagAndLength> tals = getLogFormat();
            // read all records
            for (int rec = 1; rec <= pLogEntry[1]; rec++) {
                byte[] response = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, pLogEntry[0] << 3 | 4, 0).toBytes());
                // Extract data
                if (ResponseUtils.isSucceed(response)) {
                    EmvTransactionRecord record = new EmvTransactionRecord();
                    record.parse(response, tals);

                    // Fix artifact in EMV VISA card
                    if (record.getAmount() >= 1500000000) {
                        record.setAmount(record.getAmount() - 1500000000);
                    }

                    // Skip transaction with nul amount
                    if (record.getAmount() == null || record.getAmount() == 0) {
                        continue;
                    }

                    if (record != null) {
                        // Unknown currency
                        if (record.getCurrency() == null) {
                            record.setCurrency(CurrencyEnum.XXX);
                        }
                        listRecord.add(record);
                    }
                } else {
                    // No more transaction log or transaction disabled
                    break;
                }
            }
        }
        return listRecord;
    }

    /**
     * Extract list of application file locator from Afl response
     *
     * @param pAfl AFL data
     * @return list of AFL
     */
    protected List<Afl> extractAfl(final byte[] pAfl) {
        List<Afl> list = new ArrayList<Afl>();
        ByteArrayInputStream bai = new ByteArrayInputStream(pAfl);
        while (bai.available() >= 4) {
            Afl afl = new Afl();
            afl.setSfi(bai.read() >> 3);
            afl.setFirstRecord(bai.read());
            afl.setLastRecord(bai.read());
            afl.setOfflineAuthentication(bai.read() == 1);
            list.add(afl);
        }
        return list;
    }

    /**
     * Extract card holder lastname and firstname
     *
     * @param pData card data
     */
    protected void extractCardHolderName(final byte[] pData) {
        // Extract Card Holder name (if exist)
        byte[] cardHolderByte = TlvUtil.getValue(pData, EmvTags.CARDHOLDER_NAME);
        byte[] cvmListByte = TlvUtil.getValue(pData, EmvTags.CVM_LIST);
        if (cardHolderByte != null) {
            String[] name = StringUtils.split(new String(cardHolderByte).trim(), CARD_HOLDER_NAME_SEPARATOR);
            if (name != null && name.length == 2) {
                card.setHolderFirstname(StringUtils.trimToNull(name[0]));
                card.setHolderLastname(StringUtils.trimToNull(name[1]));
            }
        }
        if (cvmListByte != null) {
            CVMList cvmList = new CVMList(cvmListByte);
            card.setCVMList(cvmList);
        }
    }


    /**
     * May be performed any time after Read Application Data and before completion of the terminal action analysis
     * <p>
     * Card holder verification is performed to ensure that the person presenting
     * the ICC is the person to whom the application in the card was issued.
     *
     */
    private void performCardholderVerification(byte[] unp) throws  CommunicationException {

        //TODO finish this
        //see page 108-112

        //The terminal shall use the cardholder verification related data in the
        //ICC to determine whether one of the issuer-specified cardholder
        //verification methods (CVMs) shall be executed.

        //If the CVM List is not present in the ICC, the terminal shall terminate
        //cardholder verification without setting the "Cardholder verification was performed" bit in the TSI.
        //Note:
        //A CVM List with no Cardholder Verification Rules is considered to be the same as a CVM List not being present.

        CVMList cvmList = card.getCVMList();
        if (cvmList == null || cvmList.getRules().isEmpty()) {
            EMVTerminal.getTerminalVerificationResults().setICCDataMissing(true);
            //TODO Set CVM Results to "3F0000" - "No CVM performed"
            return;
        }

        //If the CVM List is present in the ICC, the terminal shall process each
        //rule in the order in which it appears in the list according to the
        //following specifications.
        //Cardholder verification is completed when any one CVM is successfully
        //performed or when the list is exhausted.

        try {
            for (CVRule rule : cvmList.getRules()) {

                //Check condition code (second byte)

                //If any of the following is true:
                //- the conditions expressed in the second byte of a CV Rule are not satisfied, or
                //- data required by the condition (for example, the Application Currency Code
                //  or Amount, Authorised) is not present, or
                //- the CVM Condition Code is outside the range of codes understood
                //  by the terminal (which might occur if the terminal application
                //  program is at a different version level than the ICC application),
                //then the terminal shall bypass the rule and proceed to the next.

                if (!EMVTerminal.isCVMConditionSatisfied(rule)) { //Checks all 3
                    continue;
                }


                //determine whether the terminal supports the CVM
                if (EMVTerminal.isCVMSupported(rule)) {
                    //If the CVM is supported, the terminal shall attempt to perform it

                    if (rule.isPinRelated() && !EMVTerminal.getDoVerifyPinIfRequired()) {
                        //If the terminal bypassed PIN entry at the direction of either the merchant or the cardholder:
                        //Terminal shall set the "PIN entry required, PIN pad present, but PIN was not entered" bit in the TVR to 1.
                        //The terminal shall consider this CVM unsuccessful and shall continue cardholder
                        //verification processing in accordance with the card's CVM List
                        EMVTerminal.getTerminalVerificationResults().setPinEntryRequired_PINPadPresent_ButPINWasNotEntered(true);
                        if (!rule.applySucceedingCVRuleIfThisCVMIsUnsuccessful()) {
                            EMVTerminal.getTerminalVerificationResults().setCardholderVerificationWasNotSuccessful(true);
                            return;
                        }
                        continue;
                    }

                    switch (rule.getRule()) {
                        case NO_CVM_REQUIRED:
                            return;
                        case FAIL_PROCESSING:
                            //If the CVM just processed was "Fail CVM Processing", the terminal shall
                            //set the "Cardholder verification was not successful" bit in the TVR (b8 of byte 3)
                            //to 1 and no further CVMs shall be processed regardless of the
                            //setting of b7 of byte 1 in the first byte of the CV Rule
                            EMVTerminal.getTerminalVerificationResults().setCardholderVerificationWasNotSuccessful(true);
                            return;
                        case SIGNATURE_ON_PAPER:
                            if (EMVTerminal.hasSignatureOnPaper()) {
                                return;
                            }
                            break;
                        case ENCIPHERED_PIN_VERIFIED_BY_ICC_AND_SIGNATURE_ON_PAPER:
                            if (!EMVTerminal.hasSignatureOnPaper() && processVerifyPIN(true,unp)) {
                                return;
                            }
                            break;
                        case PLAINTEXT_PIN_VERIFIED_BY_ICC_AND_SIGNATURE_ON_PAPER:
                            if (EMVTerminal.hasSignatureOnPaper()) {
                                processVerifyPIN(false,unp);
                                return;
                            }
                            break;
                        case ENCIPHERED_PIN_VERIFIED_BY_ICC:
                            if (processVerifyPIN(true,unp)) {
                                return;
                            }
                            break;
                        case PLAINTEXT_PIN_VERIFIED_BY_ICC:
                            processVerifyPIN(false,unp);
                            return;
                        case ENCIPHERED_PIN_VERIFIED_ONLINE:
                            if (EMVTerminal.verifyEncipheredPinOnline()) {
                                EMVTerminal.getTerminalVerificationResults().setOnlinePINEntered(true);
                                return;
                            }
                            break;
                        case RESERVED_FOR_USE_BY_THE_INDIVIDUAL_PAYMENT_SYSTEMS:
                            //TODO
                        case RESERVED_FOR_USE_BY_THE_ISSUER:
                            //TODO
                        case NOT_AVAILABLE_FOR_USE:
                            //TODO fail

                    }

                } else {
                    if (rule.isPinRelated()) {
                        //In case the CVM was PIN-related, then in addition the terminal shall set the
                        //"PIN entry required and PIN pad not present or not working" bit (b5 of byte 3) of the TVR to 1
                        EMVTerminal.getTerminalVerificationResults().setPinEntryRequiredAndPINPadNotPresentOrNotWorking(true);
                    }
                }


                //Step 2
                //The CVM was not recognised, was not supported, or failed.
                //Check if we should try next rule
                if (!rule.applySucceedingCVRuleIfThisCVMIsUnsuccessful()) {
                    EMVTerminal.getTerminalVerificationResults().setCardholderVerificationWasNotSuccessful(true);
                    return;
                }

            }
        } finally {
            //When cardholder verification is completed, the terminal shall:
            //- set the CVM Results according to Book 4 section 6.3.4.5 (TODO)
            //- set the Cardholder verification was performed" bit in the TSI to 1.
            card.getTransactionStatusInformation().setCardholderVerificationWasPerformed(true);
        }

        //All cv rules have been processed and failed
        EMVTerminal.getTerminalVerificationResults().setCardholderVerificationWasNotSuccessful(true);


    }


    private boolean processVerifyPIN(boolean encipherPin,byte[] unp) throws  CommunicationException {

//		EMVApplication app = card.getSelectedApplication();
//
//		if(app.getPINTryCounter() == -1) { //-1 means we have not tried reading the PIN Try Counter before
//			readPINTryCounter();
//		}
//
//		if(app.getPINTryCounter() == 0) {
//			Log.debug("PIN Try limit exeeded. Unable to verify PIN.");
//			EMVTerminal.getTerminalVerificationResults().setPinTryLimitExceeded(true);
//			return false;
//		}
//
//		byte[] command;

//		while(app.getPINTryCounter() != 0) {
//
//			PasswordCallback pinInput = EMVTerminal.getPinInput();
//
//			char[] pin = pinInput.getPassword();
//			pinInput.clearPassword();
//
//			if(pin == null) { //Input aborted by user or failed
//				Log.debug("PIN input aborted or failed.");
//				return false;
//			}

        if(encipherPin) {
            //Recover the public key to be used for PIN encipherment
            CA ca = CA.getCA(new AID(card.getAid()));
            ICCPublicKeyCertificate iccPKCert = new ICCPublicKeyCertificate(new IssuerPublicKeyCertificate(ca),"");
            if(iccPKCert == null || !iccPKCert.validate() || iccPKCert.getICCPublicKey() == null) {
                Log.debug("Unable to encipher PIN: ICC Public Key Certificate not valid");
                return false;
            }

            //Get unpredictable number from ICC
            byte[] challenge = unp;
            if(challenge.length != 8) {
                Log.debug("Unable to encipher PIN: GET CHALLENGE returned response length "+challenge.length);
                return false;
            }

            //TODO encipher PIN
        }
        return true;
//			command = EMVAPDUCommands.verifyPIN(pin, !encipherPin);
//			Arrays.fill(pin, ' ');
//
//			Log.commandHeader("Send VERIFY (PIN) command");
//
//			CardResponse verifyResponse = EMVUtil.sendCmd(terminal, command);
//
//			if (verifyResponse.getSW() == SW.SUCCESS.getSW()) {
//				//Try to update PTC
//				if(app.getPINTryCounter() != -2) { //-2 means app does not support the command
//					readPINTryCounter();
//				}
//				return true;
//			} else {
//				if (verifyResponse.getSW() == SW.COMMAND_NOT_ALLOWED_AUTHENTICATION_METHOD_BLOCKED.getSW()
//						|| verifyResponse.getSW() == SW.COMMAND_NOT_ALLOWED_REFERENCE_DATA_INVALIDATED.getSW()) {
//					Log.info("No more retries left. CVM blocked");
//					EMVTerminal.getTerminalVerificationResults().setPinTryLimitExceeded(true);
//					app.setPINTryCounter(0);
//					return false;
//				} else if (verifyResponse.getSW1() == (byte) 0x63 && (verifyResponse.getSW2() & (byte)0xF0) == (byte) 0xC0) {
//					int numRetriesLeft = (verifyResponse.getSW2() & 0x0F);
//					Log.info("Wrong PIN. Retries left: "+numRetriesLeft);
//					app.setPINTryCounter(numRetriesLeft);
//				} else {
//					String description = SW.getSWDescription(verifyResponse.getSW());
//					Log.info("Application returned unexpected Status: 0x"+Util.short2Hex(verifyResponse.getSW())
//							+ (description != null && !description.isEmpty()?" ("+description+")":""));
//					return false;
//				}
//			}
//		}
//		EMVTerminal.getTerminalVerificationResults().setPinTryLimitExceeded(true);
//		return false;
    }




    public static String getDateMonth(char yymmdd) {

        Calendar ca1 = Calendar.getInstance();
        String yearmonday = "";
        int iDay = ca1.get(Calendar.DATE);
        int iMonth = ca1.get(Calendar.MONTH) + 1;
        int iYear = ca1.get(Calendar.YEAR);

        if (yymmdd == 'M') {
            if ((iMonth + "").length() == 1) {
                yearmonday = "0" + iMonth;
            } else {
                yearmonday = "" + iMonth;
            }
        }
        if (yymmdd == 'Y') {
            yearmonday = "" + iYear;
        }
        if (yymmdd == 'D') {
            if ((iDay + "").length() == 1) {
                yearmonday = "0" + iDay;
            } else {
                yearmonday = "" + iDay;
            }
        }
        return yearmonday;
    }


    /**
     * Extract card ICC data
     *
     * @param pData
     */
    protected void extractICCData(final byte[] pData, String unpNumber, final byte[] pdol,TerminalVerificationResults tvr) {
        String ICCDATA = "";
        String date = getDateMonth('Y').substring(2, 4) + getDateMonth('M') + getDateMonth('D');
        TLV DE9F26 = TlvUtil.getValueString(pData, EmvTags.APP_CRYPTOGRAM);
        TLV DE9F27 = TlvUtil.getValueString(pData, EmvTags.CRYPTOGRAM_INFORMATION_DATA);
        TLV DE9F10 = TlvUtil.getValueString(pData, EmvTags.ISSUER_APPLICATION_DATA);
        TLV DE9F36 = TlvUtil.getValueString(pData, EmvTags.APP_TRANSACTION_COUNTER);
        String DE9F37 = "9F3704" + unpNumber.substring(0, 8);
        String DE95 = "9505"+tvr.parse();//
        String DE9A = "9A03" + date;//
        String DE9C = "9C0100";//
        String DE5F34 = "5F3401"+ (TextUtils.isEmpty(card.getPANSeqNumber())?"00":card.getPANSeqNumber());//
        String DE9F02 = "9F0206"+ Util.zeropad(amount+"00",12);//
        String DE5F2A = "5F2A020566";//
        TLV DE82 = TlvUtil.getValueString(pdol, EmvTags.APPLICATION_INTERCHANGE_PROFILE);//
        String DE9F1A = "9F1A020566";//
        String DE9F03 = "9F0306000000000000";//
        String DE9F33 = "9F3303E0D8C8";//
//        String DE9F34 = "9F3403440302";//
        String DE9F34 = "9F3403420302";//
        String DE9F35 = "9F350122";

        ICCDATA += DE9F26.toSting();
        ICCDATA += DE9F27.toSting();

        ICCDATA += DE9F36.toSting();
        ICCDATA += DE82.toSting();
        ICCDATA += DE95;
        ICCDATA += DE9F37;
        ICCDATA += DE9A;
        ICCDATA += DE9C;
        ICCDATA += DE9F02;
        ICCDATA += DE5F2A;
        ICCDATA += DE9F1A;
        ICCDATA += DE9F03;
        ICCDATA += DE9F33;
        ICCDATA += DE9F34;
        ICCDATA += DE9F35;
        ICCDATA += DE5F34;
        if(DE9F10.getLength()>12){
            DE9F10.setLength(12);
        }
        ICCDATA += DE9F10.toSting();


        card.setIccData(ICCDATA);
    }

    /**
     * Method used to create GPO command and execute it
     *
     * @param pPdol     PDOL data
     * @param pProvider provider
     * @return return data
     */
    protected byte[] getGetProcessingOptions(final byte[] pPdol, final IProvider pProvider, byte[] unp,TerminalVerificationResults tvr) throws CommunicationException {
        // List Tag and length from PDOL
        List<TagAndLength> list = TlvUtil.parseTagAndLength(pPdol);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(EmvTags.COMMAND_TEMPLATE.getTagBytes()); // COMMAND
            // TEMPLATE
            out.write(TlvUtil.getLength(list)); // ADD total length
            if (list != null) {
                for (TagAndLength tl : list) {
                    out.write(EmvTerminal.constructValue(tl,unp,amount,tvr));
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Construct GPO Command:" + ioe.getMessage(), ioe);
        }
        return pProvider.transceive(new CommandApdu(CommandEnum.GPO, out.toByteArray(), 0).toBytes());
    }

    /**
     * Method used to get the field card
     *
     * @return the card
     */
    public EmvCard getCard() {
        return card;
    }

}

