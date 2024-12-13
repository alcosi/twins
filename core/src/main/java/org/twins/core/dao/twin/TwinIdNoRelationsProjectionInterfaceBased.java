package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public interface TwinIdNoRelationsProjectionInterfaceBased extends EasyLoggable {

    UUID getId();

    default String easyLog(Level level) {
        return "twin[" + getId() + "]";
    }
}
