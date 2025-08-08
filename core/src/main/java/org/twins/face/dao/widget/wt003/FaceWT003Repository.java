package org.twins.face.dao.widget.wt003;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface FaceWT003Repository extends CrudRepository<FaceWT003Entity, UUID>, JpaSpecificationExecutor<FaceWT003Entity> {
    List<FaceWT003Entity> findByFaceId(UUID of);
}
