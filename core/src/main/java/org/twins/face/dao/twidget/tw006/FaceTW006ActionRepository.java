package org.twins.face.dao.twidget.tw006;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FaceTW006ActionRepository extends CrudRepository<FaceTW006ActionEntity, UUID>, JpaSpecificationExecutor<FaceTW006ActionEntity> {
    FaceTW006ActionEntity findByFaceTW006Id(UUID id);
}
