package org.cambium.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import files.logging.HttpRegexJsonBodyMasking;

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

    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
    private static final ObjectMapper LENIENT_MAPPER = new ObjectMapper()
            .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true)
            .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);


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
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static Map<Locale, String> jsonToTranslationsMap(String json) {
        if (StringUtils.isEmpty(json)) return Map.of();
        try {
            Map<String, String> rawTranslations = LENIENT_MAPPER.readValue(json, new TypeReference<>(){});
            return rawTranslations.entrySet().stream()
                    .collect(Collectors.toMap(e -> Locale.of(e.getKey()), Map.Entry::getValue));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}