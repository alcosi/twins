package org.cambium.featurer.params;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

@FeaturerParamType(
        id = "STRING_SET",
        description = "",
        regexp = FeaturerParamStringSet.STRING_SET_REGEXP,
        example = FeaturerParamStringSet.STRING_SET_EXAMPLE)
public class FeaturerParamStringSet extends FeaturerParam<Set<String>> {
    public static final String STRING_SET_REGEXP = ".*";
    public static final String STRING_SET_EXAMPLE = "string|,| |."; // we are not using trim here, be careful with spaces
    private static final String SEPARATOR = "|";
    public FeaturerParamStringSet(String key) {
        super(key);
    }

    @Override
    public Set<String> extract(Properties properties) {
        Set<String> ret = new LinkedHashSet<>();
        if (ObjectUtils.isNotEmpty(properties.get(key)) && StringUtils.isNotBlank(properties.get(key).toString()))
            ret.addAll(Arrays.asList(properties.get(key).toString().split(Pattern.quote(SEPARATOR))));
        return ret;
    }
}
