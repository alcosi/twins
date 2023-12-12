package org.cambium.common;

public abstract class EasyLoggableImpl implements EasyLoggable {
    public String logNormal() {
        return easyLog(Level.NORMAL);
    }

    public String logShort() {
        return easyLog(Level.SHORT);
    }

    public String logDetailed() {
        return easyLog(Level.DETAILED);
    }
}
