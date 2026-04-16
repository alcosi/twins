package org.cambium.common.util;

import org.springframework.web.util.InvalidUrlException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {
    public static boolean isValid(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            // Проверяем, что есть схема и хост
            return scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && host != null && !host.isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static URI toURI(String uriString) {
        try {
            return UriComponentsBuilder.fromUriString(uriString).encode().build().toUri();
        } catch (InvalidUrlException e) {
            return UriComponentsBuilder
                    .fromUriString(escapeInvalidPercentEncoding(uriString))
                    .encode()
                    .build()
                    .toUri();
        }
    }

    private static String escapeInvalidPercentEncoding(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        StringBuilder result = new StringBuilder(url.length());
        for (int i = 0; i < url.length(); i++) {
            char current = url.charAt(i);
            if (current == '%') {
                boolean hasTwoCharsAfter = i + 2 < url.length();
                boolean validHexPair = hasTwoCharsAfter
                        && isHexDigit(url.charAt(i + 1))
                        && isHexDigit(url.charAt(i + 2));
                if (!validHexPair) {
                    result.append("%25");
                    continue;
                }
            }
            result.append(current);
        }
        return result.toString();
    }

    private static boolean isHexDigit(char character) {
        return (character >= '0' && character <= '9')
                || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }

}
