package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRoleRepository extends CrudRepository<SpaceRoleEntity, UUID>, JpaSpecificationExecutor<SpaceRoleEntity> {

    @Query("select sr.id from SpaceRoleEntity sr where sr.businessAccountId = :businessAccountId and sr.twinClass.domainId = :domainId")
    List<UUID> findAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

}
