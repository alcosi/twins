package org.twins.core.dao.domain;

import org.cambium.common.util.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
public interface DomainUserRepository extends CrudRepository<DomainUserEntity, UUID>, JpaSpecificationExecutor<DomainUserEntity> {
    <T> T findByDomainIdAndUserId(UUID domainId, UUID userId, Class<T> type);

    @Query(value = "select du.domainSpecOnly from DomainUserEntity du where du.userId = :userId")
    Page<DomainEntity> findAllDomainByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = "select du.domainSpecOnly from DomainUserEntity du where du.userId = :userId and du.domainSpecOnly.domainStatusId = 'ACTIVE'")
    Page<DomainEntity> findAllActiveDomainByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = "select du, du.domainSpecOnly, du.userSpecOnly from DomainUserEntity du where du.userId = :userId and du.domainId = :domainId")
    List<Object[]> _findByDomainIdAndUserId(@Param("domainId") UUID domainId, @Param("userId") UUID userId);

    default DomainUserEntity findByDomainIdAndUserId(UUID domainId, UUID userId) {
        var results = _findByDomainIdAndUserId(domainId, userId);
        if (CollectionUtils.isEmpty(results))
            return null;
        var row = results.getFirst();
        var du = (DomainUserEntity) row[0];
        du
                .setDomain((DomainEntity) row[1])
                .setUser((UserEntity) row[2]);
        return du;
    }

    boolean existsByDomainIdAndUserId(UUID uuid, UUID userId);

    @Modifying
    @Query("UPDATE DomainUserEntity e SET e.i18nLocaleId = :locale WHERE e.domainId = :domainId AND e.userId = :userId")
    void updateLocale(@Param("domainId") UUID domainId, @Param("userId") UUID userId, @Param("locale") Locale locale);

    @Modifying
    @Query("UPDATE DomainUserEntity e SET e.lastActivityAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void updateLastActivityAt(@Param("id") UUID id);
}
