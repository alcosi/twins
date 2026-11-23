package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinMarkerEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.twin.TwinMarkerService;

import java.util.*;

/**
 * Storage of the base markers field. Unlike {@link TwinFieldStorageTwin} ("all data is already in
 * TwinEntity"), marker options live in twin_marker rows and must be loaded — in bulk, via
 * {@link TwinMarkerService#loadMarkers(Collection)} — into {@code twin.twinMarkerKit}, from where
 * the typer deserializes a FieldValueSelect. Tags have the mirror storage
 * {@link TwinFieldStorageTag}.
 */
@Component
@RequiredArgsConstructor
public class TwinFieldStorageMarker extends TwinFieldStorage {
    @Lazy
    private final TwinMarkerService twinMarkerService;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        twinMarkerService.loadMarkers(twinsKit.getCollection());
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinMarkerKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinMarkerKit(new Kit<>(new ArrayList<>(), DataListOptionEntity::getId));
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.emptyList();
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        // all markers of a twin hang off the single pseudo field: nothing to re-link
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        // marker rows follow TwinMarkerService add/remove semantics, same as before this storage existed
    }

    @Override
    public GroupExpressionProvider<TwinEntity> countGroup(TwinClassFieldEntity field) {
        // counts the static twin_marker rows only: dynamic markers live in the class config and never
        // appear in twin_marker, so they are not part of this SQL dimension
        return countGroupByColumn(field.getId(), TwinEntity.Fields.markersSpecOnly, TwinMarkerEntity.Fields.markerDataListOptionId, true);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
