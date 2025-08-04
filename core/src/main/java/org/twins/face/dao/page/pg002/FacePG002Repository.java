package org.twins.face.dao.page.pg002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FacePG002Repository extends CrudRepository<FacePG002Entity, UUID>, JpaSpecificationExecutor<FacePG002Entity> {
    List<FacePG002Entity> findByFaceId(UUID of);
}
