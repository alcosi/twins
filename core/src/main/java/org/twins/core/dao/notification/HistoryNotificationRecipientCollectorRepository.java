package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface HistoryNotificationRecipientCollectorRepository extends CrudRepository<HistoryNotificationRecipientCollectorEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationRecipientCollectorEntity> {
    Set<HistoryNotificationRecipientCollectorEntity> findAllByHistoryNotificationRecipientIdAndExclude(UUID recipientId, boolean exclude);
}

