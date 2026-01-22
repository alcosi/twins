package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.history.context.HistoryContextDatalistMultiChange;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchList;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.featurer.params.FeaturerParamUUIDSetDatalistOptionId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetDatalistSubsetId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListId;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.history.HistoryItem;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class FieldTyperList extends FieldTyper<FieldDescriptor, FieldValueSelect, TwinFieldStorageDatalist, TwinFieldSearchList> {
    @Autowired
    @Lazy
    DataListService dataListService;

    @Autowired
    DataListOptionService dataListOptionService;

    @FeaturerParam(name = "Datalist", description = "", order = 1)
    public static final FeaturerParamUUID dataListId = new FeaturerParamUUIDTwinsDataListId("listUUID");

    @FeaturerParam(name = "datalist option ids", description = "", order = 6, optional = true)
    public static final FeaturerParamUUIDSet dataListOptionIds = new FeaturerParamUUIDSetDatalistOptionId("dataListOptionIds");

    @FeaturerParam(name = "datalist option exclude ids", description = "", order = 7, optional = true)
    public static final FeaturerParamUUIDSet dataListOptionExcludeIds = new FeaturerParamUUIDSetDatalistOptionId("dataListOptionExcludeIds");

    @FeaturerParam(name = "datalist subset ids", description = "", order = 8, optional = true)
    public static final FeaturerParamUUIDSet dataListSubsetIds = new FeaturerParamUUIDSetDatalistSubsetId("dataListSubsetIds");

    @FeaturerParam(name = "datalist subset exclude ids", description = "", order = 9, optional = true)
    public static final FeaturerParamUUIDSet dataListSubsetIdExcludeIds = new FeaturerParamUUIDSetDatalistSubsetId("dataListSubsetIdExcludeIds");

    @Override
    protected FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        FieldDescriptorList fieldDescriptorList = new FieldDescriptorList();
        UUID listId = dataListId.extract(properties);
        dataListService.checkId(listId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS); //todo check on save, to reduce db load
        fieldDescriptorList
                .dataListId(listId)
                .dataListOptionIdList(dataListOptionIds.extract(properties))
                .dataListOptionIdExcludeList(dataListOptionExcludeIds.extract(properties))
                .dataListSubsetIdList(dataListSubsetIds.extract(properties))
                .dataListSubsetIdExcludeList(dataListSubsetIdExcludeIds.extract(properties));
        return fieldDescriptorList;

    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueSelect value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        List<DataListOptionEntity> dataListOptionEntityList = value.getOptions();
        Map<UUID, TwinFieldDataListEntity> storedOptions = null;
        twinService.loadTwinFields(twin);
        if (twin.getTwinFieldDatalistKit().containsGroupedKey(value.getTwinClassField().getId()))
            storedOptions = twin.getTwinFieldDatalistKit().getGrouped(value.getTwinClassField().getId())
                    .stream().collect(Collectors.toMap(TwinFieldDataListEntity::getDataListOptionId, Function.identity()));
        if (FieldValueChangeHelper.isSingleValueAdd(dataListOptionEntityList, storedOptions)) {
            DataListOptionEntity dataListOptionEntity = dataListOptionEntityList.get(0);
            if (twinChangesCollector.isHistoryCollectorEnabled())
                twinChangesCollector.getHistoryCollector(twin)
                        .add(historyService.fieldChangeDataList(value.getTwinClassField(), null, dataListOptionEntity));
            twinChangesCollector.add(new TwinFieldDataListEntity()
                    .setTwin(twin)
                    .setTwinId(twin.getId())
                    .setTwinClassFieldId(value.getTwinClassField().getId())
                    .setDataListOptionId(checkOptionAllowed(twin, value.getTwinClassField(), dataListOptionEntity)) //todo move to validate method
                    .setDataListOption(dataListOptionEntity));
            return;
        }
        if (FieldValueChangeHelper.isSingleToSingleValueUpdate(dataListOptionEntityList, storedOptions)) {
            DataListOptionEntity dataListOptionEntity = dataListOptionEntityList.get(0);
            TwinFieldDataListEntity storeField = storedOptions.values().iterator().next();
            if (!storeField.getDataListOptionId().equals(dataListOptionEntity.getId())) {
                if (twinChangesCollector.isHistoryCollectorEnabled())
                    twinChangesCollector.getHistoryCollector(twin)
                            .add(historyService.fieldChangeDataList(value.getTwinClassField(), storeField.getDataListOption(), dataListOptionEntity));
                twinChangesCollector.add(storeField //we can update existing record
                        .setDataListOptionId(checkOptionAllowed(twin, value.getTwinClassField(), dataListOptionEntity)) //todo move to validate method
                        .setDataListOption(dataListOptionEntity));
            }
            return;
        }
        HistoryItem<HistoryContextDatalistMultiChange> historyItem = historyService.fieldChangeDataListMulti(value.getTwinClassField());
        for (DataListOptionEntity dataListOptionEntity : dataListOptionEntityList) {
            if (FieldValueChangeHelper.notSaved(dataListOptionEntity.getId(), storedOptions)) { // no values were saved before
                if (twinChangesCollector.isHistoryCollectorEnabled())
                    historyItem.getContext().shotAddedDataListOption(dataListOptionEntity, i18nService);
                twinChangesCollector.add(new TwinFieldDataListEntity()
                        .setTwin(twin)
                        .setTwinId(twin.getId())
                        .setTwinClassFieldId(value.getTwinClassField().getId())
                        .setDataListOptionId(checkOptionAllowed(twin, value.getTwinClassField(), dataListOptionEntity)) //todo move to validate method
                        .setDataListOption(dataListOptionEntity));
            } else {
                storedOptions.remove(dataListOptionEntity.getId()); // we remove is from list, because all remained list elements will be deleted from database (pretty logic inversion)
            }
        }
        if (FieldValueChangeHelper.hasOutOfDateValues(storedOptions)) { // old values must be deleted
            if (twinChangesCollector.isHistoryCollectorEnabled())
                for (TwinFieldDataListEntity deleteField : storedOptions.values()) {
                    historyItem.getContext().shotDeletedDataListOption(deleteField.getDataListOption(), i18nService);
                }
            twinChangesCollector.deleteAll(storedOptions.values());
        }
        if (twinChangesCollector.isHistoryCollectorEnabled() && historyItem.getContext().notEmpty())
            twinChangesCollector.getHistoryCollector(twin).add(historyItem);
    }

    @Override
    protected ValidationResult validate(Properties properties, TwinEntity twin, FieldValueSelect fieldValue) throws ServiceException {
        //todo - check that additional option conditions are met
        if (fieldValue.getOptions() != null && fieldValue.getOptions().size() > 1 && !allowMultiply(properties)) {
            return new ValidationResult(false, fieldValue.getTwinClassField().logNormal() + " multiply options are not allowed");
        }
        UUID fieldListId = dataListId.extract(properties);
        dataListOptionService.reloadOptionsOnDataListAbsent(fieldValue.getOptions());
        var ret = new ValidationResult(true);
        for (var option : fieldValue.getOptions()) {
            if (!option.getDataListId().equals(fieldListId)) {
                ret
                        .setValid(false)
                        .addMessage(fieldValue.getTwinClassField().logNormal() + " optionId[" + option.getId() + "] is not valid for list[" + fieldListId + "]");
            }
        }
        return ret;
    }

    public UUID checkOptionAllowed(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        return dataListOptionEntity.getId();
    }

    protected boolean allowMultiply(Properties properties) {
        return true;
    }

    public static final String LIST_SPLITTER = "<@2@>";
    public static final String EXTERNAL_ID_PREFIX = "#externalId=";

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twinEntity = twinField.getTwin();
        twinService.loadTwinFields(twinEntity);
        List<TwinFieldDataListEntity> twinFieldDataListEntityList = twinEntity.getTwinFieldDatalistKit().getGrouped(twinField.getTwinClassField().getId());
        FieldValueSelect ret = new FieldValueSelect(twinField.getTwinClassField());
        if (twinFieldDataListEntityList != null)
            for (TwinFieldDataListEntity twinFieldDataListEntity : twinFieldDataListEntityList) {
                ret.add(twinFieldDataListEntity.getDataListOption());
            }
        return ret;
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchList search) throws ServiceException {
        return TwinSpecification.checkFieldList(search);
    }

    public static UUID getDataListId(Properties properties) throws ServiceException {
        return dataListId.extract(properties);
    }

    public static Set<UUID> getDataListOptionIds(Properties properties) {
        return dataListOptionIds.extract(properties);
    }

    public static Set<UUID> getDataListOptionExcludeIds(Properties properties) {
        return dataListOptionExcludeIds.extract(properties);
    }

    public static Set<UUID> getDataListSubsetIds(Properties properties) {
        return dataListSubsetIds.extract(properties);
    }

    public static Set<UUID> getDataListSubsetExcludeIds(Properties properties) {
        return dataListSubsetIdExcludeIds.extract(properties);
    }
}
