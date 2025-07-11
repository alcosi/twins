package org.twins.face.dao.twidget.tw004;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceTW004Repository extends CrudRepository<FaceTW004Entity, UUID>, JpaSpecificationExecutor<FaceTW004Entity> {
    List<FaceTW004Entity> findByFaceId(UUID faceId);
}
