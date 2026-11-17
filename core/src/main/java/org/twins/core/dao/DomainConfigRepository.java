package org.twins.core.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository base for {@link DomainConfigEntity} (TWINS-719): domain-filtered by-id reads backing
 * {@code DomainConfigAccessService}'s loadEntityById/loadEntitiesById overrides. Ids from OTHER
 * domains are invisible (read as "not found"); system rows (domain_id IS NULL) stay visible —
 * same semantics as {@code TwinsEntityAccessService.isDomainAccessDenied}.
 */
@NoRepositoryBean
public interface DomainConfigRepository<T extends DomainConfigEntity> extends CrudRepository<T, UUID> {

    @Query("select e from #{#entityName} e where e.id = :id and (e.domainId = :domainId or e.domainId is null)")
    Optional<T> findByDomainIdAndId(@Param("domainId") UUID domainId, @Param("id") UUID id);

    @Query("select e from #{#entityName} e where e.id in :ids and (e.domainId = :domainId or e.domainId is null)")
    List<T> findAllByDomainIdAndIdIn(@Param("domainId") UUID domainId, @Param("ids") Collection<UUID> ids);
}
