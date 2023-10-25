package org.cambium.common;

public interface EasyLoggable {
    String easyLog(Level level);

    public enum Level {
        SHORT, NORMAL, DETAILED
    }
}
