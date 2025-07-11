package org.twins.face.dao.twidget.tw006;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceTW006Repository extends CrudRepository<FaceTW006Entity, UUID>, JpaSpecificationExecutor<FaceTW006Entity> {
    List<FaceTW006Entity> findByFaceId(UUID faceId);
}
