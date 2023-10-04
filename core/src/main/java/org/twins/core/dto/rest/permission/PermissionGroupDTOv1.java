package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "PermissionGroupV1")
public class PermissionGroupDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_GROUP_ID)
    public UUID id;

    @Schema(description = "key")
    public String name;

    @Schema()
    public UUID twinClassId;

}
