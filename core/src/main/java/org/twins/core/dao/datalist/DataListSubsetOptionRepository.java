package org.twins.core.dao.datalist;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataListSubsetOptionRepository extends CrudRepository<DataListSubsetOptionEntity, DataListOptionSubsetId> {
    List<DataListSubsetOptionEntity> findByDataListSubsetIdIn(Collection<UUID> dataListSubsetIds);

    void deleteAllByDataListSubsetIdIn(Collection<UUID> dataListSubsetIds);
}
