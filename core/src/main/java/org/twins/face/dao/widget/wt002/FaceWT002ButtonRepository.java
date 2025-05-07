package org.twins.face.dao.widget.wt002;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface FaceWT002ButtonRepository extends CrudRepository<FaceWT002ButtonEntity, UUID> {
    Collection<FaceWT002ButtonEntity> findByFaceIdIn(Set<UUID> idSet);
}
