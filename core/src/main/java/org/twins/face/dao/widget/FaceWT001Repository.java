package org.twins.face.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaceWT001Repository extends CrudRepository<FaceWT001Entity, UUID>, JpaSpecificationExecutor<FaceWT001Entity> {
}
