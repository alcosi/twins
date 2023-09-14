package org.twins.core.dao.datalist;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataListOptionRepository extends CrudRepository<DataListOptionEntity, UUID>, JpaSpecificationExecutor<DataListOptionEntity> {
    List<DataListOptionEntity> findByDataListId(UUID dataListId);

    int countByDataListId(UUID dataListId);
}
