package org.twins.face.dao.widget.wt011;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface FaceWT011Repository extends CrudRepository<FaceWT011Entity, UUID>, JpaSpecificationExecutor<FaceWT011Entity> {
    List<FaceWT011Entity> findByFaceId(UUID faceId);
}
