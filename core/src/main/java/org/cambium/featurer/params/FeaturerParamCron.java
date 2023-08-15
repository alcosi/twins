package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.springframework.scheduling.support.CronExpression;

import java.util.Properties;

@FeaturerParamType(
        id = "CRON",
        description = "spring cron",
        regexp = ".*",
        example = "0 0 0 * * ?")
public class FeaturerParamCron extends FeaturerParam<CronExpression> {
    public FeaturerParamCron(String key) {
        super(key);
    }

    @Override
    public CronExpression extract(Properties properties) {
        return CronExpression.parse((String) properties.get(key));
    }
}
