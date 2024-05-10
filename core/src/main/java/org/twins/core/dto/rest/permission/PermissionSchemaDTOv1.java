package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "PermissionSchemaV1")
public class PermissionSchemaDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "domainId")
    public UUID domainId;

    @Schema(description = "businessAccountId")
    public UUID businessAccountId;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

}
