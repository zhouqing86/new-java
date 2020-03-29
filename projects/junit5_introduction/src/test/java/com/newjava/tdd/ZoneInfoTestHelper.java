package com.newjava.tdd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ZoneInfoTestHelper {

    public static ZoneInfo buildZoneInfo(String zoneName, String gmtOffset, String... abbreviations) {
        ZoneInfo zoneInfo = new ZoneInfo();
        zoneInfo.setZoneName(zoneName);
        zoneInfo.setGmtOffset(gmtOffset);
        zoneInfo.setAbbreviations(Arrays.asList(abbreviations));
        return zoneInfo;
    }

    public static Map<String, ZoneInfo> buildZoneInfoMap() {
        Map<String, ZoneInfo> map = new HashMap<>();
        map.put("shanghai", buildZoneInfo("Asia/Shanghai", "+ 08:00", "CST", "SHANGHAI"));
        map.put("darwin", buildZoneInfo("Australia/Darwin", "+ 09:30", "ACST"));
        map.put("darwin2", buildZoneInfo("Australia/Darwin", "+ 09:30", "DARWIN"));
        map.put("sydney", buildZoneInfo("Australia/Sydney", "+ 11:00", "AEDT"));
        map.put("empty",  new ZoneInfo());
        return map;
    }
}
