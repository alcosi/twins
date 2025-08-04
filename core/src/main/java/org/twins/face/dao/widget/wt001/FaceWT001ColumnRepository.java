package org.twins.face.dao.widget.wt001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FaceWT001ColumnRepository extends CrudRepository<FaceWT001ColumnEntity, UUID>, JpaSpecificationExecutor<FaceWT001Entity> {
    List<FaceWT001ColumnEntity> findByFaceWT001Id(UUID id);

    List<FaceWT001ColumnEntity> findByFaceWT001IdIn(Set<UUID> idSet);
}
