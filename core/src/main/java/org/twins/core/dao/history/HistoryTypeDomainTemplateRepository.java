package org.twins.core.dao.history;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryTypeDomainTemplateRepository extends CrudRepository<HistoryTypeDomainTemplateEntity, UUID>, JpaSpecificationExecutor<HistoryTypeDomainTemplateEntity> {

    @Query(value = "select template.snapshotMessageTemplate from HistoryTypeDomainTemplateEntity template where template.historyType = :historyTypeId " +
            "and template.domainId = :domainId")
    String findSnapshotMessageTemplate(@Param("historyTypeId") HistoryType historyTypeId, @Param("domainId") UUID domainId);

    @Query(value = "select type.snapshotMessageTemplate from HistoryTypeEntity type where type.id = :historyTypeId")
    String findSnapshotMessageTemplate(@Param("historyTypeId") String historyTypeId);
}
