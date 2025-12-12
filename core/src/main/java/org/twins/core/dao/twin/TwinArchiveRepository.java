package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.UUID;

public interface TwinArchiveRepository extends JpaRepository<TwinArchiveEntity, UUID> {
    long countAllByCreatedAtBefore(Timestamp createdAfter);
    void deleteAllByCreatedAtBefore(Timestamp createdAfter);
}
