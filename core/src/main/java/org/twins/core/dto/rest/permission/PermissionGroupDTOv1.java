package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "PermissionGroupV1")
public class PermissionGroupDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_GROUP_ID)
    public UUID id;

    @Schema(description = "key", example = DTOExamples.PERMISSION_GROUP_KEY)
    public String key;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassBaseDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "twin class")
    public TwinClassDTOv1 twinClass;
}


