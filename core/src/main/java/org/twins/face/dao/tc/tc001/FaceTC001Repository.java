package org.twins.face.dao.tc.tc001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaceTC001Repository extends CrudRepository<FaceTC001Entity, UUID>, JpaSpecificationExecutor<FaceTC001Entity> {
}
