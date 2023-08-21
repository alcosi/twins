package org.twins.core.dao.error;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ErrorRepository extends CrudRepository<ErrorEntity, String>, JpaSpecificationExecutor<ErrorEntity> {
    ErrorEntity findById(UUID id);
    ErrorEntity findByErrorCodeLocal(int errorCodeLocal);
    ErrorEntity findByErrorCodeLocalAndErrorCodeExternal(int errorCodeLocal, String errorCodeExernal);
}
