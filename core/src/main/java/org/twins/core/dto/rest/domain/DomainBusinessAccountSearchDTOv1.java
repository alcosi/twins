package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.dto.rest.IntegerRangeDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainBusinessAccountSearchDTOv1")
public class DomainBusinessAccountSearchDTOv1 {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "business account id list")
    public List<UUID> businessAccountIdList;

    @Schema(description = "business account id exclude list")
    public List<UUID> businessAccountIdExcludeList;

    @Schema(description = "business account name like keyword list(AND)")
    public List<String> businessAccountNameLikeList;

    @Schema(description = "business account name exclude like keyword list(OR)")
    public List<String> businessAccountNameNotLikeList;

    @Schema(description = "business account permission schema id list")
    public List<UUID> permissionSchemaIdList;

    @Schema(description = "business account permission schema id exclude list")
    public List<UUID> permissionSchemaIdExcludeList;

    @Schema(description = "business account notification schema id list")
    public List<UUID> notificationSchemaIdList;

    @Schema(description = "business account notification schema id exclude list")
    public List<UUID> notificationSchemaIdExcludeList;

    @Schema(description = "business account twinflow schema id list")
    public List<UUID> twinflowSchemaIdList;

    @Schema(description = "business account twinflows chema id exclude list")
    public List<UUID> twinflowSchemaIdExcludeList;

    @Schema(description = "business account twin class schema id list")
    public List<UUID> twinClassSchemaIdList;

    @Schema(description = "business account twin class schema id exclude list")
    public List<UUID> twinClassSchemaIdExcludeList;

    @Schema(description = "business account tier id list")
    public Set<UUID> tierIdList;

    @Schema(description = "business account tier id exclude list")
    public Set<UUID> tierIdExcludeList;

    @Schema(description = "used of storage size at range")
    public IntegerRangeDTOv1 storageUsedSizeRange;

    @Schema(description = "used of storage file count at range")
    public IntegerRangeDTOv1 storageUsedCountRange;

    @Schema(description = "created at range")
    public DataTimeRangeDTOv1 createdAt;
}
