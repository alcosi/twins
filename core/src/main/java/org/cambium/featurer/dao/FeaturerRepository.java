package org.cambium.featurer.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturerRepository extends CrudRepository<FeaturerEntity, Integer>, JpaSpecificationExecutor<FeaturerEntity> {
    List<FeaturerEntity> findByFeaturerTypeId(int type);
}
