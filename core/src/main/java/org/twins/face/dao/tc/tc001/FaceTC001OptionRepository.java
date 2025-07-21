package org.twins.face.dao.tc.tc001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FaceTC001OptionRepository extends CrudRepository<FaceTC001OptionEntity, UUID>, JpaSpecificationExecutor<FaceTC001OptionEntity> {
    List<FaceTC001OptionEntity> findByFaceTC001Id(UUID id);

    List<FaceTC001OptionEntity> findByFaceTC001IdIn(Set<UUID> idSet);
}
