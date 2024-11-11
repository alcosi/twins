package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainBusinessAccountUpdateV1")
public class DomainBusinessAccountUpdateRqDTOv1 extends Request {
    @Schema(description = "permissionSchemaId", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "twinClassSchemaId", example = DTOExamples.TWIN_CLASS_SCHEMA_ID)
    public UUID twinClassSchemaId;

    @Schema(description = "twinFlowSchemaId", example = DTOExamples.TWINFLOW_SCHEMA_ID)
    public UUID twinFlowSchemaId;

    @Schema(description = "Tier id.")
    public UUID tierId;

    @Schema(description = "Name")
    public String name;
}
