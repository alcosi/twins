package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.datalist.DataListService;

import java.util.*;

@Slf4j
public abstract class FieldTyperList extends FieldTyper<FieldDescriptor, FieldValueSelect> {
    @Autowired
    DataListOptionRepository dataListOptionRepository;
    @Autowired
    DataListRepository dataListRepository;
    @Autowired
    EntitySmartService entitySmartService;
    @Lazy
    @Autowired
    DataListService dataListService;

    @FeaturerParam(name = "listUUID", description = "")
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUID("listUUID");

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueSelect value) throws ServiceException {
        if (twinFieldEntity.twinClassField().isRequired() && CollectionUtils.isEmpty(value.options()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.twinClassField().logShort() + " is required");
        if (value.options() != null && value.options().size() > 1 && !allowMultiply(properties))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, twinFieldEntity.twinClassField().logShort() + " multiply options are not allowed");
        List<String> ret = new ArrayList<>();
        UUID fieldListId = listUUID.extract(properties);
        for (DataListOptionEntity dataListOptionEntity : value.options()) {
            DataListOptionEntity dbEntity = entitySmartService.findById(dataListOptionEntity.getId(), "dataListOptionId", dataListOptionRepository, EntitySmartService.FindMode.ifEmptyThrows);
            if (!dbEntity.getDataListId().equals(fieldListId))
                throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_IS_NOT_VALID_FOR_LIST, twinFieldEntity.twinClassField().logShort() + " optionId[" + dataListOptionEntity.getId() + "] is not valid for list[" + fieldListId + "]");
            ret.add(checkOptionAllowed(twinFieldEntity, dataListOptionEntity).toString());
        }
        return StringUtils.join(ret, LIST_SPLITTER);
    }

    public UUID checkOptionAllowed(TwinFieldEntity twinFieldEntity, DataListOptionEntity dataListOptionEntity) throws ServiceException {
        return dataListOptionEntity.getId();
    }

    protected boolean allowMultiply(Properties properties) {
        return true;
    }

    public static final String LIST_SPLITTER = "<@2@>";

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity, Object value) {
        FieldValueSelect ret = new FieldValueSelect();
        if (value != null)
            for (String dataListOptionUUID : value.toString().split(LIST_SPLITTER)) {
                try {
                    Optional<DataListOptionEntity> dataListOption = dataListOptionRepository.findById(UUID.fromString(dataListOptionUUID));
                    dataListOption.ifPresent(ret::add);
                } catch (Exception e) {
                    log.error("Can not parse dataListOption uuid[" + dataListOptionUUID + "]");
                }
            }
        return ret;
    }
}
