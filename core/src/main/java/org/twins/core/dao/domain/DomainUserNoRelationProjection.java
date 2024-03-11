package org.twins.core.dao.domain;

import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;

public record DomainUserNoRelationProjection(UUID id,  UUID domainId, UUID userId, Timestamp createdAt) implements EasyLoggable {
    public String easyLog(Level level) {
        return "domainUser[id:" + id + ", domainId:" + domainId + ", userId:" + userId + "]";
    }
}
