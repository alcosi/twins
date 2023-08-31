package org.twins.core.dao.widget;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.AccessRule;

import java.util.List;
import java.util.UUID;

@Repository
public interface WidgetAccessRepository extends CrudRepository<WidgetAccessEntity, UUID>, JpaSpecificationExecutor<WidgetAccessEntity> {
    List<WidgetAccessEntity> findByTwinClassIdAndAccessRuleEquals(UUID twinClassId, AccessRule accessRule);
    List<WidgetAccessEntity> findByTwinClassIdIsNullAndAccessRule(AccessRule accessRule);
    List<WidgetAccessEntity> findByTwinClassIdOrTwinClassIdIsNull(UUID twinClassId);
}
