package com.nownow.softpos.creditCardNfcReader.terminalData;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Application Record
 *
 * @author sasc
 */
public class Record {

    private byte[] rawDataIncTag;
    private boolean isInvolvedInOfflineDataAuthentication = false;
    private int recordNumber;

    public Record(byte[] rawDataIncTag, int recordNumber, boolean isInvolvedInOfflineDataAuthentication){
        this.rawDataIncTag = rawDataIncTag;
        this.recordNumber = recordNumber;
        this.isInvolvedInOfflineDataAuthentication = isInvolvedInOfflineDataAuthentication;
    }

    public byte[] getRawData(){
        return Arrays.copyOf(rawDataIncTag, rawDataIncTag.length);
    }

    public boolean isInvolvedInOfflineDataAuthentication(){
        return isInvolvedInOfflineDataAuthentication;
    }

    public int getRecordNumber(){
        return recordNumber;
    }

    @Override
    public String toString(){
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw), 0);
        return sw.toString();
    }

    public void dump(PrintWriter pw, int indent){
        pw.println(Util.getSpaces(indent)+"Record: "+getRecordNumber());
        String indentStr = Util.getSpaces(indent+Log.INDENT_SIZE);
        pw.println(indentStr+"Length: "+rawDataIncTag.length);
        pw.println(indentStr+"Involved In Offline Data Authentication: "+
                isInvolvedInOfflineDataAuthentication());
    }

}