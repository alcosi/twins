package org.twins.face.dao.widget.wt011;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT011FieldRepository extends CrudRepository<FaceWT011FieldEntity, UUID>, JpaSpecificationExecutor<FaceWT011FieldEntity> {
    List<FaceWT011FieldEntity> findByFaceWT011RowId(UUID id);

    List<FaceWT011FieldEntity> findByFaceWT011RowIdIn(Set<UUID> idSet);
}
