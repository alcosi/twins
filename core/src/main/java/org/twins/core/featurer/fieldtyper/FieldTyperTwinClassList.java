package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassListEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwinClassList;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;

public abstract class FieldTyperTwinClassList<D extends FieldDescriptor, T extends FieldValue, A extends TwinFieldSearch> extends FieldTyper<D, T, TwinFieldStorageTwinClassList, A> {

    protected void detectValueChange(TwinFieldTwinClassListEntity twinFieldTwinClassListEntity, TwinChangesCollector twinChangesCollector, Set<UUID> newValue) throws ServiceException {
        if (twinChangesCollector.collectIfChanged(twinFieldTwinClassListEntity, "field[" + twinFieldTwinClassListEntity.getTwinClassField().getKey() + "]", twinFieldTwinClassListEntity.getTwinClassSet(), newValue)) {
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(twinFieldTwinClassListEntity.getTwin()).add(
                        historyService.fieldChangeSimple(twinFieldTwinClassListEntity.getTwinClassField(), String.valueOf(twinFieldTwinClassListEntity.getTwinClassSet()), String.valueOf(newValue)));

            Set<TwinClassEntity> twinClassSet = new HashSet<>(twinClassService.findEntitiesSafe(newValue).getCollection());
            twinFieldTwinClassListEntity.setTwinClassSet(twinClassSet);

            for (var twinClass : twinClassSet) {
                twinClass.getTwinFieldTwinClassListSet().add(twinFieldTwinClassListEntity);
            }

            twinClassService.saveAllSafe(twinClassSet);
        }
    }

    public TwinFieldTwinClassListEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldTwinClassListKit().get(twinClassFieldEntity.getId());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinFieldTwinClassListEntity twinFieldTwinClassListEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());

        if (twinFieldTwinClassListEntity == null) {
            twinFieldTwinClassListEntity = twinService.createTwinFieldTwinClassEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldTwinClassListEntity);
        }

        serializeValue(properties, twinFieldTwinClassListEntity, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinFieldTwinClassListEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;

    @Override
    protected T deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinFieldTwinClassListEntity twinFieldTwinClassListEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return deserializeValue(properties, twinField, twinFieldTwinClassListEntity);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField, TwinFieldTwinClassListEntity twinFieldTwinClassListEntity) throws ServiceException;
}
