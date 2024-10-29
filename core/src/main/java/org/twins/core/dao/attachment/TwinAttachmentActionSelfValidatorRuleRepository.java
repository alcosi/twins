
package org.twins.core.dao.attachment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAttachmentActionSelfValidatorRuleRepository extends CrudRepository<TwinAttachmentActionSelfValidatorRuleEntity, UUID> {
    List<TwinAttachmentActionSelfValidatorRuleEntity> findByTwinClassId(UUID twinClassId);

    List<TwinAttachmentActionSelfValidatorRuleEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
}
