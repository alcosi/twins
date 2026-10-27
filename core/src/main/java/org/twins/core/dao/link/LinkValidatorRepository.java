package org.twins.core.dao.link;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface LinkValidatorRepository extends CrudRepository<LinkValidatorEntity, UUID>, JpaSpecificationExecutor<LinkValidatorEntity> {
    List<LinkValidatorEntity> findByLinkIdOrderByOrder(UUID linkId);

    List<LinkValidatorEntity> findByLinkIdIn(Collection<UUID> linkIds);

    // rows: [link_id (UUID), max order (Integer, absent link -> no row)]
    @Query(nativeQuery = true, value = "select link_id, max(\"order\") from link_validator where link_id in (:linkIds) group by link_id")
    List<Object[]> findMaxOrderByLinkIdIn(@Param("linkIds") Collection<UUID> linkIds);

    // rows: [id (UUID), link_id (UUID), order (Integer)] — order-conflict checks without entity hydration
    @Query(nativeQuery = true, value = "select id, link_id, \"order\" from link_validator where link_id in (:linkIds)")
    List<Object[]> findSiblingsOrderByLinkIdIn(@Param("linkIds") Collection<UUID> linkIds);
}
