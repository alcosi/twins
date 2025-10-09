package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionV1")
public class ProjectionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "an id of src twin, for which current config is pointed")
    @RelatedObject(type = TwinDTOv2.class, name = "srcPointedTwin")
    public UUID srcPointedTwinId;

    @Schema(description = "src twin class field id")
    @RelatedObject(type = TwinDTOv2.class, name = "srcTwinClassField")
    public UUID srcTwinClassFieldId;

    @Schema(description = "dst twin class fid")
    @RelatedObject(type = TwinDTOv2.class, name = "dstTwinClass")
    public UUID dstTwinClassId;

    @Schema(description = "dst twin class field id")
    @RelatedObject(type = TwinDTOv2.class, name = "dstTwinClassField")
    public UUID dstTwinClassFieldId;
}


