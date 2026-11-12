package org.twins.core.dao.datalist;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.DomainConfigRepository;

import java.util.UUID;

@Repository
public interface DataListSubsetRepository extends DomainConfigRepository<DataListSubsetEntity>, JpaSpecificationExecutor<DataListSubsetEntity> {
    boolean existsByDataListIdAndKey(UUID dataListId, String key);

    boolean existsByDataListIdAndKeyAndIdNot(UUID dataListId, String key, UUID id);
}
