package org.twins.core.dao.face;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaceTwidgetRepository extends CrudRepository<FaceTwidgetEntity, UUID>, JpaSpecificationExecutor<FaceTwidgetEntity> {
    FaceTwidgetEntity findByFaceId(UUID id);
}
