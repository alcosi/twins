package org.twins.face.dao.widget.wt009;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceWT009Repository extends CrudRepository<FaceWT009Entity, UUID>, JpaSpecificationExecutor<FaceWT009Entity> {
    List<FaceWT009Entity> findByFaceId(UUID faceId);
}
