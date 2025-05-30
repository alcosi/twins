package org.twins.face.dao.page.pg002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FacePG002TabRepository extends CrudRepository<FacePG002TabEntity, UUID>, JpaSpecificationExecutor<FacePG002TabEntity> {
    Collection<FacePG002TabEntity> findByFaceIdIn(Set<UUID> idSet);
}
