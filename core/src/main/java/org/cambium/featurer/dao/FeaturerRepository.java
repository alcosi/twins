package org.cambium.featurer.dao;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturerRepository extends CrudRepository<FeaturerEntity, Integer>, JpaSpecificationExecutor<FeaturerEntity> {
    @Cacheable(value = "FeaturerRepository.getById")
    FeaturerEntity getById(int id);
    List<FeaturerEntity> findByFeaturerTypeId(int type);
}
