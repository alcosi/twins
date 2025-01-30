package org.twins.core.dao.datalist;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataListRepository extends CrudRepository<DataListEntity, UUID>, JpaSpecificationExecutor<DataListEntity> {
    List<DataListEntity> findByDomainId(UUID domainId);

    DataListEntity findByDomainIdAndId(UUID domainId, UUID id);

    Optional<DataListEntity> findByDomainIdAndKey(UUID domainId, String key);

    boolean existsByDomainIdAndKey(UUID domainId, String key);

    boolean existsByDomainIdAndKeyAndIdNot(UUID domainId, String key, UUID id);

    List<DataListEntity> findByDomainIdAndIdIn(UUID domainId, Collection<UUID> ids);

    boolean existsByIdAndDomainIdOrIdAndDomainIdIsNull(UUID dataListId, UUID domainId, UUID dataListIdSame);
}
