package org.twins.core.dao.link;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LinkRepository extends CrudRepository<LinkEntity, UUID>, JpaSpecificationExecutor<LinkEntity> {
    List<LinkEntity> findBySrcTwinClassIdInOrDstTwinClassIdIn(Set<UUID> srcTwinClassId, Set<UUID> dstTwinClassId);
}
