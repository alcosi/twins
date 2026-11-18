package org.twins.face.dao.widget.wt010;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceWT010Repository extends CrudRepository<FaceWT010Entity, UUID>, JpaSpecificationExecutor<FaceWT010Entity> {
    List<FaceWT010Entity> findByFaceId(UUID uuid);
}
