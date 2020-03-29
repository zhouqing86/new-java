package com.newjava.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZoneServiceImplTest {

    private ZoneService zoneService;

    @BeforeEach
    void setUp() throws IOException {
        zoneService = new ZoneServiceImpl("zone_info.txt");
    }

    @ParameterizedTest
    @CsvSource({
            "CST, +08:00[Asia/Shanghai]",
            "' CST ', +08:00[Asia/Shanghai]",
            "cst, +08:00[Asia/Shanghai]",
    })
    void testGetZoneNameWithGmtOffsetByAbbreviation(String zoneAbbreviation, String expected) {
        String zoneNameWithGmtOffice = zoneService.zoneNameWithGmtOffice(zoneAbbreviation);
        assertEquals(expected, zoneNameWithGmtOffice);
    }
}
