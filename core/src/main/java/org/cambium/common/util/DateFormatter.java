package org.cambium.common.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateFormatter {
    public static DateTimeFormatter getForrmatter(String format) {
       return DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
    }
}
