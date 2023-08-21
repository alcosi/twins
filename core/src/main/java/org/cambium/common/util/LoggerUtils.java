package org.cambium.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;


public class LoggerUtils {
    private static final String LOD_PREFIX = "logPrefix";
    private static final String CONTROLLER = "controller";
    private static final String SESSION = "session";

    public static void logPrefix(String logPrefix) {
        MDC.put(LOD_PREFIX, logPrefix + " ");
    }

    public static void logPrefixAppend(String logPrefix) {
        String currentPrefix = MDC.get(LOD_PREFIX);
        MDC.put(LOD_PREFIX, (StringUtils.isNotEmpty(currentPrefix) ? currentPrefix + " " : "") + logPrefix + " ");
    }

    public static void logController(String controller) {
        MDC.put(CONTROLLER, controller);
    }

    public static void logSession(String session) {
        MDC.put(SESSION, session);
    }

    public static void logSession() {
        logSession(UUID.randomUUID().toString().replace("-", "").toUpperCase());
    }


    public static void cleanMDC() {
        MDC.remove(LOD_PREFIX);
        MDC.remove(CONTROLLER);
        MDC.remove(SESSION);
    }

    public static String getShortTrace(Throwable th, int depth) {
        StringBuilder ret = new StringBuilder(th.getClass().getName() + ": " + th.getMessage() + System.lineSeparator());
        StackTraceElement[] traceElements = th.getStackTrace();
        if (depth < 0 || depth > traceElements.length - 1)
            depth = traceElements.length - 1;
        for (int i = 0; i <= depth; i++) {
            ret.append("\t").append(traceElements[i]).append(System.lineSeparator());
        }
        return ret.append("\t...").toString();
    }

    public static String getShortTrace(Throwable th) {
        return th.toString() + System.lineSeparator() + getShortTrace(th, 3);
    }

    public static String prettyLog(String key, Object value) {
        return key + "[" + value + "] ";
    }

    public static String prettyLogNotBlank(String key, Object value) {
        return value != null && StringUtils.isNoneBlank(value.toString()) ? key + "[" + value + "] " : "";
    }

}
