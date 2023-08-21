package org.cambium.common.util;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static boolean isInDayHourInterval(LocalDateTime localDateTime, int startHours, int endHours) {
        return localDateTime.toLocalTime().isAfter(LocalTime.of(startHours, 0))
                && localDateTime.toLocalTime().isBefore(LocalTime.of(endHours, 0));
    }
}
