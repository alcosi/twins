package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryNotificationContextRecipientRepository extends CrudRepository<HistoryNotificationContextRecipientEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationContextRecipientEntity> {

}

