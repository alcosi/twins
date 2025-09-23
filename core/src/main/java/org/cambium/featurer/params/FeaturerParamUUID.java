package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;
import java.util.UUID;

@FeaturerParamType(
        id = "UUID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE)
public class FeaturerParamUUID extends FeaturerParam<UUID> {
    public static final String UUID_REGEXP = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    public static final String UUID_EXAMPLE = "9a3f6075-f175-41cd-a804-934201ec969c";
    public FeaturerParamUUID(String key) {
        super(key);
    }

    @Override
    public UUID extract(Properties properties) {
        var value = properties.get(key);
        if (value == null || value.toString().isBlank()) return null;
        return UUID.fromString(value.toString());
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(UUID_REGEXP))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] is not correct uuid");
    }
}
