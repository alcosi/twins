package org.cambium.featurer.params;

import org.apache.commons.lang3.StringUtils;
import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@FeaturerParamType(
        id = "WORD_LIST",
        description = "words splited by comma",
        regexp = ".*",
        example = "Hello world!")
public class FeaturerParamWordList extends FeaturerParam<List<String>> {
    public FeaturerParamWordList(String key) {
        super(key);
    }

    @Override
    public List<String> extract(Properties properties) {
        String str = (String) properties.get(key);
        if (StringUtils.isNotEmpty(str))
            return Arrays.stream(str.split(",")).map(String::trim).collect(Collectors.toList());
        else
            return new ArrayList<>();
    }
}
