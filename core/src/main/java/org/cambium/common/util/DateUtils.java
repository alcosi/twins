package org.cambium.common.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static boolean isInDayHourInterval(LocalDateTime localDateTime, int startHours, int endHours) {
        return localDateTime.toLocalTime().isAfter(LocalTime.of(startHours, 0))
                && localDateTime.toLocalTime().isBefore(LocalTime.of(endHours, 0));
    }

    public static LocalDateTime convertOrNull(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public static Timestamp convertOrNull(LocalDateTime localDateTime) {
        return localDateTime == null ? null : Timestamp.valueOf(localDateTime);
    }
}
