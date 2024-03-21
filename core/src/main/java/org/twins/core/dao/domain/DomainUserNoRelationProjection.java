package org.twins.core.dao.domain;

import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.UUID;

public record DomainUserNoRelationProjection(UUID id,  UUID domainId, UUID userId, Timestamp createdAt, Locale i18nLocaleId) implements EasyLoggable {
    public String easyLog(Level level) {
        return "domainUser[id:" + id + ", domainId:" + domainId + ", userId:" + userId + "]";
    }
}
