package org.twins.core.dao.twin;

import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.StringUtils;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public interface TwinFieldSimpleRepository extends CrudRepository<TwinFieldSimpleEntity, UUID>, JpaSpecificationExecutor<TwinFieldSimpleEntity> {

    List<TwinFieldSimpleEntity> findByTwinIdInAndTwinClassFieldIdIn(Collection<UUID> twinIdSet, Collection<UUID> twinClassFieldIds);
    boolean existsByTwinClassFieldId(UUID twinClassFieldId);

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
    where t.ownerUserId = :ownerUserId and tfs.value = :value and tfs.twinClassFieldId = :twinClassFieldId and tfs.twinId != :excludeTwinId
    """)
    boolean existsByTwinClassFieldIdAndValueAndOwnerUserIdExcludingTwin(UUID twinClassFieldId, String value, UUID ownerUserId, UUID excludeTwinId);

    @Query(value = """
    select COUNT(*) = 0 from TwinFieldSimpleEntity tfs
    inner join TwinEntity t on tfs.twinId = t.id
    where t.ownerBusinessAccountId = :ownerBusinessAccountId and tfs.value = :value and tfs.twinClassFieldId = :twinClassFieldId and tfs.twinId != :excludeTwinId
    """)
    boolean existsByTwinClassFieldIdAndValueAndOwnerBusinessAccountIdExcludingTwin(UUID twinClassFieldId, String value, UUID ownerBusinessAccountId, UUID excludeTwinId);

    @Query(value = """
    select COUNT(*) = 0 from TwinFieldSimpleEntity tfs
    where tfs.twinClassFieldId = :twinClassFieldId and tfs.value = :value and tfs.twinId != :excludeTwinId
    """)
    boolean existsByTwinClassFieldIdAndValueExcludingTwin(UUID twinClassFieldId, String value, UUID excludeTwinId);

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

    void deleteByTwinIdAndTwinClassFieldIdIn(UUID twinId, Set<UUID> twinClassFieldIds);

    @Query(value = """
        SELECT * FROM twin_field_calc_sum_by_head(
            string_to_array(:headTwinIdListStr, ',')::uuid[],
            string_to_array(:twinClassFieldIdsStr, ',')::uuid[],
            string_to_array(:childrenTwinStatusIdListStr, ',')::uuid[],
            :excludeStatus,
            string_to_array(:childrenTwinOfClassIdListStr, ',')::uuid[]
        )
        """, nativeQuery = true)
    List<Object[]> _sumChildrenTwinFieldValuesByHead(
            @Param("headTwinIdListStr") String headTwinIdListStr,
            @Param("twinClassFieldIdsStr") String twinClassFieldIdsStr,
            @Param("childrenTwinStatusIdListStr") String childrenTwinStatusIdListStr,
            @Param("excludeStatus") boolean exclude,
            @Param("childrenTwinOfClassIdListStr") String childrenTwinOfClassIdListStr);

    @Query(value = """
    SELECT * FROM twin_field_calc_sum_of_divisions_by_head(
        string_to_array(:headTwinIdsStr, ',')::uuid[],
        string_to_array(:childrenInTwinStatusIdsStr, ',')::uuid[],
        string_to_array(:childrenOfTwinClassIdsStr, ',')::uuid[],
        :firstTwinClassFieldId,
        :secondTwinClassFieldId,
        :excludeStatus,
        :throwOnDivisionByZero
    )
    """, nativeQuery = true)
    List<Object[]> _sumChildrenTwinFieldValuesOfDivisionsByHead(
            @Param("headTwinIdsStr") String headTwinIdsStr,
            @Param("childrenInTwinStatusIdsStr") String childrenInTwinStatusIdsStr,
            @Param("childrenOfTwinClassIdsStr") String childrenOfTwinClassIdsStr,
            @Param("firstTwinClassFieldId") UUID firstTwinClassFieldId,
            @Param("secondTwinClassFieldId") UUID secondTwinClassFieldId,
            @Param("excludeStatus") boolean excludeStatus,
            @Param("throwOnDivisionByZero") boolean throwOnDivisionByZero);

    @Query(value = """
        SELECT * FROM twin_field_calc_sum_of_multiplications_by_head(
            string_to_array(:headTwinIdsStr, ',')::uuid[],
            string_to_array(:childrenInTwinStatusIdsStr, ',')::uuid[],
            string_to_array(:childrenOfTwinClassIdsStr, ',')::uuid[],
            :firstTwinClassFieldId,
            :secondTwinClassFieldId,
            :excludeStatus
        )
        """, nativeQuery = true)
    List<Object[]> _sumChildrenTwinFieldValuesOfMultiplicationsByHead(
            @Param("headTwinIdsStr") String headTwinIdsStr,
            @Param("childrenInTwinStatusIdsStr") String childrenInTwinStatusIdsStr,
            @Param("childrenOfTwinClassIdsStr") String childrenOfTwinClassIdsStr,
            @Param("firstTwinClassFieldId") UUID firstTwinClassFieldId,
            @Param("secondTwinClassFieldId") UUID secondTwinClassFieldId,
            @Param("excludeStatus") boolean exclude);

    @Query(value = """
        SELECT * FROM twin_field_calc_sum_of_subtractions_by_head(
            string_to_array(:headTwinIdsStr, ',')::uuid[],
            string_to_array(:childrenInTwinStatusIdsStr, ',')::uuid[],
            string_to_array(:childrenOfTwinClassIdsStr, ',')::uuid[],
            :firstTwinClassFieldId,
            :secondTwinClassFieldId,
            :excludeStatus
        )
        """, nativeQuery = true)
    List<Object[]> _sumChildrenTwinFieldValuesOfSubtractionsByHead(
            @Param("headTwinIdsStr") String headTwinIdsStr,
            @Param("childrenInTwinStatusIdsStr") String childrenInTwinStatusIdsStr,
            @Param("childrenOfTwinClassIdsStr") String childrenOfTwinClassIdsStr,
            @Param("firstTwinClassFieldId") UUID firstTwinClassFieldId,
            @Param("secondTwinClassFieldId") UUID secondTwinClassFieldId,
            @Param("excludeStatus") boolean exclude);

    @Query(value = """
        SELECT * FROM twin_field_calc_sum_by_link(
            string_to_array(:linkedToTwinIdsStr, ',')::uuid[],
            :srcElseDst,
            string_to_array(:linkedFromInTwinStatusIdsStr, ',')::uuid[],
            string_to_array(:linkedTwinOfClassIdsStr, ',')::uuid[],
            string_to_array(:twinClassFieldIdsStr, ',')::uuid[],
            :statusExclude
        )
        """, nativeQuery = true)
    List<Object[]> _sumLinkedTwinFieldValuesByLink(
            @Param("linkedToTwinIdsStr") String linkedToTwinIdsStr,
            @Param("srcElseDst") boolean srcElseDst,
            @Param("linkedFromInTwinStatusIdsStr") String linkedFromInTwinStatusIdsStr,
            @Param("linkedTwinOfClassIdsStr") String linkedTwinOfClassIdsStr,
            @Param("twinClassFieldIdsStr") String twinClassFieldIdsStr,
            @Param("statusExclude") boolean statusExclude);

    @Query(value = """
    SELECT * FROM twin_field_calc_sum_of_divisions_by_link(
        string_to_array(:linkedToTwinIdsStr, ',')::uuid[],
        :srcElseDst,
        string_to_array(:linkedFromInTwinStatusIdsStr, ',')::uuid[],
        string_to_array(:linkedTwinOfClassIdsStr, ',')::uuid[],
        :firstTwinClassFieldId,
        :secondTwinClassFieldId,
        :statusExclude,
        :throwOnDivisionByZero
    )
    """, nativeQuery = true)
    List<Object[]> _sumLinkedTwinFieldValuesOfDivisionsByLink(
            @Param("linkedToTwinIdsStr") String linkedToTwinIdsStr,
            @Param("srcElseDst") boolean srcElseDst,
            @Param("linkedFromInTwinStatusIdsStr") String linkedFromInTwinStatusIdsStr,
            @Param("linkedTwinOfClassIdsStr") String linkedTwinOfClassIdsStr,
            @Param("firstTwinClassFieldId") UUID firstTwinClassFieldId,
            @Param("secondTwinClassFieldId") UUID secondTwinClassFieldId,
            @Param("statusExclude") boolean statusExclude,
            @Param("throwOnDivisionByZero") boolean throwOnDivisionByZero);

    @Query(value = """
        SELECT * FROM twin_field_calc_sum_of_multiplications_by_link(
            string_to_array(:linkedToTwinIdsStr, ',')::uuid[],
            :srcElseDst,
            string_to_array(:linkedFromInTwinStatusIdsStr, ',')::uuid[],
            string_to_array(:linkedTwinOfClassIdsStr, ',')::uuid[],
            :firstTwinClassFieldId,
            :secondTwinClassFieldId,
            :statusExclude
        )
        """, nativeQuery = true)
    List<Object[]> _sumLinkedTwinFieldValuesOfMultiplicationsByLink(
            @Param("linkedToTwinIdsStr") String linkedToTwinIdsStr,
            @Param("srcElseDst") boolean srcElseDst,
            @Param("linkedFromInTwinStatusIdsStr") String linkedFromInTwinStatusIdsStr,
            @Param("linkedTwinOfClassIdsStr") String linkedTwinOfClassIdsStr,
            @Param("firstTwinClassFieldId") UUID firstTwinClassFieldId,
            @Param("secondTwinClassFieldId") UUID secondTwinClassFieldId,
            @Param("statusExclude") boolean statusExclude);

    @Query(value = """
        SELECT * FROM twin_field_calc_sum_of_subtractions_by_link(
            string_to_array(:linkedToTwinIdsStr, ',')::uuid[],
            :srcElseDst,
            string_to_array(:linkedFromInTwinStatusIdsStr, ',')::uuid[],
            string_to_array(:linkedTwinOfClassIdsStr, ',')::uuid[],
            :firstTwinClassFieldId,
            :secondTwinClassFieldId,
            :statusExclude
        )
        """, nativeQuery = true)
    List<Object[]> _sumLinkedTwinFieldValuesOfSubtractionsByLink(
            @Param("linkedToTwinIdsStr") String linkedToTwinIdsStr,
            @Param("srcElseDst") boolean srcElseDst,
            @Param("linkedFromInTwinStatusIdsStr") String linkedFromInTwinStatusIdsStr,
            @Param("linkedTwinOfClassIdsStr") String linkedTwinOfClassIdsStr,
            @Param("firstTwinClassFieldId") UUID firstTwinClassFieldId,
            @Param("secondTwinClassFieldId") UUID secondTwinClassFieldId,
            @Param("statusExclude") boolean statusExclude);

    default List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesByHead(
            Collection<UUID> headTwinIdList,
            Collection<UUID> twinClassFieldIds,
            Collection<UUID> childrenTwinStatusIdList,
            boolean exclude,
            Collection<UUID> childrenTwinOfClassIdList) {

        String headStr = StringUtils.collectionToString(headTwinIdList);
        String fieldsStr = StringUtils.collectionToString(twinClassFieldIds);
        String statusStr = StringUtils.collectionToString(childrenTwinStatusIdList);
        String classStr = StringUtils.collectionToString(childrenTwinOfClassIdList);

        List<Object[]> results = _sumChildrenTwinFieldValuesByHead(
                headStr, fieldsStr, statusStr, exclude, classStr
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesOfDivisionsByHead(
            Collection<UUID> headTwinIds,
            Collection<UUID> childrenInTwinStatusIds,
            Collection<UUID> childrenOfTwinClassIds,
            UUID firstTwinClassFieldId,
            UUID secondTwinClassFieldId,
            boolean exclude,
            boolean throwOnDivisionByZero) {

        String headStr = StringUtils.collectionToString(headTwinIds);
        String statusStr = StringUtils.collectionToString(childrenInTwinStatusIds);
        String classStr = StringUtils.collectionToString(childrenOfTwinClassIds);

        List<Object[]> results = _sumChildrenTwinFieldValuesOfDivisionsByHead(
                headStr, statusStr, classStr,
                firstTwinClassFieldId, secondTwinClassFieldId,
                exclude, throwOnDivisionByZero
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesOfMultiplicationsByHead(
            Collection<UUID> headTwinIds,
            Collection<UUID> childrenInTwinStatusIds,
            Collection<UUID> childrenOfTwinClassIds,
            UUID firstTwinClassFieldId,
            UUID secondTwinClassFieldId,
            boolean exclude) {

        String headStr = StringUtils.collectionToString(headTwinIds);
        String statusStr = StringUtils.collectionToString(childrenInTwinStatusIds);
        String classStr = StringUtils.collectionToString(childrenOfTwinClassIds);

        List<Object[]> results = _sumChildrenTwinFieldValuesOfMultiplicationsByHead(
                headStr, statusStr, classStr,
                firstTwinClassFieldId, secondTwinClassFieldId, exclude
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesOfSubtractionsByHead(
            Collection<UUID> headTwinIds,
            Collection<UUID> childrenInTwinStatusIds,
            Collection<UUID> childrenOfTwinClassIds,
            UUID firstTwinClassFieldId,
            UUID secondTwinClassFieldId,
            boolean exclude) {

        String headStr = StringUtils.collectionToString(headTwinIds);
        String statusStr = StringUtils.collectionToString(childrenInTwinStatusIds);
        String classStr = StringUtils.collectionToString(childrenOfTwinClassIds);

        List<Object[]> results = _sumChildrenTwinFieldValuesOfSubtractionsByHead(
                headStr, statusStr, classStr,
                firstTwinClassFieldId, secondTwinClassFieldId, exclude
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> sumLinkedTwinFieldValuesByLink(
            Collection<UUID> linkedToTwinIds,
            boolean srcElseDst,
            Collection<UUID> linkedFromInTwinStatusIds,
            Collection<UUID> linkedTwinOfClassIds,
            Collection<UUID> twinClassFieldIds,
            boolean statusExclude) {

        String linkedToStr = StringUtils.collectionToString(linkedToTwinIds);
        String statusStr = StringUtils.collectionToString(linkedFromInTwinStatusIds);
        String classStr = StringUtils.collectionToString(linkedTwinOfClassIds);
        String fieldsStr = StringUtils.collectionToString(twinClassFieldIds);

        List<Object[]> results = _sumLinkedTwinFieldValuesByLink(
                linkedToStr, srcElseDst, statusStr, classStr, fieldsStr, statusExclude
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> sumLinkedTwinFieldValuesOfDivisionsByLink(
            Collection<UUID> linkedToTwinIds,
            boolean srcElseDst,
            Collection<UUID> linkedFromInTwinStatusIds,
            Collection<UUID> linkedTwinOfClassIds,
            UUID firstTwinClassFieldId,
            UUID secondTwinClassFieldId,
            boolean statusExclude,
            boolean throwOnDivisionByZero) {

        String linkedToStr = StringUtils.collectionToString(linkedToTwinIds);
        String statusStr = StringUtils.collectionToString(linkedFromInTwinStatusIds);
        String classStr = StringUtils.collectionToString(linkedTwinOfClassIds);

        List<Object[]> results = _sumLinkedTwinFieldValuesOfDivisionsByLink(
                linkedToStr, srcElseDst, statusStr, classStr,
                firstTwinClassFieldId, secondTwinClassFieldId,
                statusExclude, throwOnDivisionByZero
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> sumLinkedTwinFieldValuesOfMultiplicationsByLink(
            Collection<UUID> linkedToTwinIds,
            boolean srcElseDst,
            Collection<UUID> linkedFromInTwinStatusIds,
            Collection<UUID> linkedTwinOfClassIds,
            UUID firstTwinClassFieldId,
            UUID secondTwinClassFieldId,
            boolean statusExclude) {

        String linkedToStr = StringUtils.collectionToString(linkedToTwinIds);
        String statusStr = StringUtils.collectionToString(linkedFromInTwinStatusIds);
        String classStr = StringUtils.collectionToString(linkedTwinOfClassIds);

        List<Object[]> results = _sumLinkedTwinFieldValuesOfMultiplicationsByLink(
                linkedToStr, srcElseDst, statusStr, classStr,
                firstTwinClassFieldId, secondTwinClassFieldId, statusExclude
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> sumLinkedTwinFieldValuesOfSubtractionsByLink(
            Collection<UUID> linkedToTwinIds,
            boolean srcElseDst,
            Collection<UUID> linkedFromInTwinStatusIds,
            Collection<UUID> linkedTwinOfClassIds,
            UUID firstTwinClassFieldId,
            UUID secondTwinClassFieldId,
            boolean statusExclude) {

        String linkedToStr = StringUtils.collectionToString(linkedToTwinIds);
        String statusStr = StringUtils.collectionToString(linkedFromInTwinStatusIds);
        String classStr = StringUtils.collectionToString(linkedTwinOfClassIds);

        List<Object[]> results = _sumLinkedTwinFieldValuesOfSubtractionsByLink(
                linkedToStr, srcElseDst, statusStr, classStr,
                firstTwinClassFieldId, secondTwinClassFieldId, statusExclude
        );
        return mapNativeQueryResults(results);
    }

    default List<TwinFieldCalcProjection> mapNativeQueryResults(List<Object[]> results) {
        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyList();
        }

        return results.stream()
                .filter(Objects::nonNull)
                .filter(row -> row.length >= 2)
                .map(row -> {
                    UUID twinId = (UUID) row[0];
                    Object value = row[1];
                    String calcValue = StringUtils.formatNumericValue(value);
                    return new TwinFieldCalcProjection(twinId, calcValue);
                })
                .collect(Collectors.toList());
    }
}
