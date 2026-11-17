package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.enums.domain.DomainOperationStatus;
import org.twins.core.enums.domain.DomainVersionAuditOperation;
import org.twins.core.enums.domain.DomainVersionedTable;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainVersionAuditSearchDTOv1")
public class DomainVersionAuditSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "domain version id list")
    public Set<UUID> domainVersionIdList;

    @Schema(description = "domain version id exclude list")
    public Set<UUID> domainVersionIdExcludeList;

    @Schema(description = "changed table list")
    public Set<DomainVersionedTable> tableNameList;

    @Schema(description = "changed table exclude list")
    public Set<DomainVersionedTable> tableNameExcludeList;

    @Schema(description = "changed row id list")
    public Set<UUID> rowIdList;

    @Schema(description = "changed row id exclude list")
    public Set<UUID> rowIdExcludeList;

    @Schema(description = "operation list")
    public Set<DomainVersionAuditOperation> operationList;

    @Schema(description = "operation exclude list")
    public Set<DomainVersionAuditOperation> operationExcludeList;

    @Schema(description = "domain operation status list")
    public Set<DomainOperationStatus> domainOperationStatusList;

    @Schema(description = "domain operation status exclude list")
    public Set<DomainOperationStatus> domainOperationStatusExcludeList;

    @Schema(description = "actor user id list")
    public Set<UUID> actorUserIdList;

    @Schema(description = "actor user id exclude list")
    public Set<UUID> actorUserIdExcludeList;

    @Schema(description = "changedAt range")
    public DataTimeRangeDTOv1 changedAt;
}
