package org.twins.core.dao.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinLinkNoRelationsProjection implements EasyLoggable, TwinFieldStorage {
    private UUID id;

    private UUID srcTwinId;

    private UUID dstTwinId;

    private UUID linkId;

    private UUID createdByUserId;

    private Timestamp createdAt;

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
