package org.cambium.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.twins.core.service.user.UserService.maskEmail;
import static org.twins.core.service.user.UserService.maskName;

@RequiredArgsConstructor
public class JsonUtils {
    protected final String[] fieldNames;
    protected final Pattern pattern = buildMultiFieldPattern(fieldNames);

    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
    private static final ObjectMapper LENIENT_MAPPER = new ObjectMapper()
            .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true)
            .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);


    public String mask(String message) {
        if (message == null || fieldNames == null || fieldNames.length == 0) {
            return message;
        }
        Pattern pattern = buildMultiFieldPattern(fieldNames);
        Matcher matcher = pattern.matcher(message);

        // RE2/J использует StringBuffer для appendReplacement
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            // Берем префикс (ключ) + маску + суффикс (кавычку)
            // Важно: quoteReplacement нужен, чтобы $ и \ в тексте не ломали замену
            String replacement = Matcher.quoteReplacement(matcher.group(1) + mask() + matcher.group(3));
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }


    public static String mask(String[] nameList, String[] emailList, String message) {
        if (message == null) return null;

        String result = message;

        // 1. Проход для имен (используем maskName)
        if (nameList != null && nameList.length > 0) {
            Pattern namePattern = buildMultiFieldPattern(nameList);
            Matcher matcher = namePattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String originalValue = matcher.group(2);
                String replacement = Matcher.quoteReplacement(
                        matcher.group(1) + maskName(originalValue) + matcher.group(3)
                );
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        // 2. Проход для email (используем maskEmail)
        if (emailList != null && emailList.length > 0) {
            Pattern emailPattern = buildMultiFieldPattern(emailList);
            Matcher matcher = emailPattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String originalValue = matcher.group(2);
                String replacement = Matcher.quoteReplacement(
                        matcher.group(1) + maskEmail(originalValue) + matcher.group(3)
                );
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    /**
     * Вспомогательный метод для построения быстрого RE2/J паттерна.
     * Создает структуру: (?:\"key1\"|\"key2\"):\s*\"((?:[^"\\]|\\.)*)\"
     */
    private static Pattern buildMultiFieldPattern(String[] fields) {
        StringBuilder sb = new StringBuilder();

        // Группа 1: Открывающая часть, например "password": "
        sb.append("(\"(?:");
        for (int i = 0; i < fields.length; i++) {
            sb.append(Pattern.quote(fields[i]));
            if (i < fields.length - 1) {
                sb.append("|");
            }
        }
        sb.append(")\":\\s*\")"); // Конец группы 1

        // Группа 2: Значение JSON.
        // Логика: ([^"\\]|\\.)* -> "Любой символ кроме кавычки/слеша ИЛИ слеш+любой символ"
        sb.append("((?:[^\"\\\\]|\\\\.)*)");

        // Группа 3: Закрывающая кавычка
        sb.append("(\")");

        return Pattern.compile(sb.toString());
    }

    // Остальные методы (Jackson) остаются без изменений
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

    protected static String mask() {
        return "*******";
    }
}