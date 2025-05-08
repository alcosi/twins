package org.cambium.featurer.params;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@FeaturerParamType(
        id = "STRING_SET",
        description = "",
        regexp = FeaturerParamStringSet.UUID_SET_REGEXP,
        example = FeaturerParamStringSet.UUID_SET_EXAMPLE)
public class FeaturerParamStringSet extends FeaturerParam<Set<String>> {
    public static final String UUID_SET_REGEXP = ".*";
    public static final String UUID_SET_EXAMPLE = "first some text, second some text:123";
    public FeaturerParamStringSet(String key) {
        super(key);
    }

    @Override
    public Set<String> extract(Properties properties) {
        Set<String> ret = new HashSet<>();
        if (ObjectUtils.isNotEmpty(properties.get(key)) && StringUtils.isNotBlank(properties.get(key).toString()))
            for (String string : properties.get(key).toString().split(","))
                ret.add(string.trim());
        return ret;
    }
}
