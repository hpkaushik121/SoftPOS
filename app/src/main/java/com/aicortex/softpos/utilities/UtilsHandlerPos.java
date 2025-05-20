package com.aicortex.softpos.utilities;

import java.util.Calendar;
import java.util.Locale;

public class UtilsHandlerPos {
    public static byte[] hexToByte(String hexString) {

        String str = new String("0123456789ABCDEF");
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0, j = 0; i < hexString.length(); i++) {

            byte firstQuad = (byte) ((str.indexOf(hexString.charAt(i))) << 4);
            byte secondQuad = (byte) str.indexOf(hexString.charAt(++i));
            bytes[j++] = (byte) (firstQuad | secondQuad);
        }
        return bytes;
    }
    public static byte[] hexStringToBytes(String s) {


        int iLength = s.length();
        int iBuff = iLength / 2;

        byte[] buff = new byte[iBuff];

        //if(s != null && !s.equals("")){
        ////System.out.println("Attempting Hex 2 Bin Conversion");

        int j = 0;
        for (int i = 0; i < iLength; i += 2) {
            String s1 = s.substring(i, i + 2);
            buff[j++] = (byte) Integer.parseInt(s1, 16);
        }
        // }
        return buff;

    }
    public static String getMPOSMMDDHHMMSS() {

        Calendar ca1 = Calendar.getInstance(Locale.getDefault());
        String yearmonday = "";
//        ca1.add(Calendar.HOUR, -4);
//        ca1.add(Calendar.MINUTE, -30);
        int DD = ca1.get(Calendar.DAY_OF_MONTH);
        int MM = ca1.get(Calendar.MONTH);
        int iHour = ca1.get(Calendar.HOUR_OF_DAY);
        int iMinute = ca1.get(Calendar.MINUTE);
        int iSeconds = ca1.get(Calendar.SECOND);

        if (((MM + 1) + "").length() == 1) {
            yearmonday = "0" + (MM + 1);
        } else {
            yearmonday = "" + (MM + 1);
        }
        if ((DD + "").length() == 1) {
            yearmonday += "0" + DD;
        } else {
            yearmonday += "" + DD;
        }

        if ((iHour + "").length() == 1) {
            yearmonday += "0" + iHour;

        } else {
            yearmonday += "" + iHour;
        }
        if ((iMinute + "").length() == 1) {
            yearmonday += "0" + iMinute;

        } else {
            yearmonday += "" + iMinute;
        }
        if ((iSeconds + "").length() == 1) {
            yearmonday += "0" + iSeconds;

        } else {
            yearmonday += "" + iSeconds;
        }
        // System.out.println("yearmonday"+yearmonday);
        return yearmonday;
    }
    public static String maskPan(String PAN) {
        String panStart4digit = PAN.substring(0, 4);
        //String panlast4digit = PAN.substring(PAN.length() - 5, PAN.length() - 1);
        String panlast4digit = PAN.substring(PAN.length() - 4);
        return panStart4digit + "XXXXXXXX" + panlast4digit;
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
}
