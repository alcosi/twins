package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class BasicSearch {
    List<UUID> twinClassIdList;
    List<UUID> headerTwinIdList;
    List<UUID> statusIdList;
    List<UUID> assignerUserIdList;
    List<UUID> createdByUserIdList;
    List<UUID> ownerUserIdList;
    List<UUID> ownerBusinessAccountIdList;

    public BasicSearch addTwinClassId(UUID twinClassId) {
        if (twinClassIdList == null)
            twinClassIdList = new ArrayList<>();
        twinClassIdList.add(twinClassId);
        return this;
    }
}
