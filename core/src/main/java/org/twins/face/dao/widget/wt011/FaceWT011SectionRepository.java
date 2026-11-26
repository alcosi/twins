package org.twins.face.dao.widget.wt011;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT011SectionRepository extends CrudRepository<FaceWT011SectionEntity, UUID>, JpaSpecificationExecutor<FaceWT011SectionEntity> {
    List<FaceWT011SectionEntity> findByFaceWT011ColumnId(UUID id);

    List<FaceWT011SectionEntity> findByFaceWT011ColumnIdIn(Set<UUID> idSet);
}
