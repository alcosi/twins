package org.cambium.featurer.params;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.*;

@FeaturerParamType(
        id = "UUID_SET",
        description = "",
        regexp = FeaturerParamUUIDSet.UUID_SET_REGEXP,
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE)
public class FeaturerParamUUIDSet extends FeaturerParam<Set<UUID>> {
    public static final String UUID_SET_REGEXP = ".*"; //todo
    public static final String UUID_SET_EXAMPLE = "9a3f6075-f175-41cd-a804-934201ec969c, 1b3f6075-f175-41cd-a804-934201ec969c";
    public FeaturerParamUUIDSet(String key) {
        super(key);
    }

    @Override
    public Set<UUID> extract(Properties properties) {
        Set<UUID> ret = new LinkedHashSet<>();
        if (ObjectUtils.isNotEmpty(properties.get(key)) && StringUtils.isNotBlank(properties.get(key).toString()))
            for (String uuidString : properties.get(key).toString().split(","))
                ret.add(UUID.fromString(uuidString.trim()));
        return ret;
    }
}
