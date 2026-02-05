package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainVersionGhostRepository extends CrudRepository<DomainVersionGhostEntity, DomainVersionGhostId>,
        JpaSpecificationExecutor<DomainVersionGhostEntity> {
}
