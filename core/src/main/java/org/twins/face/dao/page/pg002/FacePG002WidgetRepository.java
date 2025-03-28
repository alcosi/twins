package org.twins.face.dao.page.pg002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FacePG002WidgetRepository extends CrudRepository<FacePG002WidgetEntity, UUID>, JpaSpecificationExecutor<FacePG002WidgetEntity> {
    Collection<FacePG002WidgetEntity> findByFacePagePG002TabIdIn(Set<UUID> idSet);
}
