package com.aicortex.softpos.creditCardNfcReader.terminalData;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Short File Identifier
 *
 * @author sasc
 */
public class ShortFileIdentifier {

    private int sfi;

    public ShortFileIdentifier(int sfi) {
        this.sfi = sfi;
    }

    public int getValue() {
        return sfi;
    }

    public static String getDescription(int sfi) {
        if (sfi < 1 || sfi > 30) {
            throw new IllegalArgumentException("Illegal SFI value. SFI must be in the range 1-30. sfi=" + sfi);
        }
        if (sfi <= 10) { //1-10
            return "Governed by the EMV specification";
        } else if (sfi <= 20) { //11-20
            return "Payment system-specific";
        } else { //21-30
            return "Issuer-specific";
        }
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw), 0);
        return sw.toString();
    }

    public void dump(PrintWriter pw, int indent) {
        pw.println(Util.getSpaces(indent) + "Short File Identifier:");
        String indentStr = Util.getSpaces(indent + Log.INDENT_SIZE);

        pw.println(indentStr + sfi + " (" + ShortFileIdentifier.getDescription(sfi) + ")");
    }
}