package org.twins.face.dao.widget.wt011;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT011RowRepository extends CrudRepository<FaceWT011RowEntity, UUID>, JpaSpecificationExecutor<FaceWT011RowEntity> {
    List<FaceWT011RowEntity> findByFaceWT011SectionId(UUID id);

    List<FaceWT011RowEntity> findByFaceWT011SectionIdIn(Set<UUID> idSet);
}
