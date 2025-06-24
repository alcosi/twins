package org.twins.face.dao.page.pg002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FacePG002WidgetRepository extends CrudRepository<FacePG002WidgetEntity, UUID>, JpaSpecificationExecutor<FacePG002WidgetEntity> {
    List<FacePG002WidgetEntity> findByFacePagePG002TabIdInAndActiveTrue(Set<UUID> idSet);
}
