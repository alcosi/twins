package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@FeaturerParamType(
        id = "UUID",
        description = "",
        regexp = ".*", //todo
        example = "9a3f6075-f175-41cd-a804-934201ec969c")
public class FeaturerParamUUIDSet extends FeaturerParam<Set<UUID>> {
    public FeaturerParamUUIDSet(String key) {
        super(key);
    }

    @Override
    public Set<UUID> extract(Properties properties) {
        Set<UUID> ret = new HashSet<>();
        for (String uuidString : properties.get(key).toString().split(","))
            ret.add(UUID.fromString(uuidString));
        return ret;
    }
}
