package org.twins.core.dao.datalist;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataListOptionSearchPredicateRepository extends CrudRepository<DataListOptionSearchPredicateEntity, UUID>, JpaSpecificationExecutor<DataListOptionSearchPredicateEntity> {
    List<DataListOptionSearchPredicateEntity> findByDataListOptionSearchId(UUID searchId);
}
