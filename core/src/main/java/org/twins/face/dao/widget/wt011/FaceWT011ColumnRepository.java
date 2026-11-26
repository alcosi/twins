package org.twins.face.dao.widget.wt011;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT011ColumnRepository extends CrudRepository<FaceWT011ColumnEntity, UUID>, JpaSpecificationExecutor<FaceWT011ColumnEntity> {
    List<FaceWT011ColumnEntity> findByFaceWT011Id(UUID id);

    List<FaceWT011ColumnEntity> findByFaceWT011IdIn(Set<UUID> idSet);
}
