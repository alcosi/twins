package org.twins.core.featurer.domain.dto;


import java.util.List;

public record FeaturerParamInfo(
        String key, String name, String description, String type, Boolean optional, String defaultValue,
        List<String> exampleValues
) {
};