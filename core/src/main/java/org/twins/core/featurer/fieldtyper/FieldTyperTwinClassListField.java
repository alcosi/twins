package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.context.HistoryContextTwinClassMultiChange;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchSpaceRoleUser;
import org.twins.core.domain.search.TwinFieldSearchTwinClassList;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwinClassList;
import org.twins.core.featurer.fieldtyper.value.FieldValueTwinClassList;
import org.twins.core.service.history.HistoryItem;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1334,
        name = "Twin class list field",
        description = "Field typer for twin class list field")
public class FieldTyperTwinClassListField extends FieldTyper<FieldDescriptorTwinClassList, FieldValueTwinClassList, TwinFieldStorageTwinClassList, TwinFieldSearchTwinClassList> {

    @Override
    protected FieldDescriptorTwinClassList getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorTwinClassList();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueTwinClassList value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.getTwinClassField().getRequired() && CollectionUtils.isEmpty(value.getTwinClassIdSet())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        }

        List<TwinClassEntity> selectedTwinClassEntities = twinClassService.findEntitiesSafe(value.getTwinClassIdSet()).getList();
        twinService.loadTwinFields(twin);

        Map<UUID, TwinFieldTwinClassEntity> storedTwinClassEntities = null;
        if (twin.getTwinFieldTwinClassKit().containsGroupedKey(value.getTwinClassField().getId())) {
            storedTwinClassEntities = twin.getTwinFieldTwinClassKit().getGrouped(value.getTwinClassField().getId()).stream()
                    .collect(Collectors.toMap(TwinFieldTwinClassEntity::getTwinClassId, Function.identity()));
        }

        if (FieldValueChangeHelper.isSingleValueAdd(selectedTwinClassEntities, storedTwinClassEntities)) {
            TwinClassEntity twinClassEntity = selectedTwinClassEntities.get(0);

            if (twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeTwinClass(value.getTwinClassField(), null, twinClassEntity));
            }

            twinChangesCollector.add(new TwinFieldTwinClassEntity()
                    .setTwin(twin)
                    .setTwinId(twin.getId())
                    .setTwinClassFieldId(value.getTwinClassField().getId())
                    .setTwinClassId(twinClassEntity.getId())
                    .setTwinClass(twinClassEntity));

            return;
        }

        if (FieldValueChangeHelper.isSingleToSingleValueUpdate(selectedTwinClassEntities, storedTwinClassEntities)) {
            TwinClassEntity twinClassEntity = selectedTwinClassEntities.get(0);
            TwinFieldTwinClassEntity storeField = MapUtils.pullAny(storedTwinClassEntities);

            if (!storeField.getTwinClassId().equals(twinClassEntity.getId())) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeTwinClass(value.getTwinClassField(), storeField.getTwinClass(), twinClassEntity));
                }

                twinChangesCollector.add(storeField
                        .setTwinClassId(twinClassEntity.getId())
                        .setTwinClass(twinClassEntity));
            }

            return;
        }

        HistoryItem<HistoryContextTwinClassMultiChange> historyItem = historyService.fieldChangeTwinClassMulti(value.getTwinClassField());
        for (TwinClassEntity userEntity : selectedTwinClassEntities) {
            //todo check if user valid for current filter result
            if (FieldValueChangeHelper.notSaved(userEntity.getId(), storedTwinClassEntities)) { // no values were saved before
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    historyItem.getContext().shotAddedTwinClassId(userEntity.getId());
                }

                twinChangesCollector.add(new TwinFieldTwinClassEntity()
                        .setTwin(twin)
                        .setTwinId(twin.getId())
                        .setTwinClassFieldId(value.getTwinClassField().getId())
                        .setTwinClassId(userEntity.getId())
                        .setTwinClass(userEntity));
            } else {
                storedTwinClassEntities.remove(userEntity.getId()); // we remove is from list, because all remained list elements will be deleted from database (pretty logic inversion)
            }
        }

        if (FieldValueChangeHelper.hasOutOfDateValues(storedTwinClassEntities)) {// old values must be deleted
            if (twinChangesCollector.isHistoryCollectorEnabled())
                for (TwinFieldTwinClassEntity deleteField : storedTwinClassEntities.values()) {
                    historyItem.getContext().shotDeletedTwinClassId(deleteField.getTwinClassId());
                }
            twinChangesCollector.deleteAll(storedTwinClassEntities.values());
        }
        if (twinChangesCollector.isHistoryCollectorEnabled() && historyItem.getContext().notEmpty())
            twinChangesCollector.getHistoryCollector(twin).add(historyItem);
    }

    @Override
    protected FieldValueTwinClassList deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twinEntity = twinField.getTwin();
        twinService.loadTwinFields(twinEntity);

        List<TwinFieldTwinClassEntity> twinFieldTwinClassEntityList = twinEntity.getTwinFieldTwinClassKit().getGrouped(twinField.getTwinClassField().getId());
        FieldValueTwinClassList ret = new FieldValueTwinClassList(twinField.getTwinClassField());
        if (twinFieldTwinClassEntityList != null)
            for (var item : twinFieldTwinClassEntityList) {
                ret.getTwinClassIdSet().add(item.getTwinClassId());
            }

        return ret;
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchTwinClassList search) {
        return Specification.where(TwinSpecification.checkFieldTwinClassList(search));
    }
}
