package org.cambium.common.util;

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
}
