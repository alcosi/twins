package org.twins.face.dao.tc.tc002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceTC002Repository extends CrudRepository<FaceTC002Entity, UUID>, JpaSpecificationExecutor<FaceTC002Entity> {
    List<FaceTC002Entity> findByFaceId(UUID of);
}
