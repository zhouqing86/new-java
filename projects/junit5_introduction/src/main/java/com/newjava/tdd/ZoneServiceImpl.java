package com.newjava.tdd;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ZoneServiceImpl implements ZoneService {

    private final List<ZoneInfo> zoneInfoList;

    public ZoneServiceImpl(String filePath) throws IOException {
        super();
        zoneInfoList = ZoneInfoParser.parseFile(filePath);
    }

    @Override
    public String zoneNameWithGmtOffice(String zoneAbbreviation) {
        if (Objects.isNull(zoneAbbreviation)) {
            return "";
        }
        String processedAbbreviation = zoneAbbreviation.trim().toUpperCase();
        return ZoneInfoUtils.zoneNameWithGmtOffset(zoneInfoList, processedAbbreviation);
    }
}
