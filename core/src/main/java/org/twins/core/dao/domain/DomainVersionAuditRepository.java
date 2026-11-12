package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DomainVersionAuditRepository extends JpaRepository<DomainVersionAuditEntity, UUID>, JpaSpecificationExecutor<DomainVersionAuditEntity> {
}
