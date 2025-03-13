package org.twins.core.dao.face.navbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaceNB001Repository extends CrudRepository<FaceNB001Entity, UUID>, JpaSpecificationExecutor<FaceNB001Entity> {
}
