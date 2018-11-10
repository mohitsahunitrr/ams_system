/*
 * All rights reserved.
 */

package com.precisionhawk.ams.bean;

/**
 *
 * @author <a href="mailto:pchapman@pcsw.us">Philip A. Chapman</a>
 */
public class Address {

    protected String city;
    protected String country;
    protected String line1;
    protected String line2;
    protected String postalCode;
    protected String region;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(sb, line1);
        append(sb, line2);
        append(sb, city);
        append(sb, region);
        append(sb, postalCode);
        return sb.toString();
    }

    protected void append(StringBuilder sb, String s) {
        if (s != null && s.length() > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(s);
        }
    }

}
