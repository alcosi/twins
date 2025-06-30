package org.twins.face.dao.twidget.tw001;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceTW001Repository extends CrudRepository<FaceTW001Entity, UUID>, JpaSpecificationExecutor<FaceTW001Entity> {
    List<FaceTW001Entity> findByFaceId(UUID faceId);
}
