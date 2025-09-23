package org.twins.core.dao.search;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinSearchSortRepository extends CrudRepository<TwinSearchSortEntity, UUID>, JpaSpecificationExecutor<TwinSearchSortEntity> {
}
