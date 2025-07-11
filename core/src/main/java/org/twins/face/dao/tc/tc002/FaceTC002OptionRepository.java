package org.twins.face.dao.tc.tc002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FaceTC002OptionRepository extends CrudRepository<FaceTC002OptionEntity, UUID>, JpaSpecificationExecutor<FaceTC002OptionEntity> {
    List<FaceTC002OptionEntity> findByFaceTC002Id(UUID id);

    List<FaceTC002OptionEntity> findByFaceTC002IdIn(Set<UUID> idSet);
}
