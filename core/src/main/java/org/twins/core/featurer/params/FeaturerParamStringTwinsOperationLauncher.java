package org.twins.core.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.domain.twinoperation.TwinOperation;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:TWIN_OPERATION_LAUNCHER",
        description = "twin operation launcher",
        regexp = FeaturerParamStringTwinsOperationLauncher.TWIN_OPERATION_LAUNCHER_REGEXP,
        example = "direct")
public class FeaturerParamStringTwinsOperationLauncher extends FeaturerParam<TwinOperation.Launcher> {
    public static final String TWIN_OPERATION_LAUNCHER_REGEXP = "direct|transition";

    public FeaturerParamStringTwinsOperationLauncher(String key) {
        super(key);
    }

    @Override
    public TwinOperation.Launcher extract(Properties properties) {
        String value = (String) properties.get(key);
        return value != null ?
                TwinOperation.Launcher.valueOf(value.toLowerCase()) :
                TwinOperation.Launcher.direct;
    }

    @Override
    public void validate(String value)  throws ServiceException {
        if (value == null || !value.matches(TWIN_OPERATION_LAUNCHER_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] must be one of: " + TWIN_OPERATION_LAUNCHER_REGEXP);
        }
    }
}