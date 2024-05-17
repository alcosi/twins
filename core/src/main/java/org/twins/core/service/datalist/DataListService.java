package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInHead;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListService extends EntitySecureFindServiceImpl<DataListEntity> {
    final DataListRepository dataListRepository;
    final DataListOptionRepository dataListOptionRepository;
    final EntitySmartService entitySmartService;
    final TwinClassFieldService twinClassFieldService;
    final FeaturerService featurerService;
    final CacheManager cacheManager;

    @Lazy
    final AuthService authService;

    @Override
    public CrudRepository<DataListEntity, UUID> entityRepository() {
        return dataListRepository;
    }

    @Override
    public boolean isEntityReadDenied(DataListEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(DataListEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<DataListEntity> findDataLists(List<UUID> uuidLists) throws ServiceException {
        List<DataListEntity> dataListEntityList = null;
        ApiUser apiUser = authService.getApiUser();
        if (CollectionUtils.isNotEmpty(uuidLists)) {
            dataListEntityList = dataListRepository.findByDomainIdAndIdIn(apiUser.getDomain().getId(), uuidLists);
        } else
            dataListEntityList = dataListRepository.findByDomainId(apiUser.getDomain().getId());
        return dataListEntityList;
    }

    public DataListEntity findDataListByKey(ApiUser apiUser, String dataListKey) throws ServiceException {
        DataListEntity dataListEntity = dataListRepository.findByDomainIdAndKey(apiUser.getDomain().getId(), dataListKey);
        if (dataListEntity == null)
            throw new ServiceException(ErrorCodeTwins.DATALIST_LIST_UNKNOWN, "unknown data_list_key[" + dataListKey + "]");
        return dataListEntity;
    }

    public List<DataListOptionEntity> findDataListOptions(UUID dataListId) throws ServiceException {
        return authService.getApiUser().isBusinessAccountSpecified() ?
                dataListOptionRepository.findByDataListIdAndBusinessAccountId(dataListId, authService.getApiUser().getBusinessAccount().getId()) :
                dataListOptionRepository.findByDataListId(dataListId);
    }

    //todo cache it
    public Kit<DataListOptionEntity, UUID> findDataListOptionsAsKit(UUID dataListId) throws ServiceException {
        return new Kit<>(findDataListOptions(dataListId), DataListOptionEntity::getId);
    }

    public Kit<DataListOptionEntity, UUID> loadDataListOptions(DataListEntity dataListEntity) throws ServiceException {
        if (dataListEntity.getOptions() != null) return dataListEntity.getOptions();
        dataListEntity.setOptions(findDataListOptionsAsKit(dataListEntity.getId()));
        return dataListEntity.getOptions();
    }

    public DataListEntity findDataListOptionsSharedInHead(UUID twinClassFieldId, UUID headTwinId) throws ServiceException {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findEntitySafe(twinClassFieldId);
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperSharedSelectInHead))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not shared in head");
        return ((FieldTyperSharedSelectInHead) fieldTyper).getDataListWithValidOption(twinClassFieldEntity, headTwinId);
    }

    public DataListOptionEntity findDataListOption(UUID dataListOptionId) throws ServiceException {
        DataListOptionEntity dataListOptionEntity = entitySmartService.findById(dataListOptionId, dataListOptionRepository, EntitySmartService.FindMode.ifEmptyThrows);
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.isBusinessAccountSpecified()
                && dataListOptionEntity.getBusinessAccountId() != null
                && !dataListOptionEntity.getBusinessAccountId().equals(apiUser.getBusinessAccount().getId()))
            throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_IS_NOT_VALID_FOR_BUSINESS_ACCOUNT, dataListOptionEntity.logShort() + " is not valid for " + apiUser.getBusinessAccount().logShort());
        return dataListOptionEntity;
    }

    public Kit<DataListOptionEntity, UUID> findDataListOptionsByIds(Collection<UUID> dataListOptionIdSet) throws ServiceException {
        List<DataListOptionEntity> dataListOptionEntityList;
        if (authService.getApiUser().isBusinessAccountSpecified())
            dataListOptionEntityList = dataListOptionRepository.findByIdInAndBusinessAccountId(dataListOptionIdSet, authService.getApiUser().getBusinessAccount().getId());
        else
            dataListOptionEntityList = dataListOptionRepository.findByIdIn(dataListOptionIdSet);
        return new Kit<>(dataListOptionEntityList, DataListOptionEntity::getId);
    }

    public void forceDeleteOptions(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> optionsToDelete = dataListOptionRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(optionsToDelete, dataListOptionRepository);
    }


    public Iterable<DataListOptionEntity> saveOptions(List<DataListOptionEntity> newOptions) {
        for (DataListOptionEntity option : newOptions) if (option.getId() == null) option.setId(UUID.randomUUID());
        return entitySmartService.saveAllAndLog(newOptions, dataListOptionRepository);
    }

    public DataListOptionEntity checkOptionsExists(UUID dataListId, String optionName, UUID businessAccountId) {
        List<DataListOptionEntity> foundOptions;
        if (businessAccountId != null)
            foundOptions = dataListOptionRepository.findOptionForBusinessAccount(dataListId, businessAccountId, optionName.trim(), PageRequest.of(0, 1));
        else
            foundOptions = dataListOptionRepository.findOptionOutOfBusinessAccount(dataListId, optionName.trim(), PageRequest.of(0, 1));

        if (CollectionUtils.isNotEmpty(foundOptions))
            return foundOptions.get(0);

        return null;
    }

    public List<DataListOptionEntity> processNewOptions(UUID dataListId, List<DataListOptionEntity> options, UUID businessAccountId) {
        List<DataListOptionEntity> newOptions = options.stream().filter(option -> ObjectUtils.isEmpty(option.getId())).toList();
        List<DataListOptionEntity> optionsExists = new ArrayList<>();
        List<DataListOptionEntity> optionsForSave = new ArrayList<>();
        for (DataListOptionEntity option : newOptions) {
            DataListOptionEntity foundedOption = checkOptionsExists(dataListId, option.getOption(), businessAccountId);
            if (null != foundedOption) optionsExists.add(foundedOption);
            else optionsForSave.add(option);
        }
        Iterable<DataListOptionEntity> savedOptions = saveOptions(optionsForSave);
        savedOptions.forEach(optionsExists::add);
        if (CollectionUtils.isNotEmpty(optionsForSave))
            evictOptionsCloudCache(dataListId, businessAccountId);
        options.removeIf(option -> ObjectUtils.isEmpty(option.getId()));
        options.addAll(optionsExists);
        return options;
    }

    public List<DataListOptionEntity> processNewOptions(UUID dataListId, Set<String> newOptions, UUID businessAccountId) {
        List<DataListOptionEntity> optionsExists = new ArrayList<>();
        List<DataListOptionEntity> optionsForSave = new ArrayList<>();
        for (String optionName : newOptions) {
            DataListOptionEntity foundedOption = checkOptionsExists(dataListId, optionName, businessAccountId);
            if (null != foundedOption) optionsExists.add(foundedOption);
            else {
                DataListOptionEntity newOption = new DataListOptionEntity();
                newOption.setOption(optionName);
                newOption.setBusinessAccountId(businessAccountId);
                newOption.setStatus(DataListOptionEntity.Status.active);
                newOption.setDataListId(dataListId);
                optionsForSave.add(newOption);
            }
        }
        Iterable<DataListOptionEntity> savedOptions = saveOptions(optionsForSave);
        savedOptions.forEach(optionsExists::add);
        if (CollectionUtils.isNotEmpty(optionsForSave))
            evictOptionsCloudCache(dataListId, businessAccountId);
        return optionsExists;
    }

    private void evictOptionsCloudCache(UUID dataListId, UUID businessAccountId) {
        Cache cache = cacheManager.getCache(DataListOptionRepository.CACHE_DATA_LIST_OPTIONS);
        if (cache != null)
            cache.evictIfPresent(dataListId);
        if (businessAccountId != null) {
            cache = cacheManager.getCache(DataListOptionRepository.CACHE_DATA_LIST_OPTIONS_WITH_BUSINESS_ACCOUNT);
            if (cache != null)
                cache.evictIfPresent(dataListId + "" + businessAccountId);
        }
    }

}

