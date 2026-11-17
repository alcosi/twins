package org.twins.core.dao.datalist;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.DomainConfigRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataListRepository extends DomainConfigRepository<DataListEntity>, JpaSpecificationExecutor<DataListEntity> {
    List<DataListEntity> findByDomainId(UUID domainId);

    Optional<DataListEntity> findByDomainIdAndKey(UUID domainId, String key);

    boolean existsByDomainIdAndKey(UUID domainId, String key);

    boolean existsByDomainIdAndKeyAndIdNot(UUID domainId, String key, UUID id);

    boolean existsByIdAndDomainIdOrIdAndDomainIdIsNull(UUID dataListId, UUID domainId, UUID dataListIdSame);

    @Modifying
    @Query("UPDATE DataListEntity d SET d.defaultDataListOptionId = ?2 WHERE d.id = ?1")
    void updateDefaultOptionId(UUID dataListId, UUID optionId);
}
