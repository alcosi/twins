package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassSchemaV1")
public class TwinClassSchemaDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "domainId")
    public UUID domainId;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "createdByUserId")
    public UUID createdByUserId;
}