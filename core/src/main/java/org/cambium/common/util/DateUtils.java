package org.cambium.common.util;

import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

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

    public static LocalDateTime parseDateTime(String value, String pattern) throws ServiceException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        boolean hasTime = pattern.contains("H") || pattern.contains("m") || pattern.contains("s");
        boolean hasDate = pattern.contains("y") || pattern.contains("M") || pattern.contains("d");

        try {
            if (hasDate && hasTime) {
                // Дата + время
                return LocalDateTime.parse(value, formatter);
            } else if (hasDate) {
                // Только дата
                LocalDate date = LocalDate.parse(value, formatter);
                return date.atStartOfDay();
            } else if (hasTime) {
                // Только время
                LocalTime time = LocalTime.parse(value, formatter);
                return LocalDate.now().atTime(time);
            } else {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                        "Pattern [" + pattern + "] is invalid for parsing datetime");
            }
        } catch (DateTimeParseException e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Value [" + value + "] is not a valid datetime in format " + pattern);
        }
    }


    public static String formatDate(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
