package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountUserSortField;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainBusinessAccountUserSearchDTOv1")
public class DomainBusinessAccountUserSearchDTOv1 {

    @Schema(description = "user id list")
    public Set<UUID> userIdList;

    @Schema(description = "user id exclude list")
    public Set<UUID> userIdExcludeList;

    @Schema(description = "business account id list")
    public Set<UUID> businessAccountIdList;

    @Schema(description = "business account id exclude list")
    public Set<UUID> businessAccountIdExcludeList;

    @Schema(description = "user group id list")
    public Set<UUID> userGroupIdList;

    @Schema(description = "user group id exclude list")
    public Set<UUID> userGroupIdExcludeList;

    @Schema(description = "last activity at range")
    public DataTimeRangeDTOv1 lastActivityAt;

    @Schema(description = "created at range")
    public DataTimeRangeDTOv1 createdAt;

    @Schema(description = "Sort field. Default: createdAt")
    public DomainBusinessAccountUserSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
