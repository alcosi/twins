package org.twins.core.dao.notification.email;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailSenderRepository extends CrudRepository<EmailSenderEntity, UUID>, JpaSpecificationExecutor<EmailSenderEntity> {
    <T> T findById(UUID id, Class<T> type);
}
