package org.twins.face.dao.page.pg001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FacePG001WidgetRepository extends CrudRepository<FacePG001WidgetEntity, UUID>, JpaSpecificationExecutor<FacePG001WidgetEntity> {
    List<FacePG001WidgetEntity> findByFacePG001Id(UUID facePG001Id);

    Collection<FacePG001WidgetEntity> findByFacePG001IdIn(Set<UUID> idSet);
}
