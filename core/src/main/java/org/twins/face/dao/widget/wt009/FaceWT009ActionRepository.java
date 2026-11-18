package org.twins.face.dao.widget.wt009;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT009ActionRepository extends CrudRepository<FaceWT009ActionEntity, UUID>, JpaSpecificationExecutor<FaceWT009ActionEntity> {
    List<FaceWT009ActionEntity> findByFaceWT009IdIn(Set<UUID> id);
}
