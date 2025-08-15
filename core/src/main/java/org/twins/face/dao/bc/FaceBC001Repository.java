package org.twins.face.dao.bc;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceBC001Repository extends CrudRepository<FaceBC001Entity, UUID>, JpaSpecificationExecutor<FaceBC001Entity> {
    List<FaceBC001Entity> findByFaceId(UUID faceId);
}
