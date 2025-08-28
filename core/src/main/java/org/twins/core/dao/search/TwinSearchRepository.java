package org.twins.core.dao.search;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinSearchRepository extends CrudRepository<TwinSearchEntity, UUID>, JpaSpecificationExecutor<TwinSearchEntity> {
    List<TwinSearchEntity> findByTwinSearchAliasId(UUID searchAliasId);
}
