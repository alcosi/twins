package org.twins.core.dao.twin;

import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;
public record TwinLinkNoRelationsProjection(UUID id, UUID srcTwinId, UUID dstTwinId, UUID linkId,UUID createdByUserId, Timestamp createdAt) implements EasyLoggable, TwinFieldStorage {
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinLink[id:" + id + "]";
            case NORMAL:
                return "twinLink[id:" + id + ", linkId:" + linkId +  "]";
            default:
                return "twinLink[id:" + id + ", linkId:" + linkId + ", srcTwinId:" + srcTwinId + ", dstTwinId:" + dstTwinId +  "]";
        }
    }
}
