package org.cambium.featurer.dao;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeaturerParamRepository extends CrudRepository<FeaturerParamEntity, UUID>, JpaSpecificationExecutor<FeaturerParamEntity> {
    List<FeaturerParamEntity> findByFeaturer(FeaturerEntity feature);

    FeaturerParamEntity findByFeaturerIdAndKey(Integer featurerId, String key);

    @Transactional
    void deleteAllByFeaturerIdIn(List<Integer> list);
}
