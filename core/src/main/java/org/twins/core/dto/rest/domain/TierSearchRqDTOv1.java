package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierSearchRqV1")
public class TierSearchRqDTOv1 extends Request {

    @Schema(description = "id list")
    private Set<UUID> idList;

    @Schema(description = "id exclude list")
    private Set<UUID> idExcludeList;

    @Schema(description = "permission schema id list")
    private Set<UUID> permissionSchemaIdList;

    @Schema(description = "permission schema id exclude list")
    private Set<UUID> permissionSchemaIdExcludeList;

    @Schema(description = "twinflow schema id list")
    private Set<UUID> twinflowSchemaIdList;

    @Schema(description = "twinflow schema id exclude list")
    private Set<UUID> twinflowSchemaIdExcludeList;

    @Schema(description = "twinclass schema id list")
    private Set<UUID> twinclassSchemaIdList;

    @Schema(description = "twinclass schema id exclude list")
    private Set<UUID> twinclassSchemaIdExcludeList;

    @Schema(description = "name like list")
    private Set<String> nameLikeList;

    @Schema(description = "name not like list")
    private Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    private Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    private Set<String> descriptionNotLikeList;

    @Schema(description = "attachments storage quota count range")
    private LongRangeDTOv1 attachmentsStorageQuotaCountRange;

    @Schema(description = "attachments storage quota size range")
    private LongRangeDTOv1 attachmentsStorageQuotaSizeRange;

    @Schema(description = "user count quota range")
    private LongRangeDTOv1 userCountQuotaRange;

    @Schema(description = "custom")
    private Ternary custom;
}