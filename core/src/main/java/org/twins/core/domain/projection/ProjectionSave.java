package org.twins.core.domain.projection;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class ProjectionSave {
    private UUID srcTwinPointerId;
    private UUID srcTwinClassFieldId;
    private UUID dstTwinClassId;
    private UUID dstTwinClassFieldId;
    private UUID projectionTypeId;
    private Boolean active;
    private Integer fieldProjectorFeaturerId;
    private HashMap<String, String> fieldProjectorParams;

}
