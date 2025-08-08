package org.twins.face.dao.widget.wt002;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT002ButtonRepository extends CrudRepository<FaceWT002ButtonEntity, UUID> {
    List<FaceWT002ButtonEntity> findByFaceWT002Id(UUID id);

    List<FaceWT002ButtonEntity> findByFaceWT002IdIn(Set<UUID> idSet);
}
