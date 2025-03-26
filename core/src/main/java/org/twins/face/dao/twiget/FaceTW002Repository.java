package org.twins.face.dao.twiget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaceTW002Repository extends CrudRepository<FaceTW002Entity, UUID>, JpaSpecificationExecutor<FaceTW002Entity> {
}
