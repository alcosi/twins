package org.twins.core.dao.datalist;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DataListOptionSearchRepository extends CrudRepository<DataListOptionSearchEntity, UUID>, JpaSpecificationExecutor<DataListOptionSearchEntity> {
}
