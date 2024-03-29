package com.nownow.softpos.creditCardNfcReader.terminalData;

import java.util.Arrays;


/**
 * Registered Application Provider Identifier
 *
 * @author sasc
 */
public class RID {

    private byte[] rid;
    private String applicant;
    private String country;

    public RID(byte[] rid, String applicant, String country){
        if (rid == null){
            throw new IllegalArgumentException("Argument 'rid' cannot be null");
        }
        if (rid.length != 5) {
            throw new RuntimeException("RID length != 5. Length=" + rid.length);
        }
        if(applicant == null){
            applicant = "";
        }
        if(country == null){
            country = "";
        }
        this.rid = rid;
        this.applicant = applicant;
        this.country = country;
    }

    public RID(String rid, String applicant, String country) {
        this(Util.fromHexString(rid), applicant, country);
    }

    public byte[] getRIDBytes() {
        return Arrays.copyOf(rid, rid.length);
    }

    public String getApplicant(){
        return applicant;
    }

    public String getCountry(){
        return country;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RID other = (RID) obj;
        if (!Arrays.equals(this.rid, other.rid)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Arrays.hashCode(this.rid);
        return hash;
    }

    @Override
    public String toString() {
        return Util.prettyPrintHex(rid) + " " + applicant + " " + country;
    }
}