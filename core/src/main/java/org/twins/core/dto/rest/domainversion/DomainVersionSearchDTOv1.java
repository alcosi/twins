package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.enums.domain.DomainVersionStatus;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainVersionSearchDTOv1")
public class DomainVersionSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "version like list")
    public Set<String> versionLikeList;

    @Schema(description = "version not like list")
    public Set<String> versionNotLikeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "status list")
    public Set<DomainVersionStatus> statusList;

    @Schema(description = "status exclude list")
    public Set<DomainVersionStatus> statusExcludeList;

    @Schema(description = "previous domain version id list")
    public Set<UUID> previousDomainVersionIdList;

    @Schema(description = "previous domain version id exclude list")
    public Set<UUID> previousDomainVersionIdExcludeList;

    @Schema(description = "startedAt range")
    public DataTimeRangeDTOv1 startedAt;

    @Schema(description = "releasedAt range")
    public DataTimeRangeDTOv1 releasedAt;
}
