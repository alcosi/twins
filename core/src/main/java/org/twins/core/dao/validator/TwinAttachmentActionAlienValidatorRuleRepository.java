
package org.twins.core.dao.validator;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAttachmentActionAlienValidatorRuleRepository extends CrudRepository<TwinAttachmentActionAlienValidatorRuleEntity, UUID> {
    List<TwinAttachmentActionAlienValidatorRuleEntity> findByTwinClassIdOrderByOrder(UUID twinClassId);

    List<TwinAttachmentActionAlienValidatorRuleEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
}
