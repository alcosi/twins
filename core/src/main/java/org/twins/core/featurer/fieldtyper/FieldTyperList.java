package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.history.context.HistoryContextFieldDatalistMultiChange;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDataListRepository;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.history.HistoryItem;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class FieldTyperList extends FieldTyper<FieldDescriptor, FieldValueSelect> {
    @Autowired
    DataListOptionRepository dataListOptionRepository;

    @Autowired
    @Lazy
    DataListService dataListService;

    @Autowired
    TwinFieldDataListRepository twinFieldDataListRepository;

    @FeaturerParam(name = "listUUID", description = "")
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUID("listUUID");

    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueSelect value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().isRequired() && CollectionUtils.isEmpty(value.getOptions()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (value.getOptions() != null && value.getOptions().size() > 1 && !allowMultiply(properties))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply options are not allowed");
        UUID fieldListId = listUUID.extract(properties);
        List<DataListOptionEntity> dataListOptionEntityList = dataListOptionRepository.findByIdIn(value.getOptions().stream().map(DataListOptionEntity::getId).toList());
        Map<UUID, TwinFieldDataListEntity> storedOptions = null;
        if (twinFieldEntity.getId() != null) //not new field
            storedOptions = twinFieldDataListRepository.findByTwinFieldId(twinFieldEntity.getId()).stream().collect(Collectors.toMap(TwinFieldDataListEntity::getDataListOptionId, Function.identity()));
        else
            twinFieldEntity.setId(UUID.randomUUID()); // we have to generate id here, because TwinFieldDataListEntity is linked to TwinFieldEntity by FK
        if (FieldValueChangeHelper.isSingleValueAdd(dataListOptionEntityList, storedOptions)) {
            DataListOptionEntity dataListOptionEntity = dataListOptionEntityList.get(0);
            twinChangesCollector.getHistoryCollector(twinFieldEntity.getTwin())
                    .add(historyService.fieldChangeDataList(twinFieldEntity.getTwinClassField(), null, dataListOptionEntity));
            twinChangesCollector.add(new TwinFieldDataListEntity()
                    .setTwinFieldId(twinFieldEntity.getId())
                    .setDataListOptionId(checkOptionAllowed(twinFieldEntity, dataListOptionEntity))
                    .setDataListOption(dataListOptionEntity));
            return;
        }
        if (FieldValueChangeHelper.isSingleToSingleValueUpdate(dataListOptionEntityList, storedOptions)) {
            DataListOptionEntity dataListOptionEntity = dataListOptionEntityList.get(0);
            TwinFieldDataListEntity storeField = storedOptions.values().iterator().next();
            if (!storeField.getDataListOptionId().equals(dataListOptionEntity.getId())) {
                twinChangesCollector.getHistoryCollector(twinFieldEntity.getTwin())
                        .add(historyService.fieldChangeDataList(twinFieldEntity.getTwinClassField(), storeField.getDataListOption(), dataListOptionEntity));
                twinChangesCollector.add(storeField //we can update existing record
                        .setDataListOptionId(checkOptionAllowed(twinFieldEntity, dataListOptionEntity))
                        .setDataListOption(dataListOptionEntity));
            }
            return;
        }
        HistoryItem<HistoryContextFieldDatalistMultiChange> historyItem = historyService.fieldChangeDataListMulti(twinFieldEntity.getTwinClassField());
        for (DataListOptionEntity dataListOptionEntity : dataListOptionEntityList) {
            if (!dataListOptionEntity.getDataListId().equals(fieldListId))
                throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_IS_NOT_VALID_FOR_LIST, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " optionId[" + dataListOptionEntity.getId() + "] is not valid for list[" + fieldListId + "]");
            if (FieldValueChangeHelper.notSaved(dataListOptionEntity.getId(), storedOptions)) { // no values were saved before
                historyItem.getContext().shotAddedDataListOption(dataListOptionEntity, i18nService);
                twinChangesCollector.add(new TwinFieldDataListEntity()
                        .setTwinFieldId(twinFieldEntity.getId())
                        .setDataListOptionId(checkOptionAllowed(twinFieldEntity, dataListOptionEntity))
                        .setDataListOption(dataListOptionEntity));
            } else {
                storedOptions.remove(dataListOptionEntity.getId()); // we remove is from list, because all remained list elements will be deleted from database (pretty logic inversion)
            }
        }
        if (FieldValueChangeHelper.hasOutOfDateValues(storedOptions)) { // old values must be deleted
            List<UUID> deleteIds = new ArrayList<>();
            for (TwinFieldDataListEntity deleteField : storedOptions.values()) {
                deleteIds.add(deleteField.getId());
                historyItem.getContext().shotDeletedDataListOption(deleteField.getDataListOption(), i18nService);
            }
            twinChangesCollector.deleteAll(TwinFieldDataListEntity.class, deleteIds);
        }
        if (historyItem.getContext().notEmpty())
            twinChangesCollector.getHistoryCollector(twinFieldEntity.getTwin()).add(historyItem);
    }

    public UUID checkOptionAllowed(TwinFieldEntity twinFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        return dataListOptionEntity.getId();
    }

    protected boolean allowMultiply(Properties properties) {
        return true;
    }

    public static final String LIST_SPLITTER = "<@2@>";

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) {
        FieldValueSelect ret = new FieldValueSelect();
        if (twinFieldEntity.getId() != null) {
            List<TwinFieldDataListEntity> twinFieldDataListEntityList = twinFieldDataListRepository.findByTwinFieldId(twinFieldEntity.getId());
            for (TwinFieldDataListEntity twinFieldDataListEntity : twinFieldDataListEntityList) {
                ret.add(twinFieldDataListEntity.getDataListOption());
            }
        }
        return ret;
    }
}
