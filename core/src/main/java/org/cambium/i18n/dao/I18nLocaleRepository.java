package org.cambium.i18n.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface I18nLocaleRepository extends CrudRepository<I18nLocaleEntity, UUID>, JpaSpecificationExecutor<I18nLocaleEntity> {
    @Query(value = "select il from I18nLocaleEntity il where il.locale in (select dl.locale from DomainLocaleEntity dl where dl.domainId = :domainId)")
    List<I18nLocaleEntity> findAllLocaleByDomainId(@Param("domainId") UUID domainId);
}
