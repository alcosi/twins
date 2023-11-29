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
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDataListRepository;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.EntitiesChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.datalist.DataListService;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
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
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueSelect value, EntitiesChangesCollector entitiesChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().isRequired() && CollectionUtils.isEmpty(value.options()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (value.options() != null && value.options().size() > 1 && !allowMultiply(properties))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply options are not allowed");
        UUID fieldListId = listUUID.extract(properties);
        List<DataListOptionEntity> dataListOptionEntityList = dataListOptionRepository.findByIdIn(value.options().stream().map(DataListOptionEntity::getId).toList());
        Map<UUID, TwinFieldDataListEntity> storedOptions = null;
        if (twinFieldEntity.getId() != null) //not new field
            storedOptions = twinFieldDataListRepository.findByTwinFieldId(twinFieldEntity.getId()).stream().collect(Collectors.toMap(TwinFieldDataListEntity::getDataListOptionId, Function.identity()));
        else
            twinFieldEntity.setId(UUID.randomUUID()); // we have to generate id here, because TwinFieldDataListEntity is linked to TwinFieldEntity by FK
        for (DataListOptionEntity dataListOptionEntity : dataListOptionEntityList) {
            if (!dataListOptionEntity.getDataListId().equals(fieldListId))
                throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_IS_NOT_VALID_FOR_LIST, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " optionId[" + dataListOptionEntity.getId() + "] is not valid for list[" + fieldListId + "]");
            if (storedOptions == null) { // no values were saved before
                entitiesChangesCollector.add(new TwinFieldDataListEntity()
                        .setTwinFieldId(twinFieldEntity.getId())
                        .setDataListOptionId(checkOptionAllowed(twinFieldEntity, dataListOptionEntity)));
            } else if (!storedOptions.containsKey(dataListOptionEntity.getId())) { // new option value
                entitiesChangesCollector.add(new TwinFieldDataListEntity()
                        .setTwinFieldId(twinFieldEntity.getId())
                        .setDataListOptionId(checkOptionAllowed(twinFieldEntity, dataListOptionEntity)));
            } else {
                storedOptions.remove(dataListOptionEntity.getId()); // option is already saved
            }
        }
        if (storedOptions != null && CollectionUtils.isNotEmpty(storedOptions.entrySet())) // old values must be deleted
            entitiesChangesCollector.deleteAll(TwinFieldDataListEntity.class, storedOptions.values().stream().map(TwinFieldDataListEntity::getId).toList());
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
