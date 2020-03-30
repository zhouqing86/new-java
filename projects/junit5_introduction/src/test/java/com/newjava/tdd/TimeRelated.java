package com.newjava.tdd;

import java.time.ZonedDateTime;

public class TimeRelated {
    public static boolean isExpired(ZonedDateTime zonedDateTime) {
        ZonedDateTime now = ZonedDateTime.now();
        return zonedDateTime.isAfter(now);
    }
}
