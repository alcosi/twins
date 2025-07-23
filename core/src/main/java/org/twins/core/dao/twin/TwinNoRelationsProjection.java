package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public interface TwinNoRelationsProjection extends EasyLoggable {

    UUID getId();
    UUID getHeadTwinId();

    default String easyLog(Level level) {
        return "twin[" + getId() + "]";
    }
}
