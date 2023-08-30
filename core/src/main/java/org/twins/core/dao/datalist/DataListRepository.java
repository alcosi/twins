package org.twins.core.dao.datalist;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataListRepository extends CrudRepository<DataListEntity, UUID>, JpaSpecificationExecutor<DataListEntity> {
    List<DataListEntity> findByDomainId(UUID domainId);
    List<DataListEntity> findByDomainIdAndIdIn(UUID domainId, List<UUID> ids);
}
