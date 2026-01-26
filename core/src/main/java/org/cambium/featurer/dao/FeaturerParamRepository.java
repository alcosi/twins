package org.cambium.featurer.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FeaturerParamRepository extends CrudRepository<FeaturerParamEntity, UUID>, JpaSpecificationExecutor<FeaturerParamEntity> {
    List<FeaturerParamEntity> findByFeaturer(FeaturerEntity feature);

    List<FeaturerParamEntity> findByFeaturerIdIn(Set<Integer> featurerIds);

    FeaturerParamEntity findByFeaturerIdAndKey(Integer featurerId, String key);

    @Transactional
    void deleteAllByFeaturerIdIn(Collection<Integer> list);
}
