package org.twins.core.dao.notification;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface HistoryNotificationRecipientCollectorRepository extends CrudRepository<HistoryNotificationRecipientCollectorEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationRecipientCollectorEntity> {
    String CACHE_COLLECTORS_BY_RECIPIENT_ID_IN = "HistoryNotificationRecipientCollectorRepository.findAllByHistoryNotificationRecipientIdIn";

    Set<HistoryNotificationRecipientCollectorEntity> findAllByHistoryNotificationRecipientId(UUID recipientId);

    @Cacheable(value = CACHE_COLLECTORS_BY_RECIPIENT_ID_IN, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#recipientIds)")
    Set<HistoryNotificationRecipientCollectorEntity> findAllByHistoryNotificationRecipientIdIn(Set<UUID> recipientIds);
}
