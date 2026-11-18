package org.twins.face.dao.widget.wt008;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface FaceWT008Repository extends CrudRepository<FaceWT008Entity, UUID>, JpaSpecificationExecutor<FaceWT008Entity> {
    List<FaceWT008Entity> findByFaceId(UUID of);
}
