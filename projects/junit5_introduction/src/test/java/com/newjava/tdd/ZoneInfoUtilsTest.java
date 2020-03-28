package com.newjava.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZoneInfoUtilsTest {

    private Map<String, ZoneInfo> zoneInfoMap;

    private ZoneInfo buildZoneInfo(String zoneName, String gmtOffset, String... abbreviations) {
        ZoneInfo zoneInfo = new ZoneInfo();
        zoneInfo.setZoneName(zoneName);
        zoneInfo.setGmtOffset(gmtOffset);
        zoneInfo.setAbbreviations(Arrays.asList(abbreviations));
        return zoneInfo;
    }

    @BeforeEach
    void setUp() {
        zoneInfoMap = new HashMap<>();
        zoneInfoMap.put("shanghai", buildZoneInfo("Asia/Shanghai", "+ 08:00", "CST", "SHANGHAI"));
        zoneInfoMap.put("darwin", buildZoneInfo("Australia/Darwin", "+ 09:30", "ACST"));
        zoneInfoMap.put("sydney", buildZoneInfo("Australia/Sydney", "+ 11:00", "AEDT"));
        zoneInfoMap.put("empty",  new ZoneInfo());
    }

    @ParameterizedTest
    @CsvSource({
            "shanghai, +08:00[Asia/Shanghai]",
            "shanghai, +08:00[Asia/Shanghai]",
            "darwin, +09:30[Australia/Darwin]",
            "NOT_EXIST, ''",
    })
    void testGetZoneNameWithGmtOffsetByZoneInfo(String zoneInfoKey, String expected) {
        assertEquals(expected, ZoneInfoUtils.zoneNameWithGmtOffset(zoneInfoMap.get(zoneInfoKey)));
    }

    @ParameterizedTest
    @CsvSource({
            "CST, shanghai",
            "SHANGHAI, shanghai",
            "ACST, darwin",
    })
    void testGetZoneInfoByZoneInfoListAndAbbreviation(String abbreviation, String expectedZoneInfoKey) {
        List<ZoneInfo> zoneInfoList = Arrays.asList(
                zoneInfoMap.get("shanghai"),
                zoneInfoMap.get("darwin")
        );
        assertEquals(zoneInfoMap.get(expectedZoneInfoKey), ZoneInfoUtils.zoneInfo(zoneInfoList, abbreviation));
    }

    @Test
    void testGetZoneInfoWillReturnNullWhenNoZoneInfoFound() {
        List<ZoneInfo> zoneInfoList = Collections.emptyList();
        assertNull(ZoneInfoUtils.zoneInfo(zoneInfoList, "CST"));
    }

    @ParameterizedTest
    @CsvSource({
            "CST,  +08:00[Asia/Shanghai]",
            "ACST, +09:30[Australia/Darwin]",
            "NOT_EXIST, ''",
    })
    void testGetZoneNameWithGmtOffsetByZoneInfoList(String abbreviation, String expected) {
        List<ZoneInfo> zoneInfoList = Arrays.asList(
                zoneInfoMap.get("shanghai"),
                zoneInfoMap.get("darwin"),
                zoneInfoMap.get("empty")
        );

        assertEquals(expected, ZoneInfoUtils.zoneNameWithGmtOffset(zoneInfoList, abbreviation));
    }

    @Test
    void testGetZoneNameWithGmtOffsetWillReturnEmptyStringWhenZoneInfoListIsNull() {
        assertEquals("", ZoneInfoUtils.zoneNameWithGmtOffset(null, null));
    }

    @Test
    void testFilterOutAvailableZoneInfoList() {
        zoneInfoMap.get("shanghai").setAbbreviations(null);
        zoneInfoMap.get("darwin").setGmtOffset(null);
        zoneInfoMap.get("sydney").setZoneName(null);
        List<ZoneInfo> zoneInfoList = Arrays.asList(
                zoneInfoMap.get("shanghai"),
                zoneInfoMap.get("darwin"),
                zoneInfoMap.get("sydney"),
                zoneInfoMap.get("empty")
        );

        assertEquals(0, ZoneInfoUtils.availableZoneInfoList(zoneInfoList).size());
    }
}