package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFieldSimpleRepository extends CrudRepository<TwinFieldSimpleEntity, UUID>, JpaSpecificationExecutor<TwinFieldSimpleEntity> {

    List<TwinFieldSimpleEntity> findByTwinIdInAndTwinClassFieldIdIn(Collection<UUID> twinIdSet, Collection<UUID> twinClassFieldIds);
    boolean existsByTwinClassFieldId(UUID twinClassFieldId);
    boolean existsByTwinClassFieldIdAndValue(UUID twinClassFieldId, String value);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(child.headTwinId, cast(count(child) as string))
        from TwinEntity child 
        where child.headTwinId in :headTwinIdList and child.twinStatusId in :childrenTwinStatusIdList
        group by child.headTwinId
        """)
    List<TwinFieldCalcProjection> countChildrenTwinsWithStatusIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and not child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusNotIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(child.headTwinId, cast(count(child) as string))
        from TwinEntity child 
        where child.headTwinId in :headTwinIdList and not child.twinStatusId in :childrenTwinStatusIdList
        group by child.headTwinId
        """)
    List<TwinFieldCalcProjection> countChildrenTwinsWithStatusNotIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);


    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and child.twinClassId in :twinClassIdList")
    long countChildrenTwinsOfTwinClassIdIn(@Param("headTwinId") UUID headTwinId, @Param("twinClassIdList") Collection<UUID> twinClassIds);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(child.headTwinId, cast(count(child) as string))
        from TwinEntity child 
        where child.headTwinId in :headTwinIdList and child.twinClassId in :twinClassIdList
        group by child.headTwinId
        """)
    List<TwinFieldCalcProjection> countChildrenTwinsOfTwinClassIdIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("twinClassIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
            select coalesce(sum(cast(field.value as double)), 0)
            from TwinFieldSimpleEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId=:headTwinId and field.twinClassFieldId = :twinClassFieldId and twin.twinStatusId in :childrenTwinStatusIdList
             """)
    double sumChildrenTwinFieldValuesWithStatusIn(
            @Param("headTwinId") UUID headTwinId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(twin.headTwinId, cast(coalesce(sum(cast(field.value as double)), 0) as string))
        from TwinFieldSimpleEntity field inner join TwinEntity twin on field.twinId = twin.id
        where twin.headTwinId in :headTwinIdList and field.twinClassFieldId = :twinClassFieldId and twin.twinStatusId in :childrenTwinStatusIdList
        group by twin.headTwinId
        """)
    List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesWithStatusIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("twinClassFieldId") UUID twinClassFieldId,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
            select coalesce(sum(cast(field.value as double)), 0)
            from TwinFieldSimpleEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId=:headTwinId and field.twinClassFieldId = :twinClassFieldId and not twin.twinStatusId in :childrenTwinStatusIdList
             """)
    double sumChildrenTwinFieldValuesWithStatusNotIn(
            @Param("headTwinId") UUID headTwinId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(twin.headTwinId, cast(coalesce(sum(cast(field.value as double)), 0) as string ))
        from TwinFieldSimpleEntity field inner join TwinEntity twin on field.twinId = twin.id
        where twin.headTwinId in :headTwinIdList and field.twinClassFieldId = :twinClassFieldId and not twin.twinStatusId in :childrenTwinStatusIdList
        group by twin.headTwinId
        """)
    List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesWithStatusNotIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("twinClassFieldId") UUID twinClassFieldId,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    List<TwinFieldSimpleEntity> findByTwinId(UUID twinId);

    List<TwinFieldSimpleEntity> findByTwinIdIn(Collection<UUID> twinIdList);

//    @Query(value = "select tfe from TwinFieldEntity tfe join fetch tfe.twinClassField where tfe.twinId in (:twinIds)")
//    List<TwinFieldEntity> findByTwinIdIn(@Param("twinIds") Collection<UUID> twinIdList);

    TwinFieldSimpleEntity findByTwinIdAndTwinClassField_Key(UUID twinId, String key);

    TwinFieldSimpleEntity findByTwinIdAndTwinClassFieldId(UUID twinId, UUID twinClassFieldId);

    void deleteByTwinId(UUID twinId);

    @Query(value = """
            select distinct field.twinClassFieldId
            from TwinFieldSimpleEntity field where field.twin.twinClassId = :twinClassId and field.twinClassFieldId in (:twinClassFields)
            """)
    List<UUID> findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    void deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    @Transactional
    @Modifying
    @Query(value = "update TwinFieldSimpleEntity set twinClassFieldId = :toTwinClassFieldId where twinClassFieldId = :fromTwinClassFieldId and twin.twinClassId = :twinClassId")
    void replaceTwinClassFieldForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("fromTwinClassFieldId") UUID fromTwinClassFieldId, @Param("toTwinClassFieldId") UUID toTwinClassFieldId);


    @Query("""
    SELECT new org.twins.core.dao.twin.TwinFieldSimpleNoRelationsProjection(tfs.id, tfs.twinId, tfs.twinClassFieldId, tfs.value) FROM TwinFieldSimpleEntity tfs
    JOIN TwinEntity te ON te.id = tfs.twinId
    JOIN TwinClassEntity tc ON tc.id = te.twinClassId
    WHERE tc.domainId = :domainId
      AND te.headTwinId IN :headerTwinIdList
      AND te.twinStatusId IN :statusIdList
      AND te.id NOT IN :excludedTwinIds
    """)
    List<TwinFieldSimpleNoRelationsProjection> findTwinFieldSimpleEntityProjected(
            @Param("domainId") UUID domainId,
            @Param("headerTwinIdList") Collection<UUID> headerTwinIdList,
            @Param("excludedTwinIds") Collection<UUID> excludedTwinIds,
            @Param("statusIdList") Collection<UUID> statusIdList);

    @Query(value = """
        select COUNT(*) = 0 from TwinFieldSimpleEntity tfs
        inner join TwinEntity t on tfs.twinId = t.id
        where t.ownerUserId = :ownerUserId and tfs.value = :value and tfs.twinClassFieldId = :twinClassFieldId
    """)
    boolean existsByTwinClassFieldIdAndValueAndOwnerUserId(UUID twinClassFieldId, String value, UUID ownerUserId);

    @Query(value = """
        select COUNT(*) = 0 from TwinFieldSimpleEntity tfs
        inner join TwinEntity t on tfs.twinId = t.id
        where t.ownerBusinessAccountId = :ownerBusinessAccountId and tfs.value = :value and tfs.twinClassFieldId = :twinClassFieldId
    """)
    boolean existsByTwinClassFieldIdAndValueAndOwnerBusinessAccountId(UUID twinClassFieldId, String value, UUID ownerBusinessAccountId);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldHeadSumCountProjection(t.headTwinId, sum(cast(tfs.value as double)), count(tfs.id))
        from TwinFieldSimpleEntity tfs, TwinEntity t
        where tfs.twinId = t.id and t.headTwinId in :headTwinIdSet and tfs.twinClassFieldId = :twinClassFieldId
        group by t.headTwinId
        """)
    List<TwinFieldHeadSumCountProjection> sumAndCountByHeadTwinId(
            @Param("headTwinIdSet") Collection<UUID> headTwinIdSet,
            @Param("twinClassFieldId") UUID twinClassFieldId);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldValueProjection(tfs.twinId, cast(tfs.value as double))
        from TwinFieldSimpleEntity tfs
        where tfs.twinId in :twinIdSet and tfs.twinClassFieldId = :twinClassFieldId
        """)
    List<TwinFieldValueProjection> valueByTwinId(
            @Param("twinIdSet") Collection<UUID> twinIdSet,
            @Param("twinClassFieldId") UUID twinClassFieldId);
}
