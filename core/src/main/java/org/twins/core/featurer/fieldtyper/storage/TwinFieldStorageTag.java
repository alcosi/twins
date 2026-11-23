package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTagEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.twin.TwinTagService;

import java.util.*;

/**
 * Storage of the base tags field. Unlike {@link TwinFieldStorageTwin} ("all data is already in
 * TwinEntity"), tag options live in twin_tag rows and must be loaded — in bulk, via
 * {@link TwinTagService#loadTags(Collection)} — into {@code twin.twinTagKit}, from where the typer
 * deserializes a FieldValueSelect. Markers have the mirror storage
 * {@link TwinFieldStorageMarker}.
 */
@Component
@RequiredArgsConstructor
public class TwinFieldStorageTag extends TwinFieldStorage {
    @Lazy
    private final TwinTagService twinTagService;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        twinTagService.loadTags(twinsKit.getCollection());
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinTagKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinTagKit(new Kit<>(new ArrayList<>(), DataListOptionEntity::getId));
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.emptyList();
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        // all tags of a twin hang off the single pseudo field: nothing to re-link
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        // tag rows follow TwinTagService add/remove semantics, same as before this storage existed
    }

    @Override
    public GroupExpressionProvider<TwinEntity> countGroup(TwinClassFieldEntity field) {
        // a twin carries one row per chosen option, so count it once inside each option group
        return countGroupByColumn(field.getId(), TwinEntity.Fields.tagsSpecOnly, TwinTagEntity.Fields.tagDataListOptionId, true);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
