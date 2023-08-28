package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;
import java.util.UUID;

@FeaturerParamType(
        id = "UUID",
        description = "",
        regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}",
        example = "9a3f6075-f175-41cd-a804-934201ec969c")
public class FeaturerParamUUID extends FeaturerParam<UUID> {
    public FeaturerParamUUID(String key) {
        super(key);
    }

    @Override
    public UUID extract(Properties properties) {
        return UUID.fromString(properties.get(key).toString());
    }
}
