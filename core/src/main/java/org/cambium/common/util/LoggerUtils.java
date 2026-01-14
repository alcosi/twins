package org.cambium.common.util;

import com.github.f4b6a3.uuid.UuidCreator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;


public class LoggerUtils {
    private static final String LOD_PREFIX = "logPrefix";
    private static final String CONTROLLER = "controller";
    private static final String SESSION = "session";
    private static final String TRACE_TREE = "traceTree";

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

    public static void logSession(UUID session) {
        logSession(session.toString().replace("-", "").toUpperCase());
    }

    public static void logSession() {
        logSession(UuidCreator.getTimeOrdered().toString().replace("-", "").toUpperCase());
    }


    public static void cleanMDC() {
        MDC.remove(LOD_PREFIX);
        MDC.remove(CONTROLLER);
        MDC.remove(SESSION);
        MDC.remove(TRACE_TREE);
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

    private static final String TRACE_TREE_LEAF = "|- ";
    private static final String TRACE_TREE_BRANCH = "| ";
    public static void traceTreeStart() {
        MDC.put(TRACE_TREE, TRACE_TREE_LEAF);
    }

    public static void traceTreeEnd() {
        MDC.remove(TRACE_TREE);
    }

    public static void traceTreeLevelUp() {
        String currentLeaf = MDC.get(TRACE_TREE);
        currentLeaf = (currentLeaf == null || currentLeaf.length() < 4) ? "" : currentLeaf.substring(2);
        MDC.put(TRACE_TREE, currentLeaf);
    }

    public static void traceTreeLevelDown() {
        String currentLeaf = MDC.get(TRACE_TREE);
        currentLeaf = (currentLeaf == null || currentLeaf.length() == 0) ? TRACE_TREE_LEAF : TRACE_TREE_BRANCH + currentLeaf;
        MDC.put(TRACE_TREE, currentLeaf);
    }

}
