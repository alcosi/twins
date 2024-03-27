package org.cambium.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twins.core.service.user.UserService.maskEmail;
import static org.twins.core.service.user.UserService.maskName;

public class JsonUtils {
    public static String mask(String[] nameList, String[] emailList, String message) {
        for (String name : nameList) {
            String regex = "(\"" + name + "\":\\s*\")([^\"]+)(\")";
            Matcher matcher = Pattern.compile("(\"" + name + "\":\\s*\")([^\"]+)(\")").matcher(message);
            while (matcher.find()) {
                message = message.replaceAll(regex, "$1"+ maskName(matcher.group(2)) +"$3");
            }
        }
        for (String email : emailList) {
            String regex = "(\"" + email + "\":\\s*\")([^\"]+)(\")";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                message = message.replaceAll(regex, "$1"+ maskEmail(matcher.group(2)) +"$3");
            }
        }
        return message;
    }
}
