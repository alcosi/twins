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
    private UUID id;

    @Schema(description = "permission schema id")
    private UUID permissionSchemaId;

    @Schema(description = "twinflow schema id")
    private UUID twinflowSchemaId;

    @Schema(description = "twinclass schema id")
    private UUID twinclassSchemaId;

    @Schema(description = "name")
    private String name;

    @Schema(description = "description")
    private String description;

    @Schema(description = "custom")
    private Boolean custom;

    @Schema(description = "attachments storage quota count")
    private Long attachmentsStorageQuotaCount;

    @Schema(description = "attachments storage quota size")
    private Long attachmentsStorageQuotaSize;

    @Schema(description = "user count quota")
    private Integer userCountQuota;
}
