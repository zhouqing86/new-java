package com.newjava.tdd;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ZoneInfoUtilsTest {

    private Map<String, ZoneInfo> zoneInfoMap;

    @BeforeEach
    void setUp() {
        zoneInfoMap = ZoneInfoTestHelper.buildZoneInfoMap();
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

    @Test
    void testAggregateWillCombineZoneInfoWithSameZoneName() {
        List<ZoneInfo> zoneInfoList = Arrays.asList(
                zoneInfoMap.get("darwin"),
                zoneInfoMap.get("darwin2")
        );

        List<ZoneInfo> aggregatedList = ZoneInfoUtils.aggregate(zoneInfoList);
        assertEquals(1, aggregatedList.size());
        ZoneInfo zoneInfo = aggregatedList.get(0);
        assertEquals("Australia/Darwin", zoneInfo.getZoneName());
        assertIterableEquals(Arrays.asList("ACST", "DARWIN"), zoneInfo.getAbbreviations());
    }

    @Test
    void testAggregateWillNotCombineZoneInfoWithDifferentZoneName() {
        List<ZoneInfo> zoneInfoList = Arrays.asList(
                zoneInfoMap.get("shanghai"),
                zoneInfoMap.get("darwin")
        );
        List<ZoneInfo> aggregatedList = ZoneInfoUtils.aggregate(zoneInfoList);
        System.out.println(aggregatedList);
        assertEquals(2, aggregatedList.size());
    }
}