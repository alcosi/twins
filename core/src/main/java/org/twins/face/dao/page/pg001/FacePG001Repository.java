package org.twins.face.dao.page.pg001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FacePG001Repository extends CrudRepository<FacePG001Entity, UUID>, JpaSpecificationExecutor<FacePG001Entity> {
    List<FacePG001Entity> findByFaceId(UUID faceId);
}
