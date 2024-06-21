package org.twins.core.dao.search;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SearchAliasRepository extends CrudRepository<SearchAliasEntity, UUID>, JpaSpecificationExecutor<SearchAliasEntity> {
    SearchAliasEntity findByDomainIdAndAlias(UUID domainId, String alias);
}
