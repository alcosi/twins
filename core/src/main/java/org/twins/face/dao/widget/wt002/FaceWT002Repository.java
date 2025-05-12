package org.twins.face.dao.widget.wt002;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FaceWT002Repository extends CrudRepository<FaceWT002Entity, UUID>, JpaSpecificationExecutor<FaceWT002Entity> {
}
