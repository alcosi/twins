package org.twins.core.dao.history;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.history.HistoryType;

import java.util.UUID;

@Repository
public interface HistoryTypeDomainTemplateRepository extends CrudRepository<HistoryTypeDomainTemplateEntity, UUID>, JpaSpecificationExecutor<HistoryTypeDomainTemplateEntity> {

    String CACHE_SNAPSHOT_MESSAGE_TEMPLATE_IN_DOMAIN = "HistoryTypeDomainTemplateRepository.findSnapshotMessageTemplate";
    @Cacheable(value = CACHE_SNAPSHOT_MESSAGE_TEMPLATE_IN_DOMAIN, key = "#historyType.id + '' + #domainId")
    @Query(value = "select template.snapshotMessageTemplate from HistoryTypeDomainTemplateEntity template where template.historyType = :historyType " +
            "and template.domainId = :domainId")
    String findSnapshotMessageTemplate(@Param("historyType") HistoryType historyType, @Param("domainId") UUID domainId);


    String CACHE_SNAPSHOT_MESSAGE_TEMPLATE = "HistoryTypeDomainTemplateRepository.findSnapshotMessageTemplate";
    @Cacheable(value = CACHE_SNAPSHOT_MESSAGE_TEMPLATE, key = "#historyType")
    @Query(value = "select type.snapshotMessageTemplate from HistoryTypeEntity type where type.id = :historyType")
    String findSnapshotMessageTemplate(@Param("historyType") String historyType);
}
