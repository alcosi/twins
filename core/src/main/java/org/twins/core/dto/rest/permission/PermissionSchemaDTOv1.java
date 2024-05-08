package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "PermissionGroupV1")
public class PermissionSchemaDTOv1 {
    @Schema(description = "id")
    private UUID id;

    @Schema(description = "domainId")
    private UUID domainId;

    @Schema(description = "businessAccountId")
    private UUID businessAccountId;

    @Schema(description = "name")
    private String name;

    @Schema(description = "description")
    private String description;

}
