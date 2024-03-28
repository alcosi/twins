package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpaceRoleRepository extends CrudRepository<SpaceRoleEntity, UUID>, JpaSpecificationExecutor<SpaceRoleEntity> {

    @Query("delete from SpaceRoleEntity sr where sr.businessAccountId = :businessAccountId and sr.twinClass.domainId = :domainId")
    void deleteAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

}
