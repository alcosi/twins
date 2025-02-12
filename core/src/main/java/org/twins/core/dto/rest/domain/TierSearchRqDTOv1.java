package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.LongRangeDTOv1;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TierSearchRqV1")
public class TierSearchRqDTOv1 extends Request {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "permission schema id list")
    public Set<UUID> permissionSchemaIdList;

    @Schema(description = "permission schema id exclude list")
    public Set<UUID> permissionSchemaIdExcludeList;

    @Schema(description = "twinflow schema id list")
    public Set<UUID> twinflowSchemaIdList;

    @Schema(description = "twinflow schema id exclude list")
    public Set<UUID> twinflowSchemaIdExcludeList;

    @Schema(description = "twinclass schema id list")
    public Set<UUID> twinclassSchemaIdList;

    @Schema(description = "twinclass schema id exclude list")
    public Set<UUID> twinclassSchemaIdExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "attachments storage quota count range")
    public LongRangeDTOv1 attachmentsStorageQuotaCountRange;

    @Schema(description = "attachments storage quota size range")
    public LongRangeDTOv1 attachmentsStorageQuotaSizeRange;

    @Schema(description = "user count quota range")
    public LongRangeDTOv1 userCountQuotaRange;

    @Schema(description = "custom")
    public Ternary custom;
}