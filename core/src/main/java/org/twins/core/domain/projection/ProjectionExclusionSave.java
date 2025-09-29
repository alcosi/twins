package org.twins.core.domain.projection;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class ProjectionExclusionSave {
    private UUID twinId;
    private UUID twinClassFieldId;
}
