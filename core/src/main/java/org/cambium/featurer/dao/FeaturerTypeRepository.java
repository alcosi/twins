package org.cambium.featurer.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeaturerTypeRepository extends CrudRepository<FeaturerTypeEntity, Integer>, JpaSpecificationExecutor<FeaturerTypeEntity> {

}
