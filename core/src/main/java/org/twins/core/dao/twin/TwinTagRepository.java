package org.twins.core.dao.twin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinTagRepository extends CrudRepository<TwinTagEntity, UUID>, JpaSpecificationExecutor<TwinTagEntity> {
    List<TwinTagEntity> findByTwinId(UUID twinId);

    List<TwinTagEntity> findByTwinIdIn(Set<UUID> twinIdList);

    @Query(value = "select m.tagDataListOption from TwinTagEntity m where m.twinId = :twinId ")
    List<DataListOptionEntity> findDataListOptionByTwinId(@Param("twinId") UUID twinId);

    @Query(value = "select o from TwinTagEntity tag " +
            " join DataListOptionEntity o on tag.tagDataListOption.id = o.id " +
            " left join I18nTranslationEntity t on o.optionI18NId = t.i18nId" +
            " where o.businessAccountId is null and " +
            " (lower(:name) like lower(o.option) or lower(:name) like lower(t.translation))")
    List<DataListOptionEntity> findOptionOutOfBusinessAccount(@Param("name") String name, Pageable pageable);

    @Query(value = "select tag.tagDataListOption from TwinTagEntity tag " +
                " join DataListOptionEntity o on tag.tagDataListOption.id = o.id " +
                " left join I18nTranslationEntity t on o.optionI18NId = t.i18nId" +
                " where (o.businessAccountId = :businessAccountId or o.businessAccountId is null) and " +
                " (lower(:name) like lower(o.option) or lower(:name) like lower(t.translation)) " +
                " order by o.businessAccountId")
    List<DataListOptionEntity> findOptionForBusinessAccount(@Param("name") String name, @Param("businessAccountId") UUID businessAccountId, Pageable pageable);

    @Query(value = "select o from DataListOptionEntity o " +
            "where o.dataListId = :dataListId " +
            "and (o.businessAccountId = :businessAccountId or o.businessAccountId is null) " +
            "and o.id in (:idList)")
    List<DataListOptionEntity> findForBusinessAccount(@Param("dataListId") UUID dataListId, @Param("businessAccountId") UUID businessAccountId, @Param("idList") Collection<UUID> idList);

    @Query(value = "select o from DataListOptionEntity o " +
                        "where o.dataListId = :dataListId " +
                        "and o.businessAccountId is null " +
                        "and o.id in (:idList)")
    List<DataListOptionEntity> findTagsOutOfBusinessAccount(@Param("dataListId") UUID dataListId, @Param("idList") Collection<UUID> idList);

    void deleteByTwinIdAndTagDataListOptionIdIn(UUID twinId, Set<UUID> markerIdList);
}
