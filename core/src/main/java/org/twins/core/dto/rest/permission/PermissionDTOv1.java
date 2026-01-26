package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "PermissionV1")
public class PermissionDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_ID)
    public UUID id;

    @Schema(description = "key")
    public String key;

    @Schema(description = "name", example = "Manager")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "group id", example = DTOExamples.PERMISSION_GROUP_ID)
    @RelatedObject(type = PermissionGroupDTOv1.class, name = "group")
    public UUID groupId;
}
