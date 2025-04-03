package org.cambium.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.twins.core.service.user.UserService.maskEmail;
import static org.twins.core.service.user.UserService.maskName;

public class JsonUtils {
    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
    private static final ObjectMapper LENIENT_MAPPER = new ObjectMapper()
            .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true)
            .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

    public static String mask(String[] nameList, String[] emailList, String message) {
        for (String name : nameList) {
            String regex = "(\"" + name + "\":\\s*\")([^\"]+)(\")";
            Matcher matcher = Pattern.compile("(\"" + name + "\":\\s*\")([^\"]+)(\")").matcher(message);
            while (matcher.find()) {
                message = message.replaceAll(regex, "$1" + maskName(matcher.group(2)) + "$3");
            }
        }
        for (String email : emailList) {
            String regex = "(\"" + email + "\":\\s*\")([^\"]+)(\")";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                message = message.replaceAll(regex, "$1" + maskEmail(matcher.group(2)) + "$3");
            }
        }
        return message;
    }

    public static String translationsMapToJson(Map<Locale, String> translations) {
        if (CollectionUtils.isEmpty(translations))
            return null;

        Map<String, String> stringKeyMap = translations.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        Map.Entry::getValue
                ));
        try {
            return DEFAULT_MAPPER.writeValueAsString(stringKeyMap);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static Map<Locale, String> jsonToTranslationsMap(String json) {
        if (StringUtils.isEmpty(json))
            return Map.of();
        try {
            Map<String, String> rawTranslations = LENIENT_MAPPER.readValue(json, new TypeReference<>(){});
            return rawTranslations.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> Locale.of(entry.getKey()),
                            Map.Entry::getValue
                    ));
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}