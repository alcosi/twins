package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinTagRepository extends CrudRepository<TwinTagEntity, UUID>, JpaSpecificationExecutor<TwinTagEntity> {
    List<TwinTagEntity> findByTwinId(UUID twinId);

    List<TwinTagEntity> findByTwinIdIn(Set<UUID> twinIdList);

    @Query(value = "select m.tagDataListOption from TwinTagEntity m where m.twinId = :twinId ")
    List<DataListOptionEntity> findDataListOptionByTwinId(@Param("twinId") UUID twinId);

    @Query(value = "select tag.tagDataListOption from TwinTagEntity tag " +
            " join DataListOptionEntity o on tag.tagDataListOption.id = o.id " +
            " left join I18nEntity i on o.optionI18NId = i.id" +
            " left join I18nTranslationEntity t on i.id = t.i18nId" +
            " where (lower(:name) like lower(o.option) or lower(:name) like lower(t.translation))")
    DataListOptionEntity findOptionByTagName(@Param("name") String name);

    void deleteByTwinId(UUID twinId);
}
