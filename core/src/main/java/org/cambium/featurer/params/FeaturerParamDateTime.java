package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

@FeaturerParamType(
        id = "DATETIME",
        description = "date and time in format yyyy-MM-dd'T'HH:mm:ss",
        regexp = FeaturerParamDateTime.DATETIME_REGEXP,
        example = "2024-03-15T14:30:00")
public class FeaturerParamDateTime extends FeaturerParam<LocalDateTime> {
    public static final String DATETIME_REGEXP = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public FeaturerParamDateTime(String key) {
        super(key);
    }

    @Override
    public LocalDateTime extract(Properties properties) {
        String value = (String) properties.get(key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(value, formatter);
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        if (!value.matches(DATETIME_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, 
                "param[" + key + "] value[" + value + "] does not match required format: " + DATETIME_PATTERN);
        }
        try {
            LocalDateTime.parse(value, formatter);
        } catch (DateTimeParseException e) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, 
                "param[" + key + "] value[" + value + "] cannot be parsed as datetime: " + e.getMessage());
        }
    }
} 