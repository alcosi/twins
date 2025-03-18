package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KeyUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nType;
import org.twins.core.service.i18n.I18nService;
import org.cambium.service.EntitySmartService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.datalist.DataListAttribute;
import org.twins.core.domain.datalist.DataListSave;
import org.twins.core.domain.datalist.DataListUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInHead;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class DataListService extends TwinsEntitySecureFindService<DataListEntity> {
    final DataListRepository dataListRepository;
    final DataListOptionRepository dataListOptionRepository;
    final EntitySmartService entitySmartService;
    final TwinClassFieldService twinClassFieldService;
    final FeaturerService featurerService;
    final CacheManager cacheManager;

    @Lazy
    final AuthService authService;
    private final I18nService i18nService;

    @Override
    public CrudRepository<DataListEntity, UUID> entityRepository() {
        return dataListRepository;
    }

    @Override
    public Function<DataListEntity, UUID> entityGetIdFunction() {
        return DataListEntity::getId;
    }

    @Override
    public BiFunction<UUID, String, Optional<DataListEntity>> findByDomainIdAndKeyFunction() throws ServiceException {
        return dataListRepository::findByDomainIdAndKey;
    }

    @Override
    public boolean isEntityReadDenied(DataListEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        if (!entity.getDomainId().equals(domain.getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logNormal() + " is not allowed in " + domain.logShort());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(DataListEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        switch (entityValidateMode) {
            case beforeSave -> {
                if (entity.getId() == null) {
                    if (dataListRepository.existsByDomainIdAndKey(apiUser.getDomainId(), entity.getKey()))
                        throw new ServiceException(ErrorCodeTwins.DATALIST_NAME_IS_NOT_UNIQUE, "data list with key[" + entity.getKey() + "] already exists in domain[" + apiUser.getDomainId() + "]");
                } else {
                    if (dataListRepository.existsByDomainIdAndKeyAndIdNot(apiUser.getDomainId(), entity.getKey(), entity.getId()))
                        throw new ServiceException(ErrorCodeTwins.DATALIST_NAME_IS_NOT_UNIQUE, "data list with key[" + entity.getKey() + "] already exists in domain[" + apiUser.getDomainId() + "]");
                }
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListEntity createDataList(DataListSave dataListSave) throws ServiceException {
        DataListEntity dataListEntity = new DataListEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(dataListSave.getKey(), ErrorCodeTwins.DATALIST_KEY_INCORRECT))
                .setDomainId(authService.getApiUser().getDomainId())
                .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_NAME, dataListSave.getNameI18n()).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_DESCRIPTION, dataListSave.getDescriptionI18n()).getId())
                .setCreatedAt(Timestamp.from(Instant.now()));
        setAttributes(dataListEntity, dataListSave);
        return saveSafe(dataListEntity);
    }

    private void setAttributes(DataListEntity dataList, DataListSave dataListSave) throws ServiceException {
        if (dataListSave.getAttribute1() != null)
            dataList.setAttribute1nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_OPTION_VALUE, dataListSave.getAttribute1().getAttributeI18n()).getId());
        if (dataListSave.getAttribute2() != null)
            dataList.setAttribute2nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_OPTION_VALUE, dataListSave.getAttribute2().getAttributeI18n()).getId());
        if (dataListSave.getAttribute3() != null)
            dataList.setAttribute3nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_OPTION_VALUE, dataListSave.getAttribute3().getAttributeI18n()).getId());
        if (dataListSave.getAttribute4() != null)
            dataList.setAttribute4nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_OPTION_VALUE, dataListSave.getAttribute4().getAttributeI18n()).getId());
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListEntity updateDataList(DataListUpdate dataListUpdate) throws ServiceException {
        DataListEntity dbDataListEntity = findEntitySafe(dataListUpdate.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateDataListKey(dataListUpdate, dbDataListEntity, changesHelper);
        updateDataListName(dataListUpdate.getNameI18n(), dbDataListEntity, changesHelper);
        updateDataListDescription(dataListUpdate.getDescriptionI18n(), dbDataListEntity, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute1(), DataListEntity.Fields.attribute1key, dbDataListEntity, DataListEntity::getAttribute1key, DataListEntity::setAttribute1key, changesHelper);
        updateDataListAttributeI18n(dataListUpdate.getAttribute1(), DataListEntity.Fields.attribute1nameI18nId, dbDataListEntity, DataListEntity::getAttribute1nameI18nId, DataListEntity::setAttribute1nameI18nId, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute2(), DataListEntity.Fields.attribute2key, dbDataListEntity, DataListEntity::getAttribute2key, DataListEntity::setAttribute2key, changesHelper);
        updateDataListAttributeI18n(dataListUpdate.getAttribute2(), DataListEntity.Fields.attribute2nameI18nId, dbDataListEntity, DataListEntity::getAttribute2nameI18nId, DataListEntity::setAttribute2nameI18nId, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute3(), DataListEntity.Fields.attribute3key, dbDataListEntity, DataListEntity::getAttribute3key, DataListEntity::setAttribute3key, changesHelper);
        updateDataListAttributeI18n(dataListUpdate.getAttribute3(), DataListEntity.Fields.attribute3nameI18nId, dbDataListEntity, DataListEntity::getAttribute3nameI18nId, DataListEntity::setAttribute3nameI18nId, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute4(), DataListEntity.Fields.attribute4key, dbDataListEntity, DataListEntity::getAttribute4key, DataListEntity::setAttribute4key, changesHelper);
        updateDataListAttributeI18n(dataListUpdate.getAttribute4(), DataListEntity.Fields.attribute4nameI18nId, dbDataListEntity, DataListEntity::getAttribute4nameI18nId, DataListEntity::setAttribute4nameI18nId, changesHelper);
        dbDataListEntity.setUpdatedAt(Timestamp.from(Instant.now()));
        return updateSafe(dbDataListEntity, changesHelper);
    }

    private void updateDataListKey(DataListSave dataListSave, DataListEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        dataListSave.setKey(KeyUtils.lowerCaseNullFriendly(dataListSave.getKey(), ErrorCodeTwins.DATALIST_KEY_INCORRECT));
        if (!changesHelper.isChanged(DataListEntity.Fields.key, dbEntity.getKey(), dataListSave.getKey()))
            return;
        dbEntity.setKey(dataListSave.getKey());
    }

    private void updateDataListName(I18nEntity nameI18n, DataListEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbEntity.getNameI18nId() != null)
            nameI18n.setId(dbEntity.getNameI18nId());
        i18nService.saveTranslations(I18nType.DATA_LIST_NAME, nameI18n);
        if (changesHelper.isChanged(DataListEntity.Fields.nameI18nId, dbEntity.getNameI18nId(), nameI18n.getId()))
            dbEntity.setNameI18nId(nameI18n.getId());
    }

    private void updateDataListDescription(I18nEntity descriptionI18n, DataListEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.DATA_LIST_DESCRIPTION, descriptionI18n);
        if (changesHelper.isChanged(DataListEntity.Fields.descriptionI18NId, dbEntity.getDescriptionI18NId(), descriptionI18n.getId()))
            dbEntity.setDescriptionI18NId(descriptionI18n.getId());
    }

    private void updateDataListAttributeKey(DataListAttribute attribute, String fieldName, DataListEntity dbEntity, Function<DataListEntity, String> getAttributeKey, BiConsumer<DataListEntity, String> setAttributeKey, ChangesHelper changesHelper) throws ServiceException {
        if (attribute == null || attribute.getKey() == null)
            return;
        String key = getAttributeKey.apply(dbEntity);
        if (!changesHelper.isChanged(fieldName, key, attribute.getKey()))
            return;
        setAttributeKey.accept(dbEntity, attribute.getKey());
    }

    private void updateDataListAttributeI18n(DataListAttribute attribute, String fieldName, DataListEntity dbEntity, Function<DataListEntity, UUID> getAttribute, BiConsumer<DataListEntity, UUID> setAttribute, ChangesHelper changesHelper) throws ServiceException {
        if (attribute == null || attribute.getAttributeI18n() == null)
            return;
        UUID currentId = getAttribute.apply(dbEntity);
        if (currentId != null)
            attribute.getAttributeI18n().setId(currentId);
        i18nService.saveTranslations(I18nType.DATA_LIST_OPTION_VALUE, attribute.getAttributeI18n());
        UUID newId = attribute.getAttributeI18n().getId();
        if (changesHelper.isChanged(fieldName, currentId, newId)) {
            setAttribute.accept(dbEntity, newId);
        }
    }

//    private void updateDataListAttribute1I18n(I18nEntity attribute1I18n, DataListEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
//        if (attribute1I18n == null)
//            return;
//        if (dbEntity.getAttribute1nameI18nId() != null)
//            attribute1I18n.setId(dbEntity.getAttribute1nameI18nId());
//        i18nService.saveTranslations(I18nType.DATA_LIST_OPTION_VALUE, attribute1I18n);
//        if (changesHelper.isChanged(DataListEntity.Fields.attribute1nameI18nId, dbEntity.getAttribute1nameI18nId(), attribute1I18n.getId()))
//            dbEntity.setAttribute1nameI18nId(attribute1I18n.getId());
//    }

    //todo cache it
    public void loadDataListOptions(DataListEntity dataListEntity) throws ServiceException {
        loadDataListOptions(Collections.singletonList(dataListEntity));
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
        for (DataListEntity dataListEntity : needLoad.getCollection())
            dataListEntity.setOptions(new Kit<>(optionsKit.getGrouped(dataListEntity.getId()), DataListOptionEntity::getId));
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
        for (var option : options)
            if (null == option.getDataList() || null == option.getDataListId()) idsForReload.add(option.getId());
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

