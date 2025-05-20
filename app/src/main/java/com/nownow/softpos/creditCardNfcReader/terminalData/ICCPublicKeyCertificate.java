package com.aicortex.softpos.creditCardNfcReader.terminalData;

import com.aicortex.softpos.creditCardNfcReader.iso7816emv.TLV;
import com.aicortex.softpos.creditCardNfcReader.utils.TlvUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

//import id.paypro.nfc.creditCardNfcReader.iso7816emv.TLV;
//import id.paypro.nfc.creditCardNfcReader.utils.TlvUtil;

/**
 *
 * @author sasc
 */
public class ICCPublicKeyCertificate {

    private IssuerPublicKeyCertificate issuerPublicKeyCert;
    private ICCPublicKey iccPublicKey;
    private boolean isValid = false;
    private byte[] signedBytes;
    private byte[] pan = new byte[10];
    private byte certFormat;
    private byte[] certExpirationDate = new byte[2];
    private byte[] certSerialNumber = new byte[3];
    private int hashAlgorithmIndicator;
    private int iccPublicKeyAlgorithmIndicator;
    private byte[] hash = new byte[20];
    private boolean validationPerformed = false;
    private String hexFileLocation;
    public ICCPublicKeyCertificate(IssuerPublicKeyCertificate issuerPublicKeyCert, String hexFileLocation) {
//        this.application = application;
        this.issuerPublicKeyCert = issuerPublicKeyCert;
        this.iccPublicKey = new ICCPublicKey();
        this.hexFileLocation=hexFileLocation;
    }

    public void setSignedBytes(byte[] signedBytes) {
        this.signedBytes = signedBytes;
    }

    public IssuerPublicKeyCertificate getIssuerPublicKeyCertificate() {
        return issuerPublicKeyCert;
    }

    public ICCPublicKey getICCPublicKey() {
        //Don't validate just yet. Perform validation after ALL apprecords have been read
//        if(!validationPerformed){
//            validate();
//        }
        return iccPublicKey; //never null
    }

    //This method must only be called after ALL application records have been read
    public boolean validate() {
        if (validationPerformed) { //Validation already run
            return isValid();
        }
        validationPerformed = true;

        if (issuerPublicKeyCert == null) {
            return false;
        }

        if (issuerPublicKeyCert == null){
            //No isser public key cert found
            return isValid();
        }

        if(!issuerPublicKeyCert.validate()){ //Init the cert
            isValid = false;
            return isValid();
        }

        IssuerPublicKey issuerPublicKey = issuerPublicKeyCert.getIssuerPublicKey();

        byte[] recoveredBytes = Util.performRSA(signedBytes, issuerPublicKey.getExponent(), issuerPublicKey.getModulus());

        ByteArrayInputStream bis = new ByteArrayInputStream(recoveredBytes);

        if (bis.read() != 0x6a) { //Header
            throw new RuntimeException("Header != 0x6a");
        }

        certFormat = (byte) bis.read();

        if (certFormat != 0x04) { //Always 0x04
            throw new RuntimeException("Invalid certificate format");
        }

        bis.read(pan, 0, pan.length);

        bis.read(certExpirationDate, 0, certExpirationDate.length);

        bis.read(certSerialNumber, 0, certSerialNumber.length);

        hashAlgorithmIndicator = bis.read() & 0xFF;

        iccPublicKeyAlgorithmIndicator = bis.read() & 0xFF;

        int iccPublicKeyModLengthTotal = bis.read() & 0xFF;

        int iccPublicKeyExpLengthTotal = bis.read() & 0xFF;

        int modBytesLength = bis.available() - 21;

        if(iccPublicKeyModLengthTotal < modBytesLength) {
            //The mod bytes block in the cert contains padding
            //we don't want padding in our key
            modBytesLength = iccPublicKeyModLengthTotal;
        }

        byte[] modtmp = new byte[modBytesLength];

        bis.read(modtmp, 0, modtmp.length);

        iccPublicKey.setModulus(modtmp);

        //Now read padding bytes (0xbb), if available
        //The padding bytes are not used
        byte[] padding = new byte[bis.available()-21];
        bis.read(padding, 0, padding.length);

        bis.read(hash, 0, hash.length);

        ByteArrayOutputStream hashStream = new ByteArrayOutputStream();

        //Header not included in hash
        hashStream.write(certFormat);
        hashStream.write(pan, 0, pan.length);
        hashStream.write(certExpirationDate, 0, certExpirationDate.length);
        hashStream.write(certSerialNumber, 0, certSerialNumber.length);
        hashStream.write((byte)hashAlgorithmIndicator);
        hashStream.write((byte)iccPublicKeyAlgorithmIndicator);
        hashStream.write((byte)iccPublicKeyModLengthTotal);
        hashStream.write((byte)iccPublicKeyExpLengthTotal);
        byte[] ipkModulus = iccPublicKey.getModulus();
        int numPadBytes = issuerPublicKey.getModulus().length-42-ipkModulus.length;
        Log.debug("issuerMod: "+issuerPublicKey.getModulus().length + " iccMod: "+ipkModulus.length + " padBytes: "+numPadBytes);
        if(numPadBytes > 0){
            //If NIC <= NI – 42, consists of the full
            //ICC Public Key padded to the right
            //with NI – 42 – NIC bytes of value
            //'BB'
            hashStream.write(ipkModulus, 0, ipkModulus.length);
            for(int i=0; i<numPadBytes; i++){
                hashStream.write((byte)0xBB);
            }
        }else{
            //If NIC > NI – 42, consists of the NI –
            //42 most significant bytes of the
            //ICC Public Key
            //and the NIC – NI + 42 least significant bytes of the ICC Public Key
            hashStream.write(ipkModulus, 0, ipkModulus.length);
        }

        byte[] ipkExponent = iccPublicKey.getExponent();
        hashStream.write(ipkExponent, 0, ipkExponent.length);

        byte[] offlineAuthenticationRecords = getOfflineDataAuthenticationRecords(new ApplicationFileLocator(Util.fromHexString(hexFileLocation)));
        hashStream.write(offlineAuthenticationRecords, 0, offlineAuthenticationRecords.length);
        //Trailer not included in hash

        Log.debug("HashStream:\n"+Util.prettyPrintHex(hashStream.toByteArray()));

        byte[] sha1Result = null;
        try {
            sha1Result = Util.calculateSHA1(hashStream.toByteArray());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-1 hash algorithm not available", ex);
        }

        if (!Arrays.equals(sha1Result, hash)) {
            throw new RuntimeException("Hash is not valid");
        }


        int trailer = bis.read();

        if (trailer != 0xbc) {//Trailer
            throw new RuntimeException("Trailer != 0xbc");
        }

        if (bis.available() > 0) {
            throw new RuntimeException("Error parsing certificate. Bytes left=" + bis.available());
        }
        isValid = true;
        return true;
    }

