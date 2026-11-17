package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainVersionAuditEntity;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.enums.domain.DomainOperationStatus;
import org.twins.core.enums.domain.DomainVersionAuditOperation;
import org.twins.core.enums.domain.DomainVersionedTable;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class DomainVersionAuditSearch extends EntitySearch<DomainVersionAuditEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> domainVersionIdList;
    private Set<UUID> domainVersionIdExcludeList;
    private Set<DomainVersionedTable> tableNameList;
    private Set<DomainVersionedTable> tableNameExcludeList;
    private Set<UUID> rowIdList;
    private Set<UUID> rowIdExcludeList;
    private Set<DomainVersionAuditOperation> operationList;
    private Set<DomainVersionAuditOperation> operationExcludeList;
    private Set<DomainOperationStatus> domainOperationStatusList;
    private Set<DomainOperationStatus> domainOperationStatusExcludeList;
    private Set<UUID> actorUserIdList;
    private Set<UUID> actorUserIdExcludeList;
    private DataTimeRange changedAt;
}
