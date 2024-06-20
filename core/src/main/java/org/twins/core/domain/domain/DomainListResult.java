package org.twins.core.domain.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.service.pagination.PageableResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class DomainListResult extends PageableResult {
    public List<DomainEntity> domainList;
}
