package org.twins.face.dao.twidget.tw005;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceTW005Repository extends CrudRepository<FaceTW005Entity, UUID>, JpaSpecificationExecutor<FaceTW005Entity> {
    List<FaceTW005Entity> findByFaceId(UUID of);
}
