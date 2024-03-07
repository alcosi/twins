package org.twins.core.dao.businessaccount;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public record BusinessAccountUserNoRelationProjection(UUID id, UUID businessAccountId, UUID userId) implements EasyLoggable {
    public String easyLog(Level level) {
        return "businessAccountUser[id:" + id + ", businessAccount:" + businessAccountId + ", user:" + userId;
    }
}
