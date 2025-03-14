package org.twins.core.dao.face.page;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FacePG001WidgetRepository extends CrudRepository<FacePG001WidgetEntity, UUID>, JpaSpecificationExecutor<FacePG001WidgetEntity> {
    Collection<FacePG001WidgetEntity> findByFaceIdIn(Set<UUID> idSet);
}
