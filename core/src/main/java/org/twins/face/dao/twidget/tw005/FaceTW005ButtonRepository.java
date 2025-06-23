package org.twins.face.dao.twidget.tw005;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceTW005ButtonRepository extends CrudRepository<FaceTW005ButtonEntity, UUID>, JpaSpecificationExecutor<FaceTW005ButtonEntity> {
    Collection<FaceTW005ButtonEntity> findByFaceIdIn(Set<UUID> idSet);

    List<FaceTW005ButtonEntity> findByFaceTW005Id(UUID faceTW005Id);
}
