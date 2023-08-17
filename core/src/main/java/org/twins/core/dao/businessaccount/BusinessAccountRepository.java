package org.twins.core.dao.businessaccount;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BusinessAccountRepository extends CrudRepository<BusinessAccountEntity, UUID>, JpaSpecificationExecutor<BusinessAccountEntity> {
}
