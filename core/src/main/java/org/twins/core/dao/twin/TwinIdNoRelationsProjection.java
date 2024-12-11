package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public record TwinIdNoRelationsProjection(UUID id) implements EasyLoggable {
    @Override
    public String easyLog(Level level) {
        return "twin[" + id + "]";
    }
}
