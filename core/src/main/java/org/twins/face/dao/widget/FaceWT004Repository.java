package org.twins.face.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaceWT004Repository extends CrudRepository<FaceWT004Entity, UUID>, JpaSpecificationExecutor<FaceWT004Entity> {
}
