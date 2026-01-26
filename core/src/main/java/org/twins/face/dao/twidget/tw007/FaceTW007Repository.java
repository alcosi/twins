package org.twins.face.dao.twidget.tw007;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaceTW007Repository extends CrudRepository<FaceTW007Entity, UUID>, JpaSpecificationExecutor<FaceTW007Entity> {
    List<FaceTW007Entity> findByFaceId(UUID uuid);
}
