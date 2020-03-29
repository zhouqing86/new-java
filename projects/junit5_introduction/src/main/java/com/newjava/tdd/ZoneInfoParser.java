package com.newjava.tdd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ZoneInfoParser {

    public static final String REG_VALUE_SEPARATOR = "\\s*,\\s*";
    public static final String COMMENT_LINE_PREFIX = "#";

    public static ZoneInfo parse(String line) {
        if (Objects.isNull(line) || line.trim().startsWith(COMMENT_LINE_PREFIX)) {
            return null;
        }

        String[] splits = line.split(REG_VALUE_SEPARATOR);
        if (3 == splits.length) {
            List<String> abbreviations = new ArrayList<>();
            ZoneInfo zoneInfo = new ZoneInfo();
            abbreviations.add(splits[0].trim());
            zoneInfo.setAbbreviations(abbreviations);
            zoneInfo.setZoneName(splits[1].trim());
            zoneInfo.setGmtOffset(splits[2].trim());
            return zoneInfo;
        }
        return null;
    }

    public static List<String> readLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (path.isAbsolute()) {
            return Files.readAllLines(path);
        }
        ClassLoader classLoader = ZoneInfoParser.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(filePath)) {
            if (Objects.isNull(inputStream)) {
                throw new IOException();
            }
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.toList());
        }
    }

    public static List<ZoneInfo> parseFile(String filePath) throws IOException {
        List<String> lines = readLines(filePath);
        List<ZoneInfo> filteredList = lines.stream()
                .map(ZoneInfoParser::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ZoneInfoUtils.aggregate(filteredList);
    }
}
