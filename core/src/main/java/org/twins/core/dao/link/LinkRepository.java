package org.twins.core.dao.link;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LinkRepository extends CrudRepository<LinkEntity, UUID>, JpaSpecificationExecutor<LinkEntity> {
    String CACHE_LINK_BY_TWIN_CLASS_ID_IN_INHERITABLE = "LinkRepository.findByTwinClassIdInInheritable";
    @Cacheable(value = CACHE_LINK_BY_TWIN_CLASS_ID_IN_INHERITABLE, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#mainTwinClassIdList, #extendsTwinClassIdList)")
    @Query(value = "from LinkEntity where srcTwinClassId in :mainTwinClassIdList or dstTwinClassId in :mainTwinClassIdList " +
            "or (srcTwinClassId in :extendsTwinClassIdList and srcTwinClassInheritable) or (dstTwinClassId in :extendsTwinClassIdList and dstTwinClassInheritable)")
    List<LinkEntity> findByTwinClassIdInInheritable(@Param("mainTwinClassIdList") Set<UUID> mainTwinClassIdList, @Param("extendsTwinClassIdList") Set<UUID> extendsTwinClassIdList);
    List<LinkEntity> findBySrcTwinClassIdInOrDstTwinClassIdIn(Set<UUID> srcTwinClassId, Set<UUID> dstTwinClassId);
    List<LinkEntity> findBySrcTwinClassIdInAndDstTwinClassIdIn(Set<UUID> srcTwinClassId, Set<UUID> dstTwinClassId);
    LinkEntity findBySrcTwinClassIdAndDstTwinClassId(UUID srcTwinClassId, UUID dstTwinClassId);

    Collection<LinkEntity> findAllByIdIn(Collection<UUID> newLinkIds);
}
