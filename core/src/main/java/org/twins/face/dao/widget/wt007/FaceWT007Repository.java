package org.twins.face.dao.widget.wt007;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceWT007Repository extends CrudRepository<FaceWT007Entity, UUID>, JpaSpecificationExecutor<FaceWT007Entity> {
    List<FaceWT007Entity> findByFaceId(UUID of);
}
