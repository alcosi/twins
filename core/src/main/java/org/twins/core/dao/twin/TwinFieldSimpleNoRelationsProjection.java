package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public record TwinFieldSimpleNoRelationsProjection(UUID id, UUID twinId, UUID twinClassFieldId, String value) implements EasyLoggable {
    @Override
    public String easyLog(Level level) {
        return "twinField[" + id + "]";
    }
}
