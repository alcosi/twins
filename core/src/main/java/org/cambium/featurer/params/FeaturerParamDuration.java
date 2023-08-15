package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

@FeaturerParamType(
        id = "DURATION",
        description = "java 'ChronoUnit' name and number value, divided with ':' symbol. Valid units are: MILLIS, SECONDS, MINUTES, HOURS, DAYS, WEEKS,MONTHS or YEARS",
        regexp = "^(MILLIS|SECONDS|MINUTES|HOURS|DAYS|WEEKS|MONTHS|YEARS):\\d+$",
        example = "HOURS:3")
public class FeaturerParamDuration extends FeaturerParam<Duration> {
    public FeaturerParamDuration(String key) {
        super(key);
    }

    @Override
    public Duration extract(Properties properties) {
        String[] params = StringUtils.split((String) properties.get(key), ":");
        return Duration.of(Integer.parseInt(params[1]), ChronoUnit.valueOf(params[0]));
    }
}
