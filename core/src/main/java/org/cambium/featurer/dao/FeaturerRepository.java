package org.cambium.featurer.dao;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeaturerRepository extends CrudRepository<FeaturerEntity, Integer>, JpaSpecificationExecutor<FeaturerEntity> {
    @Cacheable(value = "FeaturerRepository.getById")
    Optional<FeaturerEntity> findById(Integer id);
    List<FeaturerEntity> findByIdIn(Collection<Integer> ids);
    List<FeaturerEntity> findByFeaturerTypeId(int type);
}
