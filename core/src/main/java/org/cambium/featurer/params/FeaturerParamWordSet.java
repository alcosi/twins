package org.cambium.featurer.params;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.*;
import java.util.stream.Collectors;

@FeaturerParamType(
        id = "WORD_SET",
        description = "words splited by comma",
        regexp = ".*",
        example = "Hello world!")
public class FeaturerParamWordSet extends FeaturerParam<Set<String>> {
    public FeaturerParamWordSet(String key) {
        super(key);
    }

    @Override
    public Set<String> extract(Properties properties) {
        String str = (String) properties.get(key);
        if (StringUtils.isNotEmpty(str))
            return Arrays.stream(str.split(",")).map(String::trim).collect(Collectors.toSet());
        else
            return new HashSet<>();
    }
}
