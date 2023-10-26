package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinLinkRepository extends CrudRepository<TwinLinkEntity, UUID>, JpaSpecificationExecutor<TwinLinkEntity> {
    List<TwinLinkEntity> findBySrcTwinIdOrDstTwinId(UUID srcTwinId, UUID dstTwinId);
}
