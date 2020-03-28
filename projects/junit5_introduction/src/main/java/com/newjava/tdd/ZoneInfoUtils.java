package com.newjava.tdd;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ZoneInfoUtils {

    public static final String REG_GMT_OFFSET_REPLACE_ALL = "[^0-9:\\-\\+]";
    public static final String PATTERN_ZONE_NAME_WITH_GMT_OFFSET = "{0}[{1}]";
    public static final String EMPTY_STRING = "";

    static String zoneNameWithGmtOffset(final ZoneInfo zoneInfo) {
        if (Objects.isNull(zoneInfo)) {
            return EMPTY_STRING;
        }
        String zoneName = zoneInfo.getZoneName();
        String gmtOffset = zoneInfo.getGmtOffset().replaceAll(REG_GMT_OFFSET_REPLACE_ALL, EMPTY_STRING);
        return MessageFormat.format(PATTERN_ZONE_NAME_WITH_GMT_OFFSET, gmtOffset, zoneName);
    }

    static ZoneInfo zoneInfo(final List<ZoneInfo> zoneInfoList,final String abbreviation) {
        Objects.requireNonNull(zoneInfoList);
        return zoneInfoList.stream()
                .filter(zoneInfo -> zoneInfo.getAbbreviations().contains(abbreviation))
                .findFirst()
                .orElse(null);
    }

    static List<ZoneInfo> availableZoneInfoList(List<ZoneInfo> zoneInfoList) {
        Objects.requireNonNull(zoneInfoList);

        return zoneInfoList.stream()
                .filter(zoneInfo -> Objects.nonNull(zoneInfo.getAbbreviations()))
                .filter(zoneInfo -> Objects.nonNull(zoneInfo.getGmtOffset()))
                .filter(zoneInfo -> Objects.nonNull(zoneInfo.getZoneName()))
                .collect(Collectors.toList());
    }

    public static String zoneNameWithGmtOffset(final List<ZoneInfo> zoneInfoList, final String abbreviation) {
        if (Objects.isNull(zoneInfoList)) {
            return EMPTY_STRING;
        }
        List<ZoneInfo> availableZoneInfoList = availableZoneInfoList(zoneInfoList);
        ZoneInfo zoneInfo = zoneInfo(availableZoneInfoList, abbreviation);
        return zoneNameWithGmtOffset(zoneInfo);
    }
}
