package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionSchemaRepository extends CrudRepository<PermissionSchemaEntity, UUID>, JpaSpecificationExecutor<PermissionSchemaEntity> {

    @Query("select ps.id from PermissionSchemaEntity ps where ps.businessAccountId = :businessAccountId and ps.domainId = :domainId")
    List<UUID> findAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

}
