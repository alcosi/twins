package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionV1")
public class ProjectionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "an id of src twin, for which current config is pointed")
    public UUID srcPointedTwinId;

    @Schema(description = "is projection active")
    public Boolean active;

    @Schema(description = "src twin class field id")
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "srcTwinClassField")
    public UUID srcTwinClassFieldId;

    @Schema(description = "dst twin class fid")
    @RelatedObject(type = TwinClassDTOv1.class, name = "dstTwinClass")
    public UUID dstTwinClassId;

    @Schema(description = "dst twin class field id")
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "dstTwinClassField")
    public UUID dstTwinClassFieldId;

    @Schema(description = "projection type id")
    @RelatedObject(type = ProjectionTypeDTOv1.class, name = "projectionType")
    public UUID projectionTypeId;

    @Schema(description = "field projector featurer id")
    public Integer fieldProjectorFeaturerId;

    @Schema(description = "field projector params")
    public HashMap<String, String> fieldProjectorParams;
}


