package org.twins.face.dao.widget.wt007;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT007ButtonRepository extends CrudRepository<FaceWT007ButtonEntity, UUID>, JpaSpecificationExecutor<FaceWT007ButtonEntity> {
    List<FaceWT007ButtonEntity> findByFaceWT007IdIn(Set<UUID> idSet);

    List<FaceWT007ButtonEntity> findByFaceWT007Id(UUID faceWT007Id);
}
