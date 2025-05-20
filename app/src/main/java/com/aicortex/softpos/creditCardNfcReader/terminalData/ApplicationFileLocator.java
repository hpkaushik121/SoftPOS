package com.aicortex.softpos.creditCardNfcReader.terminalData;


import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Application File Locator (AFL)
 * Indicates the location (SFI, range of records) of the AEFs related to a given application
 *
 * @author sasc
 */
public class ApplicationFileLocator {
    private LinkedList<ApplicationElementaryFile> aefList = new LinkedList<ApplicationElementaryFile>();

    public List<ApplicationElementaryFile> getApplicationElementaryFiles(){
        return Collections.unmodifiableList(aefList);
    }

    public ApplicationFileLocator(byte[] data){
        if(data.length % 4 != 0) throw new RuntimeException("Length is not a multiple of 4. Length="+data.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        while(bis.available() > 0){
            byte[] tmp = new byte[4];
            bis.read(tmp, 0, tmp.length);
            aefList.add(new ApplicationElementaryFile(tmp));
        }

    }

    @Override
    public String toString(){
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw), 0);
        return sw.toString();
    }

    public void dump(PrintWriter pw, int indent){
        pw.println(Util.getSpaces(indent)+"Application File Locator");
        for(ApplicationElementaryFile aef : aefList){
            aef.dump(pw, indent+Log.INDENT_SIZE);
        }
    }

    public static void main(String[] args){
        System.out.println(new ApplicationFileLocator(Util.fromHexString("08 01 01 00 10 01 05 00 18 01 01 01 18 02 02 00")).toString());
        System.out.println(new ApplicationFileLocator(Util.fromHexString("08 01 01 00 10 01 02 00 10 04 05 00 18 01 02 01")).toString());
    }
}