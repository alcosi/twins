package org.cambium.common.util;

public class JsonUtils {
//    public static void mask(String[] nameList, String[] emailList, String message) {
//        message = "  \"name\": \"Ivan Sokolov\",\n" +
//                "  \"fullName\": \"Ivan Sokolov\",\n" +
//                "  \"email\": \"solol@email.com\",\n" +
//                "  \"avatar\": \"http://twins.org/a/avatar/carkikrefmkawfwfwg.png\"";
//        for (String s : nameList) {
//            Pattern namePattern = Pattern.compile("\"" + s + "\"\\s*:\\s*\"[^\"]+\"");
//            Matcher nameMatcher = namePattern.matcher(message);
//            if (nameMatcher.find()) {
//                String name = nameMatcher.group(1);
//
//                    message.replaceAll(namePattern, nameMatcher);
//            }
//
//            //            Pattern pattern = Pattern.compile("\"name\":\"" + maskName(s) + "\"");
//        }
//
//    }

    public static String mask(String jsonMessage, String[] keysToMask) {
        // Парсинг JSON-строки в HashMap
        HashMap<String, String> jsonMap = parseJson(jsonMessage);

        // Замена значений для указанных ключей
        for (String key : keysToMask) {
            if (jsonMap.containsKey(key)) {
                jsonMap.put(key, "MASKED");
            }
        }

        // Преобразование обновленной HashMap обратно в JSON-строку
        return mapToJson(jsonMap);
    }

    private static HashMap<String, String> parseJson(String jsonMessage) {
        HashMap<String, String> jsonMap = new HashMap<>();
        String[] pairs = jsonMessage.substring(1, jsonMessage.length() - 1).split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim().replaceAll("\"", "");
            String value = keyValue[1].trim().replaceAll("\"", "");
            jsonMap.put(key, value);
        }
        return jsonMap;
    }

    private static String mapToJson(HashMap<String, String> jsonMap) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        for (String key : jsonMap.keySet()) {
            jsonBuilder.append("\"").append(key).append("\": \"").append(jsonMap.get(key)).append("\", ");
        }
        if (jsonBuilder.length() > 1) {
            jsonBuilder.setLength(jsonBuilder.length() - 2); // Удаление лишних запятых и пробелов в конце
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
}
