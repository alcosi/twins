package org.twins.core.dto.rest.tier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TierSaveV1")
public class TierSaveDTOv1 {

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "custom", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean custom;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "twinflow schema id", example = DTOExamples.TWINFLOW_SCHEMA_ID)
    public UUID twinflowSchemaId;

    @Schema(description = "twinclass schema id", example = DTOExamples.TWIN_CLASS_SCHEMA_ID)
    public UUID twinClassSchemaId;

    @Schema(description = "attachments storage quota count", example = DTOExamples.COUNT)
    public Integer attachmentsStorageQuotaCount;

    @Schema(description = "attachments storage quota size", example = DTOExamples.COUNT)
    public Long attachmentsStorageQuotaSize;

    @Schema(description = "user count quota", example = DTOExamples.COUNT)
    public Integer userCountQuota;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}

