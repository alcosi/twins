package org.cambium.common;

public interface EasyLoggable {
    String easyLog(Level level);

    default String logNormal() {
        return easyLog(Level.NORMAL);
    }

    default String logShort() {
        return easyLog(Level.SHORT);
    }

    default String logDetailed() {
        return easyLog(Level.DETAILED);
    }

    public enum Level {
        SHORT, NORMAL, DETAILED
    }
}
