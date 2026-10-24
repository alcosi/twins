package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.twins.core.enums.domain.DomainVersionStatus;

import java.util.Optional;
import java.util.UUID;

public interface DomainVersionRepository extends JpaRepository<DomainVersionEntity, UUID>, JpaSpecificationExecutor<DomainVersionEntity> {

    Optional<DomainVersionEntity> findByDomainIdAndStatus(UUID domainId, DomainVersionStatus status);
}
