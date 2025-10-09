package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionExclusionV1")
public class ProjectionExclusionDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin_id")
    @RelatedObject(type = TwinClassBaseDTOv1.class, name = "twin")
    public UUID twinId;

    @Schema(description = "twin_class_field_id")
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "twinClassField")
    public UUID twinClassFieldId;
}


