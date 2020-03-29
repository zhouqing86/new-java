package com.newjava.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ZoneInfoParserTest {

    @Test
    void testParseFileAndReturnZoneInfoList() throws IOException {
        List<ZoneInfo> zoneInfoList = ZoneInfoParser.parseFile("test_zone_info.txt");
        assertEquals(2, zoneInfoList.size());
    }

    @ParameterizedTest
    @CsvSource({
            "'CST, Asia/Shanghai, + 08:00', CST, Asia/Shanghai, + 08:00",
            "' CST  ,  Asia/Shanghai  ,  + 08:00  ', CST, Asia/Shanghai, + 08:00",
    })
    void testParseStringThenGenerateZoneInfo(String line,
                                             String expectedAbbreviation,
                                             String expectedZoneName,
                                             String expectedGmtOffset) {
        ZoneInfo zoneInfo = ZoneInfoParser.parse(line);
        assertEquals(expectedZoneName, zoneInfo.getZoneName());
        assertEquals(expectedGmtOffset, zoneInfo.getGmtOffset());
        assertEquals(1, zoneInfo.getAbbreviations().size());
        assertEquals(expectedAbbreviation, zoneInfo.getAbbreviations().get(0));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            // The elements number(separate by ,) in a line is not equal to 3
            "A",
            "A, B",
            "A, B, C, D",

            // A line with comment #
            "# CST, Asia/Shanghai, + 08:00",
            "   # CST, Asia/Shanghai, + 08:00",
    })
    void testParseWithUnexpectedInputWillReturnNull(String line) {
        assertNull(ZoneInfoParser.parse(line));
    }

    @Nested
    class ParseFileTest {
        @TempDir
        Path sharedTempDir;

        String fileAbsolutePath;

        @BeforeEach
        void setUp() throws FileNotFoundException {
            Path file = sharedTempDir.resolve("test.txt");
            try (PrintWriter pw = new PrintWriter(file.toFile())){
                fileAbsolutePath = file.toAbsolutePath().toString();
                pw.println("# Abbreviation, Zone Name, GMT Offset");
                pw.println("SHANGHAI, Asia/Shanghai, + 08:00");
                pw.println("ACST, Australia/Darwin, + 09:30");
            }
        }

        @Test
        void testReadFileWithAbsolutePathAndReturnStringList() throws IOException {
            List<String> lines = ZoneInfoParser.readLines(fileAbsolutePath);
            assertEquals(3, lines.size());
        }

        @Test
        void testReadFileWithClassPathAndReturnStringList() throws IOException {
            List<String> lines = ZoneInfoParser.readLines("test_zone_info.txt");
            assertEquals(4, lines.size());
        }

        @Test
        void testReadFileWithNotExistFileWillThrowIOException() {
            final String absolutePath = "/ANY_PATH/NOT_EXISTED_FILE.txt";
            assertThrows(IOException.class, () -> ZoneInfoParser.readLines(absolutePath));
            assertThrows(IOException.class, () -> ZoneInfoParser.readLines("NOT_EXISTED_FILE.txt"));
        }
    }
}
