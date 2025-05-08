package org.twins.face.dao.twidget.tw005;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface FaceTW005ButtonRepository extends CrudRepository<FaceTW005ButtonEntity, UUID>, JpaSpecificationExecutor<FaceWT001Entity> {
    Collection<FaceTW005ButtonEntity> findByFaceIdIn(Set<UUID> idSet);
}
