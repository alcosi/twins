package org.twins.face.dao.widget.wt005;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceWT005Repository extends CrudRepository<FaceWT005Entity, UUID>, JpaSpecificationExecutor<FaceWT005Entity> {
    List<FaceWT005Entity> findByFaceId(UUID faceId);
}
