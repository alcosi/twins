package org.twins.core.dao.twin;

import org.springframework.data.jpa.domain.Specification;
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

    boolean existsByTwinClassFieldId(UUID twinClassFieldId);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and not child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusNotIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);


    @Query(value = """
            select coalesce(sum(cast(field.value as double)), 0)
            from TwinFieldSimpleEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId=:headTwinId and field.twinClassFieldId = :twinClassFieldId and twin.twinStatusId in :childrenTwinStatusIdList
             """)
    double sumChildrenTwinFieldValuesWithStatusIn(
            @Param("headTwinId") UUID headTwinId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
            select coalesce(sum(cast(field.value as double)), 0)
            from TwinFieldSimpleEntity field inner join TwinEntity twin on field.twinId = twin.id
            where twin.headTwinId=:headTwinId and field.twinClassFieldId = :twinClassFieldId and not twin.twinStatusId in :childrenTwinStatusIdList
             """)
    double sumChildrenTwinFieldValuesWithStatusNotIn(
            @Param("headTwinId") UUID headTwinId, @Param("twinClassFieldId") UUID twinClassFieldId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);


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


}
