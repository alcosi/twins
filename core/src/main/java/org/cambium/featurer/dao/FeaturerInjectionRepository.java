package org.cambium.featurer.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeaturerInjectionRepository extends CrudRepository<FeaturerInjectionEntity, UUID>, JpaSpecificationExecutor<FeaturerInjectionEntity> {
}
