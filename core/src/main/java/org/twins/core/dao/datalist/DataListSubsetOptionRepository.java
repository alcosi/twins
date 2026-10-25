package org.twins.core.dao.datalist;

import org.springframework.stereotype.Repository;
import org.twins.core.dao.DomainConfigRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataListSubsetOptionRepository extends DomainConfigRepository<DataListSubsetOptionEntity> {
    List<DataListSubsetOptionEntity> findByDataListSubsetIdIn(Collection<UUID> dataListSubsetIds);
}