    public boolean isValid() {
        return isValid;
    }


    public byte[] getOfflineDataAuthenticationRecords(ApplicationFileLocator fileLocator) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (ApplicationElementaryFile aef :fileLocator.getApplicationElementaryFiles()) {

            //Only those records identified in the AFL as participating in offline data authentication are to be processed.
            if (aef.getNumRecordsInvolvedInOfflineDataAuthentication() == 0) {
                continue;
            }

            for (Record record : aef.getRecords()) {

                if (!record.isInvolvedInOfflineDataAuthentication()) {
                    continue;
                }

                byte[] fileRawData = record.getRawData();
                if (fileRawData == null || fileRawData.length < 2) {
                    //The records read for offline data authentication shall be TLV-coded with tag equal to '70'
                    throw new RuntimeException("File Raw Data was null or invalid length (less than 2): " + fileRawData == null ? "null" : String.valueOf(fileRawData.length));
                }
                //The records read for offline data authentication shall be TLV-coded with tag equal to '70'
                if (fileRawData[0] != (byte) 0x70) {
                    //If the records read for offline data authentication are not TLV-coded with tag equal to '70'
                    //then offline data authentication shall be considered to have been performed and to have failed;
                    //that is, the terminal shall set the 'Offline data authentication was performed' bit in the TSI to 1,
                    //and shall set the appropriate 'SDA failed' or 'DDA failed' or 'CDA failed' bit in the TVR.
                    //TODO
                }

                //The data from each record to be included in the offline data authentication input
                //depends upon the SFI of the file from which the record was read.
                int sfi = aef.getSFI().getValue();
                if (sfi >= 1 && sfi <= 10) {
                    //For files with SFI in the range 1 to 10, the record tag ('70') and the record length
                    //are excluded from the offline data authentication process. All other data in the
                    //data field of the response to the READ RECORD command (excluding SW1 SW2) is included.

                    //Get the 'valueBytes'
                    TLV ttlv= TlvUtil.getNextTLV(new ByteArrayInputStream(fileRawData));
                    BERTLV tlv =new BERTLV(ttlv.getTag(),ttlv.getValueBytes());
                    stream.write(tlv.getValueBytes(), 0, tlv.getValueBytes().length);
                } else {
                    //For files with SFI in the range 11 to 30, the record tag ('70') and the record length
                    //are not excluded from the offline data authentication process. Thus all data in the
                    //data field of the response to the READ RECORD command (excluding SW1 SW2) is included
                    stream.write(fileRawData, 0, fileRawData.length);
                }
            }


        }

