package org.cambium.common;

import org.cambium.common.EasyLoggable;

public abstract class EasyLoggableImpl implements EasyLoggable {
    @Override
    public String toString() {
        return easyLog(Level.NORMAL);
    }
}
