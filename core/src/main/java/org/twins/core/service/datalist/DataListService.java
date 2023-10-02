package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInHead;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListService {
    final DataListRepository dataListRepository;
    final DataListOptionRepository dataListOptionRepository;

    final EntitySmartService entitySmartService;
    final TwinClassFieldService twinClassFieldService;
    final FeaturerService featurerService;

    public List<DataListEntity> findDataLists(ApiUser apiUser, List<UUID> uuidLists) {
        if (CollectionUtils.isNotEmpty(uuidLists))
            return dataListRepository.findByDomainIdAndIdIn(apiUser.getDomain().getId(), uuidLists);
        else
            return dataListRepository.findByDomainId(apiUser.getDomain().getId());
    }

    public DataListEntity findDataList(ApiUser apiUser, UUID dataListId) {
        return dataListRepository.findByDomainIdAndId(apiUser.getDomain().getId(), dataListId);
    }

    public DataListEntity findDataListByKey(ApiUser apiUser, String dataListKey) throws ServiceException {
        DataListEntity dataListEntity = dataListRepository.findByDomainIdAndKey(apiUser.getDomain().getId(), dataListKey);
        if (dataListEntity == null)
            throw new ServiceException(ErrorCodeTwins.DATALIST_LIST_UNKNOWN, "unknown data_list_key[" + dataListKey + "]");
        return dataListEntity;
    }

    public List<DataListOptionEntity> findDataListOptions(UUID dataListId) {
        return dataListOptionRepository.findByDataListId(dataListId);
    }

    public DataListEntity findDataListOptionsSharedInHead(UUID twinClassFieldId, UUID headTwinId) throws ServiceException {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findTwinClassField(twinClassFieldId);
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperSharedSelectInHead))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldEntity.logShort() + " is not shared in head");
        return ((FieldTyperSharedSelectInHead)fieldTyper).getDataListWithValidOption(twinClassFieldEntity, headTwinId);
    }

    public DataListOptionEntity findDataListOption(UUID dataListOptionId) throws ServiceException {
        return entitySmartService.findById(dataListOptionId, "dataListOption", dataListOptionRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }
}

