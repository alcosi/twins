package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierDTOv1")
public class TierDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "permission schema id")
    public UUID permissionSchemaId;

    @Schema(description = "twinflow schema id")
    public UUID twinflowSchemaId;

    @Schema(description = "twinclass schema id")
    public UUID twinclassSchemaId;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "custom")
    public Boolean custom;

    @Schema(description = "attachments storage quota count")
    public Integer attachmentsStorageQuotaCount;

    @Schema(description = "attachments storage quota size")
    public Long attachmentsStorageQuotaSize;

    @Schema(description = "user count quota")
    public Integer userCountQuota;
}
