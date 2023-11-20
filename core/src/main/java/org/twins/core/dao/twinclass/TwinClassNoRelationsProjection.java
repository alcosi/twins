package org.twins.core.dao.twinclass;

import lombok.Getter;
import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
public class TwinClassNoRelationsProjection implements EasyLoggable {
    private UUID id;

    private UUID domainId;

    private String key;

    private boolean space;

    private boolean abstractt;

    private UUID nameI18NId;

    private UUID descriptionI18NId;

    private UUID createdByUserId;

    private Timestamp createdAt;

    private String logo;

    private UUID headTwinClassId;

    private UUID extendsTwinClassId;

    private int domainAliasCounter;

    public String easyLog(Level level) {
        return "twinClass[id:" + id + ", key:" + key + "]";
    }

}
