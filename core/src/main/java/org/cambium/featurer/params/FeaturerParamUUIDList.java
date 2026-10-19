package org.cambium.featurer.params;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@FeaturerParamType(
        id = "UUID_LIST",
        description = "ordered list of UUIDs; order and duplicates are preserved (unlike UUID_SET)",
        regexp = FeaturerParamUUIDList.UUID_LIST_REGEXP,
        example = FeaturerParamUUIDList.UUID_LIST_EXAMPLE)
public class FeaturerParamUUIDList extends FeaturerParam<List<UUID>> {
    public static final String UUID_LIST_REGEXP = ".*"; //todo
    public static final String UUID_LIST_EXAMPLE = "9a3f6075-f175-41cd-a804-934201ec969c, 9a3f6075-f175-41cd-a804-934201ec969c";
    public FeaturerParamUUIDList(String key) {
        super(key);
    }

    @Override
    public List<UUID> extract(Properties properties) {
        var ret = new ArrayList<UUID>();
        if (ObjectUtils.isNotEmpty(properties.get(key)) && StringUtils.isNotBlank(properties.get(key).toString()))
            for (String uuidString : properties.get(key).toString().split(","))
                ret.add(UUID.fromString(uuidString.trim()));
        return ret;
    }
}
