package org.cambium.common.util;

import org.apache.commons.validator.GenericValidator;
import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

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
        if (!GenericValidator.isDate(value, pattern, false))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Date[" + value + "] does not match pattern[" + pattern + "]");
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        try {
            return formatter.parse(value).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (ParseException var6) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Value [" + value + "] is not a valid datetime in format " + pattern);
        }
    }

    public static String formatDate(LocalDateTime dateTime, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return formatter.format(date);
    }
}
