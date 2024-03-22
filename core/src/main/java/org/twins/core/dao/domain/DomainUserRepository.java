package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.UUID;

@Repository
public interface DomainUserRepository extends CrudRepository<DomainUserEntity, UUID>, JpaSpecificationExecutor<DomainUserEntity> {
    <T> T findByDomainIdAndUserId(UUID domainId, UUID userId, Class<T> type);

    @Modifying
    @Query("UPDATE DomainUserEntity e SET e.i18nLocaleId = :locale WHERE e.domainId = :domainId AND e.userId = :userId")
    void updateLocale(@Param("domainId") UUID domainId, @Param("userId") UUID userId, @Param("locale") Locale locale);
}
