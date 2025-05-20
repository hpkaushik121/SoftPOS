package com.aicortex.softpos.creditCardNfcReader.terminalData;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author sasc
 */
public class IssuerPublicKey {
    private byte[] exponent = new byte[0];
    private byte[] modulus = new byte[0];
    private byte[] remainder = new byte[0];

    public IssuerPublicKey(){
    }

    public void setExponent(byte[] exp){
        this.exponent = exp;
    }

    public void setModulus(byte[] mod){
        this.modulus = mod;
    }

    public void setRemainder(byte[] remainder){
        this.remainder = remainder;
    }

    public int getKeyLengthInBytes(){
        return modulus.length+remainder.length;
    }

    public byte[] getExponent(){
        byte[] exponentCopy = new byte[exponent.length];
        System.arraycopy(exponent, 0, exponentCopy, 0, exponent.length);
        return exponentCopy;
    }

    public byte[] getModulus(){
        byte[] modulusCopy = new byte[modulus.length+remainder.length];
        System.arraycopy(modulus, 0, modulusCopy, 0, modulus.length);
        System.arraycopy(remainder, 0, modulusCopy, modulus.length, remainder.length);
        return modulusCopy;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw), 0);
        return sw.toString();
    }

    public void dump(PrintWriter pw, int indent) {
        pw.println(Util.getSpaces(indent) + "Issuer Public Key");
        String indentStr = Util.getSpaces(indent + Log.INDENT_SIZE);

        pw.println(indentStr + "Length: "+getKeyLengthInBytes()*8+"bit");
        pw.println(indentStr + "Exponent:");
        pw.println(indentStr + Util.getSpaces(Log.INDENT_SIZE) + Util.prettyPrintHex(Util.byteArrayToHexString(getExponent()), indent+Log.INDENT_SIZE*2));
        pw.println(indentStr + "Modulus:");
        pw.println(indentStr + Util.getSpaces(Log.INDENT_SIZE) + Util.prettyPrintHex(Util.byteArrayToHexString(getModulus()), indent+Log.INDENT_SIZE*2));

    }
}