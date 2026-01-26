package org.twins.face.dao.bc;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FaceBC001ItemRepository extends CrudRepository<FaceBC001ItemEntity, UUID>, JpaSpecificationExecutor<FaceBC001ItemEntity> {
    Collection<FaceBC001ItemEntity> findAllByFaceBC001IdIn(Set<UUID> faceBC001Ids);
}
