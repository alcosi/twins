package org.twins.core.dao.twinclass;

import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;

public record TwinClassNoRelationsProjection(
        UUID id,
        UUID domainId,
        String key,
        boolean space,
        boolean abstractt,
        UUID nameI18NId,
        UUID descriptionI18NId,
        UUID createdByUserId,
        Timestamp createdAt,
        String logo,
        UUID headTwinClassId,
        UUID extendsTwinClassId,
        int domainAliasCounter
) implements EasyLoggable {


    public String easyLog(Level level) {
        return "twinClass[id:" + id + ", key:" + key + "]";
    }

}
