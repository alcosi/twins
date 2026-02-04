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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public interface TwinFieldDecimalRepository extends CrudRepository<TwinFieldDecimalEntity, UUID>, JpaSpecificationExecutor<TwinFieldDecimalEntity> {

    List<TwinFieldDecimalEntity> findByTwinId(UUID twinId);
    boolean existsByTwinClassFieldId(UUID twinClassFieldId);
    List<TwinFieldDecimalEntity> findByTwinIdIn(Set<UUID> twinIds);

    @Query(value = """
            select field.twinClassFieldId
            from TwinFieldDecimalEntity field where field.twin.twinClassId = :twinClassId and field.twinClassFieldId in (:twinClassFields)
            """)
    List<UUID> findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    void deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    @Transactional
    @Modifying
    @Query(value = "update TwinFieldDecimalEntity set twinClassFieldId = :toTwinClassFieldId where twinClassFieldId = :fromTwinClassFieldId and twin.twinClassId = :twinClassId")
    void replaceTwinClassFieldForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("fromTwinClassFieldId") UUID fromTwinClassFieldId, @Param("toTwinClassFieldId") UUID toTwinClassFieldId);

    void deleteByTwinIdAndTwinClassFieldIdIn(UUID twinId, Set<UUID> twinClassFieldIds);

    @Query(value = """
            select coalesce(sum(field.value), 0)
            from TwinFieldDecimalEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId=:headTwinId and field.twinClassFieldId = :twinClassFieldId and twin.twinStatusId in :childrenTwinStatusIdList
            """)
    BigDecimal sumChildrenTwinFieldValuesWithStatusIn(@Param("headTwinId") UUID headTwinId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
            select new org.twins.core.dao.twin.TwinFieldCalcProjection(twin.headTwinId, cast(coalesce(sum(field.value), 0) as string))
            from TwinFieldDecimalEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId in :headTwinIdList and field.twinClassFieldId = :twinClassFieldId and twin.twinStatusId in :childrenTwinStatusIdList
            group by twin.headTwinId
            """)
    List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesWithStatusIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("twinClassFieldId") UUID twinClassFieldId,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList
    );

    @Query(value = """
            select coalesce(sum(field.value), 0)
            from TwinFieldDecimalEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId=:headTwinId and field.twinClassFieldId = :twinClassFieldId and not twin.twinStatusId in :childrenTwinStatusIdList
            """)
    BigDecimal sumChildrenTwinFieldValuesWithStatusNotIn(@Param("headTwinId") UUID headTwinId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
            select new org.twins.core.dao.twin.TwinFieldCalcProjection(twin.headTwinId, cast(coalesce(sum(field.value), 0) as string ))
            from TwinFieldDecimalEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId in :headTwinIdList and field.twinClassFieldId = :twinClassFieldId and not twin.twinStatusId in :childrenTwinStatusIdList
            group by twin.headTwinId
            """)
    List<TwinFieldCalcProjection> sumChildrenTwinFieldValuesWithStatusNotIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("twinClassFieldId") UUID twinClassFieldId,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList
    );

    @Query(value = """
            SELECT * FROM twin_field_decimal_calc_sum_by_head(
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
            SELECT * FROM twin_field_decimal_calc_sum_of_divisions_by_head(
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
            SELECT * FROM twin_field_decimal_calc_sum_of_multiplications_by_head(
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
            SELECT * FROM twin_field_decimal_calc_sum_of_subtractions_by_head(
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
            SELECT * FROM twin_field_decimal_calc_sum_by_link(
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
            SELECT * FROM twin_field_decimal_calc_sum_of_divisions_by_link(
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
            SELECT * FROM twin_field_decimal_calc_sum_of_multiplications_by_link(
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
            SELECT * FROM twin_field_decimal_calc_sum_of_subtractions_by_link(
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
                    var twinId = (UUID) row[0];
                    var value = (BigDecimal) row[1];
                    return new TwinFieldCalcProjection(twinId, value);
                })
                .collect(Collectors.toList());
    }
}
