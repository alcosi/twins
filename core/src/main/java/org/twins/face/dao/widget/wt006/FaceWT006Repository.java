package org.twins.face.dao.widget.wt006;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceWT006Repository extends CrudRepository<FaceWT006Entity, UUID>, JpaSpecificationExecutor<FaceWT006Entity> {
    List<FaceWT006Entity> findByFaceId(UUID faceId);
}
