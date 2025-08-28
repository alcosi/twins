package org.twins.face.dao.twidget.tw006;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceTW006ActionRepository extends CrudRepository<FaceTW006ActionEntity, UUID>, JpaSpecificationExecutor<FaceTW006ActionEntity> {
    List<FaceTW006ActionEntity> findByFaceTW006IdIn(Set<UUID> id);
}
