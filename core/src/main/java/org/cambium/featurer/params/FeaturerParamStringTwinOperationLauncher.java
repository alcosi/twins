package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.twins.core.domain.twinoperation.TwinOperation;

import java.util.Properties;

@FeaturerParamType(
        id = "TWIN_OPERATION_LAUNCHER",
        description = "twin operation launcher",
        regexp = FeaturerParamStringTwinOperationLauncher.TWIN_OPERATION_LAUNCHER_REGEXP,
        example = "direct")
public class FeaturerParamStringTwinOperationLauncher  extends FeaturerParam<TwinOperation.Launcher> {
    public static final String TWIN_OPERATION_LAUNCHER_REGEXP = "direct|transition";

    public FeaturerParamStringTwinOperationLauncher(String key) {
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