package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowSchemaV1")
public class TwinflowSchemaDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWINFLOW_SCHEMA_ID)
    public UUID id;

    @Schema(description = "domain id", example = DTOExamples.DOMAIN_ID)
    public UUID domainId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "createdByUserId", example = DTOExamples.USER_ID)
    public UUID createdByUserId;
}
