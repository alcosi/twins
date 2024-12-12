package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public interface TwinFieldSimpleNoRelationsProjectionInterfaceBased extends EasyLoggable {
    UUID getId();
    UUID getTwinId();
    UUID getTwinClassFieldId();
    String getValue();

    default String easyLog(Level level) {
        return "twinField[" + getId() + "]";
    }
}
