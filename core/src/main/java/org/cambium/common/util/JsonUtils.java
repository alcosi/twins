package org.cambium.common.util;

import files.logging.HttpRegexJsonBodyMasking;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonUtils {
    protected final String[] fieldNames;
    protected final HttpRegexJsonBodyMasking masker;

    public JsonUtils(String[] fieldNames) {
        this.fieldNames = fieldNames;
        this.masker = new HttpRegexJsonBodyMasking(fieldNames == null ? Collections.emptyList() : Arrays.asList(fieldNames)) {{
            maskedBody = "*******";
        }};
    }

    private static final ObjectMapper DEFAULT_MAPPER = JsonMapper.builder().build();
    private static final ObjectMapper LENIENT_MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .build();


    public String mask(String message) {
        if (message == null || fieldNames == null || fieldNames.length == 0) {
            return message;
        }
        return masker.mask(message);

    }
    public static String translationsMapToJson(Map<Locale, String> translations) {
        if (CollectionUtils.isEmpty(translations)) return null;
        Map<String, String> stringKeyMap = translations.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
        try {
            return DEFAULT_MAPPER.writeValueAsString(stringKeyMap);
        } catch (JacksonException e) {
            return null;
        }
    }

    public static Map<Locale, String> jsonToTranslationsMap(String json) {
        if (StringUtils.isEmpty(json)) return Map.of();
        try {
            Map<String, String> rawTranslations = LENIENT_MAPPER.readValue(json, new TypeReference<>(){});
            return rawTranslations.entrySet().stream()
                    .collect(Collectors.toMap(e -> Locale.of(e.getKey()), Map.Entry::getValue));
        } catch (JacksonException e) {
            return null;
        }
    }

}