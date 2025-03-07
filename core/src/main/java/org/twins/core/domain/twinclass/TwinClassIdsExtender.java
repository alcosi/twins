package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassIdsExtender {
    private UUID twinClassId;
    private Boolean addExtendableTwinClassIds;
}
