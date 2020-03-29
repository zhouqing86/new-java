package com.newjava.tdd;

import java.util.List;

public class ZoneInfo {

    private String zoneName;

    private List<String> abbreviations;

    private String gmtOffset;

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public List<String> getAbbreviations() {
        return abbreviations;
    }

    public void setAbbreviations(List<String> abbreviations) {
        this.abbreviations = abbreviations;
    }

    public String getGmtOffset() {
        return gmtOffset;
    }

    public void setGmtOffset(String gmtOffset) {
        this.gmtOffset = gmtOffset;
    }

    @Override
    public String toString() {
        return "ZoneInfo{" +
                "zoneName='" + zoneName + '\'' +
                ", abbreviations=" + abbreviations +
                ", gmtOffset='" + gmtOffset + '\'' +
                '}';
    }
}