        //After all records identified by the AFL have been processed, the Static Data Authentication Tag List is processed,
        //if it exists. If the Static Data Authentication Tag List exists, it shall contain only the tag for the
        //Application Interchange Profile. The tag must represent the AIP available in the current application.
        //The value field of the AIP is to be concatenated to the current end of the input string.
// TODO:        //The tag and length of the AIP are not included in the concatenation.
// TODO:        StaticDataAuthenticationTagList sdaTagListObject = this.getStaticDataAuthenticationTagList();
// TODO:        if (sdaTagListObject != null) {
// TODO:            List<Tag> sdaTagList = sdaTagListObject.getTagList();
// TODO:            if (sdaTagList != null && !sdaTagList.isEmpty()) {
// TODO:                if (sdaTagList.size() > 1 || sdaTagList.get(0) != EMVTags.APPLICATION_INTERCHANGE_PROFILE) {
// TODO:                    throw new RuntimeException("SDA Tag list must contain only the 'Application Interchange Profile' tag: " + sdaTagList);
// TODO:                } else {
// TODO:                    byte[] aipBytes = this.getApplicationInterchangeProfile().getBytes();
// TODO:                    stream.write(aipBytes, 0, aipBytes.length);
// TODO:                }
// TODO:            }
// TODO:        }

        return stream.toByteArray();
    }


    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw), 0);
        return sw.toString();
    }

    public void dump(PrintWriter pw, int indent) {
        pw.println(Util.getSpaces(indent) + "ICC Public Key Certificate");
        String indentStr = Util.getSpaces(indent + Log.INDENT_SIZE);

        if(!validationPerformed){
            validate();
        }

        if (isValid()) {
            pw.println(indentStr + "Primary Account Number (PAN): " + Util.byteArrayToHexString(pan));

            pw.println(indentStr + "Certificate Format: " + certFormat);
            pw.println(indentStr + "Certificate Expiration Date (MMYY): " + Util.byteArrayToHexString(certExpirationDate));
            pw.println(indentStr + "Certificate Serial Number: " + Util.byteArrayToHexString(certSerialNumber));
            pw.println(indentStr + "Hash Algorithm Indicator: " + hashAlgorithmIndicator +" (=SHA-1)");
            pw.println(indentStr + "ICC Public Key Algorithm Indicator: " + iccPublicKeyAlgorithmIndicator +" (=RSA)");
            pw.println(indentStr + "Hash: " + Util.byteArrayToHexString(hash));

            iccPublicKey.dump(pw, indent + Log.INDENT_SIZE);
        } else {
            if (this.issuerPublicKeyCert == null) {
                pw.println(indentStr + "NO ISSUER CERTIFICATE FOUND. UNABLE TO VALIDATE CERTIFICATE");
            } else {
                pw.println(indentStr + "CERTIFICATE NOT VALID");
            }
        }
    }

//    public static void main(String[] args){
//
//        EMVApplication app = new EMVApplication();
//
//        EMVUtil.parseProcessingOpts(Util.fromHexString("80 0e 3c 00 08 02 02 00 10 01 01 00 18 01 03 01"), app);
//
//        System.out.println(app.getApplicationFileLocator());
//
//        byte[] appRecord = Util.fromHexString("70 56 5f 25 03 12 08 01 5f 24 03 15 08 31 5a 08"
//                +"46 92 98 20 36 76 95 49 5f 34 01 01 9f 07 02 ff"
//                +"80 8e 14 00 00 00 00 00 00 00 00 02 01 44 03 01"
//                +"03 02 03 1e 03 1f 00 9f 0d 05 b8 60 ac 88 00 9f"
//                +"0e 05 00 10 00 00 00 9f 0f 05 b8 68 bc 98 00 5f"
//                +"28 02 06 42 9f 4a 01 82");
//
//        EMVUtil.printResponse(appRecord, true);
//
//        Record record = new Record(appRecord, 1, true);
//        app.getApplicationFileLocator().getApplicationElementaryFiles().get(2).setRecord(1, record);
//
//        StaticDataAuthenticationTagList staticDataAuthTagList = new StaticDataAuthenticationTagList(new byte[]{(byte)0x82});
//        app.setStaticDataAuthenticationTagList(staticDataAuthTagList);
//
//
//
//
//        IssuerPublicKeyCertificate issuerPKCert = new IssuerPublicKeyCertificate(CA.getCA(Util.fromHexString("A0 00 00 00 03")));
//
//        issuerPKCert.setSignedBytes(Util.fromHexString(" SIGNED BYTES HERE "));
//
//        issuerPKCert.setCAPublicKeyIndex(7);
//
//        issuerPKCert.getIssuerPublicKey().setExponent(new byte[]{0x03});
//        issuerPKCert.getIssuerPublicKey().setRemainder(Util.fromHexString(" REMAINDER "));
//
//        ICCPublicKeyCertificate iccPKCert = new ICCPublicKeyCertificate(app, issuerPKCert);
//        iccPKCert.setSignedBytes(Util.fromHexString(" SIGNED BYTES HERE "));
//
//        iccPKCert.getICCPublicKey().setExponent(new byte[]{0x03});
//
//        System.out.println(iccPKCert);
//    }
}