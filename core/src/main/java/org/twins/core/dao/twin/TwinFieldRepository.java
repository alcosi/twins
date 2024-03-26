package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFieldRepository extends CrudRepository<TwinFieldEntity, UUID>, JpaSpecificationExecutor<TwinFieldEntity> {

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and not child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusNotIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);


    @Query(value = """
            select coalesce(sum(
                case when field.value ~ '^\\s*[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)\\s*$'
                    then CAST(field.value as double precision)
                    else 0
                end
            ), 0)
            from twin_field field inner join twin on field.twin_id = twin.id
            where twin.head_twin_id = :headTwinId and field.twin_class_field_id = :twinClassFieldId and twin.twin_status_id in :childrenTwinStatusIdList
            """
            , nativeQuery = true)
    double sumChildrenTwinFieldValuesWithStatusIn(
            @Param("headTwinId") UUID headTwinId,
            @Param("twinClassFieldId") UUID twinClassFieldId,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
            select coalesce(sum(
                case when field.value ~ '^\\s*[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)\\s*$'
                    then CAST(field.value as double precision)
                    else 0
                end
            ), 0)
            from twin_field field inner join twin on field.twin_id = twin.id
            where twin.head_twin_id = :headTwinId and field.twin_class_field_id = :twinClassFieldId and not twin.twin_status_id in :childrenTwinStatusIdList
                     """, nativeQuery = true)
    double sumChildrenTwinFieldValuesWithStatusNotIn(
            @Param("headTwinId") UUID headTwinId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);


    List<TwinFieldEntity> findByTwinId(UUID twinId);

    List<TwinFieldEntity> findByTwinIdIn(Collection<UUID> twinIdList);

//    @Query(value = "select tfe from TwinFieldEntity tfe join fetch tfe.twinClassField where tfe.twinId in (:twinIds)")
//    List<TwinFieldEntity> findByTwinIdIn(@Param("twinIds") Collection<UUID> twinIdList);

    TwinFieldEntity findByTwinIdAndTwinClassField_Key(UUID twinId, String key);

    TwinFieldEntity findByTwinIdAndTwinClassFieldId(UUID twinId, UUID twinClassFieldId);

    void deleteByTwinId(UUID twinId);
}
