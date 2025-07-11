package org.twins.face.dao.widget.wt001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceWT001Repository extends CrudRepository<FaceWT001Entity, UUID>, JpaSpecificationExecutor<FaceWT001Entity> {
    List<FaceWT001Entity> findByFaceId(UUID of);
}
