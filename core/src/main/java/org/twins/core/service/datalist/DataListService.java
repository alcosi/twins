package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
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

import java.util.*;
import java.util.stream.Collectors;

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
        if (dataListEntity.getOptions() != null)
            return dataListEntity.getOptions();
        dataListEntity.setOptions(findDataListOptionsAsKit(dataListEntity.getId()));
        return dataListEntity.getOptions();
    }

    public void loadDataListOptions(Collection<DataListEntity> dataListEntityCollection) throws ServiceException {
        Kit<DataListEntity, UUID> needLoad = new Kit<>(DataListEntity::getId);
        for (DataListEntity dataListEntity : dataListEntityCollection) {
            if (dataListEntity.getOptions() == null)
                needLoad.add(dataListEntity);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        KitGrouped<DataListOptionEntity, UUID, UUID> optionsKit = new KitGrouped<>(
                findOptionsForDataLists(needLoad.getIdSet()),
                DataListOptionEntity::getId,
                DataListOptionEntity::getDataListId);
        for (DataListEntity dataListEntity : needLoad.getCollection()) {
            dataListEntity.setOptions(new Kit<>(optionsKit.getGrouped(dataListEntity.getId()), DataListOptionEntity::getId));
        }
    }

    public List<DataListOptionEntity> findOptionsForDataLists(Set<UUID> dataListIds) throws ServiceException {
        return authService.getApiUser().isBusinessAccountSpecified() ?
                dataListOptionRepository.findByDataListIdInAndBusinessAccountId(dataListIds, authService.getApiUser().getBusinessAccount().getId()) :
                dataListOptionRepository.findByDataListIdIn(dataListIds);
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
        return entitySmartService.saveAllAndLog(newOptions, dataListOptionRepository);
    }

    public int countByDataListId(UUID listId) {
        return dataListOptionRepository.countByDataListId(listId);
    }

    public List<DataListOptionEntity> findByDataListId(UUID listId) {
        return dataListOptionRepository.findByDataListId(listId);
    }

    public List<DataListOptionEntity> findByDataListIdAndNotUsedInDomain(UUID listId, UUID twinClassFieldId) {
       return dataListOptionRepository.findByDataListIdAndNotUsedInDomain(listId, twinClassFieldId);
    }

    public List<DataListOptionEntity> findByDataListIdAndNotUsedInBusinessAccount(UUID listId, UUID twinClassFieldId, UUID businessAccountId) {
        return dataListOptionRepository.findByDataListIdAndNotUsedInBusinessAccount(listId, twinClassFieldId, businessAccountId);
    }

    public List<DataListOptionEntity> findByDataListIdAndNotUsedInHead(UUID listId, UUID twinClassFieldId, UUID headTwinId) {
       return dataListOptionRepository.findByDataListIdAndNotUsedInHead(listId, twinClassFieldId, headTwinId);
    }

    //Method for reloading options if dataList is not present in entity;
    public List<DataListOptionEntity> reloadOptionsOnDataListAbsent(List<DataListOptionEntity> options) {
        List<UUID> idsForReload = new ArrayList<>();
        for(var option : options) if(null == option.getDataList() || null == option.getDataListId()) idsForReload.add(option.getId());
        if (!idsForReload.isEmpty()) {
            options.removeIf(o -> idsForReload.contains(o.getId()));
            options.addAll(dataListOptionRepository.findByIdIn(idsForReload));
        }
        return options;
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
        Set<String> optionsForProcessing = options.stream().filter(option -> ObjectUtils.isEmpty(option.getId())).map(DataListOptionEntity::getOption).collect(Collectors.toSet());
        options.removeIf(o -> optionsForProcessing.contains(o.getOption()));
        List<DataListOptionEntity> processedOptions = processNewOptions(dataListId, optionsForProcessing, businessAccountId);
        options.addAll(processedOptions);
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

