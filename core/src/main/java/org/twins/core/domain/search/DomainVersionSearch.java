package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.enums.domain.DomainVersionStatus;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class DomainVersionSearch extends EntitySearch<DomainVersionEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> versionLikeList;
    private Set<String> versionNotLikeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<DomainVersionStatus> statusList;
    private Set<DomainVersionStatus> statusExcludeList;
    private Set<UUID> previousDomainVersionIdList;
    private Set<UUID> previousDomainVersionIdExcludeList;
    private DataTimeRange startedAt;
    private DataTimeRange releasedAt;
}
