package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TwinflowTransitionAliasRepository extends CrudRepository<TwinflowTransitionAliasEntity, UUID> {
    @Query("select tta.id from TwinflowTransitionAliasEntity tta where tta.domainId = :domainId and tta.alias = :aliasId")
    UUID findByDomainIdAndAlias(@Param("domainId") UUID domainId, @Param("alias") String aliasId);
}
