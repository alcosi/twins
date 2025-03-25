package org.twins.face.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaceWT003Repository extends CrudRepository<FaceWT003Entity, UUID>, JpaSpecificationExecutor<FaceWT003Entity> {
}
