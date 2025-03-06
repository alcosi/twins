package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassMap {
    private UUID twinClassId;
    private Boolean includeParentFields;
}
