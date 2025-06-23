package org.twins.face.dao.page.pg002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FacePG002TabRepository extends CrudRepository<FacePG002TabEntity, UUID>, JpaSpecificationExecutor<FacePG002TabEntity> {
    List<FacePG002TabEntity> findByFacePG002IdIn(Set<UUID> idSet);

    List<FacePG002TabEntity> findByFacePG002Id(UUID facePG002Id);
}
