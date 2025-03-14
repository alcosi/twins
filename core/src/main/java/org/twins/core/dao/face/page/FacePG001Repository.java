package org.twins.core.dao.face.page;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FacePG001Repository extends CrudRepository<FacePG001Entity, UUID>, JpaSpecificationExecutor<FacePG001Entity> {
}
