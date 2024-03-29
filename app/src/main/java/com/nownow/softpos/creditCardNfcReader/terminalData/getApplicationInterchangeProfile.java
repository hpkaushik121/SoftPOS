package com.nownow.softpos.creditCardNfcReader.terminalData;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Application Interchange Profile
 * Indicates the capabilities of the card to support specific functions in the application
 *
 * EMV Book 3 Annex C1 (page 182)
 *
 * @author sasc
 */
 class ApplicationInterchangeProfile {
    private byte firstByte;
    private byte secondByte;

    public ApplicationInterchangeProfile(byte firstByte, byte secondByte) {
        this.firstByte = firstByte;
        this.secondByte = secondByte;
    }

    //Left most bit of firstByte is RFU

    public boolean isSDASupported() {
        return (firstByte & (byte) 0x40) > 0;
    }

    public boolean isDDASupported() {
        return (firstByte & (byte) 0x20) > 0;
    }

    public boolean isCardholderVerificationSupported() {
        return (firstByte & (byte) 0x10) > 0;
    }

    public boolean isTerminalRiskManagementToBePerformed() {
        return (firstByte & (byte) 0x08) > 0;
    }

    /**
     * When this bit is set to 1, Issuer Authentication using the EXTERNAL AUTHENTICATE command is supported
     */
    public boolean isIssuerAuthenticationIsSupported() {
        return (firstByte & (byte) 0x04) > 0;
    }

    public boolean isCDASupported() {
        return (firstByte & (byte) 0x01) > 0;
    }

    //The rest of the bits are RFU (Reserved for Future Use)

    public String getSDASupportedString(){
        if(isSDASupported()){
            return "Static Data Authentication (SDA) supported";
        }else{
            return "Static Data Authentication (SDA) not supported";
        }
    }

    public String getDDASupportedString(){
        if(isDDASupported()){
            return "Dynamic Data Authentication (DDA) supported";
        }else{
            return "Dynamic Data Authentication (DDA) not supported";
        }
    }

    public String getCardholderVerificationSupportedString(){
        if(isCardholderVerificationSupported()){
            return "Cardholder verification is supported";
        }else{
            return "Cardholder verification is not supported";
        }
    }

    public String getTerminalRiskManagementToBePerformedString(){
        if(isTerminalRiskManagementToBePerformed()){
            return "Terminal risk management is to be performed";
        }else{
            return "Terminal risk management does not need to be performed";
        }
    }

    public String getIssuerAuthenticationIsSupportedString(){
        if(isIssuerAuthenticationIsSupported()){
            return "Issuer authentication is supported";
        }else{
            return "Issuer authentication is not supported";
        }
    }

    public String getCDASupportedString(){
        if(isCDASupported()){
            return "CDA supported";
        }else{
            return "CDA not supported";
        }
    }

    public byte[] getBytes(){
        return new byte[]{firstByte, secondByte};
    }

    @Override
    public String toString(){
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw), 0);
        return sw.toString();
    }

    public void dump(PrintWriter pw, int indent){
        pw.println(Util.getSpaces(indent)+"Application Interchange Profile");
        String indentStr = Util.getSpaces(indent+Log.INDENT_SIZE);

        pw.println(indentStr+getSDASupportedString());
        pw.println(indentStr+getDDASupportedString());
        pw.println(indentStr+getCardholderVerificationSupportedString());
        pw.println(indentStr+getTerminalRiskManagementToBePerformedString());
        pw.println(indentStr+getIssuerAuthenticationIsSupportedString());
        pw.println(indentStr+getCDASupportedString());
    }

    public static void main(String[] args){
        ApplicationInterchangeProfile aip;
//        byte[] aipBytes = tlv.getValueBytes();
        aip = new ApplicationInterchangeProfile((byte)0x5c, (byte)0x00);
        System.out.println(aip.toString());
    }

}