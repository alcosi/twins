package org.twins.core.dao.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface DomainLocaleRepository extends CrudRepository<DomainLocaleEntity, UUID> {
    List<DomainLocaleEntity> findByDomainIdAndActiveTrueAndI18nLocaleActiveTrue(UUID domainId);

    boolean existsByDomainIdAndLocaleAndActiveTrueAndI18nLocaleActiveTrue(UUID domainId, String i18nLocaleId);
}
