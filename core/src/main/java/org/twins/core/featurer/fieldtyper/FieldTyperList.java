package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
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
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUIDTwinsDataListId("listUUID");

    private Set<UUID> dataListOptionIdList;
    private Set<UUID> dataListOptionIdExcludeList;
    private Set<UUID> dataListSubsetIdList;
    private Set<UUID> dataListSubsetIdExcludeList;


    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueSelect value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.getOptions() != null && value.getOptions().size() > 1 && !allowMultiply(properties))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply options are not allowed");
        UUID fieldListId = listUUID.extract(properties);

        List<DataListOptionEntity> dataListOptionEntityList = dataListOptionService.reloadOptionsOnDataListAbsent(value.getOptions());
        for (var option : value.getOptions()) {
            if (!option.getDataListId().equals(fieldListId))
                throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_IS_NOT_VALID_FOR_LIST, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " optionId[" + option.getId() + "] is not valid for list[" + fieldListId + "]");
        }
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
                    .setDataListOptionId(checkOptionAllowed(twin, value.getTwinClassField(), dataListOptionEntity))
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
                        .setDataListOptionId(checkOptionAllowed(twin, value.getTwinClassField(), dataListOptionEntity))
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
                        .setDataListOptionId(checkOptionAllowed(twin, value.getTwinClassField(), dataListOptionEntity))
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
        return Specification.where(TwinSpecification.checkFieldList(search));
    }

}
