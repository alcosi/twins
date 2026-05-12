package org.twins.core.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.domain.TwinBasicFields;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:TWIN_BASIC_FIELD",
        description = "",
        regexp = ".*",
        example = "name")
public class FeaturerParamBasicsTwinBasicField extends FeaturerParam<TwinBasicFields.Basics> {
    public FeaturerParamBasicsTwinBasicField(String key) {
        super(key);
    }

    @Override
    public TwinBasicFields.Basics extract(Properties properties) {
        if (!properties.containsKey(key)) {
            return null;
        }
        Object raw = properties.get(key);
        if (raw == null || raw.toString().isBlank()) {
            return null;
        }
        return TwinBasicFields.Basics.valueOf(raw.toString().strip());
    }

    @Override
    public void validate(String value) throws ServiceException {
        try {
            TwinBasicFields.Basics.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not valid twin basic field");
        }
    }
}
