package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "ProjectionSaveV1")
public class ProjectionSaveDTOv1 {
    @Schema(description = "src twin pointer id")
    public UUID srcTwinPointerId;

    @Schema(description = "src twin class field id")
    public UUID srcTwinClassFieldId;

    @Schema(description = "dst twin class fid")
    public UUID dstTwinClassId;

    @Schema(description = "dst twin class field id")
    public UUID dstTwinClassFieldId;

    @Schema(description = "projection type id")
    public UUID projectionTypeId;

    @Schema(description = "field projector featurer id")
    public Integer fieldProjectorFeaturerId;

    @Schema(description = "is projection active")
    public Boolean active;

    @Schema(description = "field projector params")
    public HashMap<String, String> fieldProjectorParams;
}
