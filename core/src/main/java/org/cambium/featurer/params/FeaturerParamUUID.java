package org.cambium.featurer.params;

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
        return UUID.fromString(properties.get(key).toString());
    }
}
