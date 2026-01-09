package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KeyUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.datalist.DataListAttribute;
import org.twins.core.domain.datalist.DataListCreate;
import org.twins.core.domain.datalist.DataListSave;
import org.twins.core.domain.datalist.DataListUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInHead;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


//Log calls that took more then 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListService extends TwinsEntitySecureFindService<DataListEntity> {
    final DataListRepository dataListRepository;
    final DataListOptionRepository dataListOptionRepository;
    final EntitySmartService entitySmartService;
    final TwinClassFieldService twinClassFieldService;
    final FeaturerService featurerService;
    final DataListOptionService dataListOptionService;

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
    public DataListEntity createDataList(DataListCreate dataListCreate) throws ServiceException {
        DataListEntity dataListEntity = new DataListEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(dataListCreate.getKey(), ErrorCodeTwins.DATALIST_KEY_INCORRECT))
                .setDomainId(authService.getApiUser().getDomainId())
                .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_NAME, dataListCreate.getNameI18n()).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_DESCRIPTION, dataListCreate.getDescriptionI18n()).getId())
                .setExternalId(dataListCreate.getExternalId())
                .setCreatedAt(Timestamp.from(Instant.now()));
        setAttributes(dataListEntity, dataListCreate);

        saveSafe(dataListEntity);

        if (dataListCreate.getDefaultOption() != null) {
            DataListOptionEntity optionEntity = dataListOptionService.createDataListOptions(
                    dataListCreate.getDefaultOption().setDataListId(dataListEntity.getId())
            );

            dataListRepository.updateDefaultOptionId(dataListEntity.getId(), optionEntity.getId());
            dataListEntity
                    .setDefaultDataListOptionId(optionEntity.getId())
                    .setOptions(new Kit<>(List.of(optionEntity), DataListOptionEntity::getId));
        }
        return dataListEntity;
    }

    private void setAttributes(DataListEntity dataList, DataListSave dataListSave) throws ServiceException {
        if (dataListSave.getAttribute1() != null)
            dataList.setAttribute1nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_ATTRIBUTE_NAME, dataListSave.getAttribute1().getAttributeI18n()).getId());
        if (dataListSave.getAttribute2() != null)
            dataList.setAttribute2nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_ATTRIBUTE_NAME, dataListSave.getAttribute2().getAttributeI18n()).getId());
        if (dataListSave.getAttribute3() != null)
            dataList.setAttribute3nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_ATTRIBUTE_NAME, dataListSave.getAttribute3().getAttributeI18n()).getId());
        if (dataListSave.getAttribute4() != null)
            dataList.setAttribute4nameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_ATTRIBUTE_NAME, dataListSave.getAttribute4().getAttributeI18n()).getId());
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListEntity updateDataList(DataListUpdate dataListUpdate) throws ServiceException {
        DataListEntity dbDataListEntity = findEntitySafe(dataListUpdate.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateDataListKey(dataListUpdate, dbDataListEntity, changesHelper);
        i18nService.updateI18nFieldForEntity(dataListUpdate.getNameI18n(), I18nType.DATA_LIST_NAME, dbDataListEntity, DataListEntity::getNameI18nId, DataListEntity::setNameI18nId, DataListEntity.Fields.nameI18nId, changesHelper);
        i18nService.updateI18nFieldForEntity(dataListUpdate.getDescriptionI18n(), I18nType.DATA_LIST_DESCRIPTION, dbDataListEntity, DataListEntity::getDescriptionI18NId, DataListEntity::setDescriptionI18NId, DataListEntity.Fields.descriptionI18NId, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute1(), DataListEntity.Fields.attribute1key, dbDataListEntity, DataListEntity::getAttribute1key, DataListEntity::setAttribute1key, changesHelper);
        i18nService.updateI18nFieldForEntity(Optional.ofNullable(dataListUpdate.getAttribute1()).map(DataListAttribute::getAttributeI18n).orElse(null), I18nType.DATA_LIST_OPTION_VALUE, dbDataListEntity, DataListEntity::getAttribute1nameI18nId, DataListEntity::setAttribute1nameI18nId, DataListEntity.Fields.attribute1nameI18nId, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute2(), DataListEntity.Fields.attribute2key, dbDataListEntity, DataListEntity::getAttribute2key, DataListEntity::setAttribute2key, changesHelper);
        i18nService.updateI18nFieldForEntity(Optional.ofNullable(dataListUpdate.getAttribute2()).map(DataListAttribute::getAttributeI18n).orElse(null), I18nType.DATA_LIST_OPTION_VALUE, dbDataListEntity, DataListEntity::getAttribute2nameI18nId, DataListEntity::setAttribute2nameI18nId, DataListEntity.Fields.attribute2nameI18nId, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute3(), DataListEntity.Fields.attribute3key, dbDataListEntity, DataListEntity::getAttribute3key, DataListEntity::setAttribute3key, changesHelper);
        i18nService.updateI18nFieldForEntity(Optional.ofNullable(dataListUpdate.getAttribute3()).map(DataListAttribute::getAttributeI18n).orElse(null), I18nType.DATA_LIST_OPTION_VALUE, dbDataListEntity, DataListEntity::getAttribute3nameI18nId, DataListEntity::setAttribute3nameI18nId, DataListEntity.Fields.attribute3nameI18nId, changesHelper);
        updateDataListAttributeKey(dataListUpdate.getAttribute4(), DataListEntity.Fields.attribute4key, dbDataListEntity, DataListEntity::getAttribute4key, DataListEntity::setAttribute4key, changesHelper);
        i18nService.updateI18nFieldForEntity(Optional.ofNullable(dataListUpdate.getAttribute4()).map(DataListAttribute::getAttributeI18n).orElse(null), I18nType.DATA_LIST_OPTION_VALUE, dbDataListEntity, DataListEntity::getAttribute4nameI18nId, DataListEntity::setAttribute4nameI18nId, DataListEntity.Fields.attribute4nameI18nId, changesHelper);
        updateExternalId(dbDataListEntity, dataListUpdate.getExternalId(), changesHelper);
        updateEntityFieldByValue(dataListUpdate.getDefaultOptionId(), dbDataListEntity, DataListEntity::getDefaultDataListOptionId, DataListEntity::setDefaultDataListOptionId, DataListEntity.Fields.defaultDataListOptionId, changesHelper);
        dbDataListEntity.setUpdatedAt(Timestamp.from(Instant.now()));
        return updateSafe(dbDataListEntity, changesHelper);
    }

    public void updateExternalId(DataListEntity dbDataListEntity, String newExternalId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(DataListEntity.Fields.externalId, dbDataListEntity.getExternalId(), newExternalId))
            return;
        dbDataListEntity.setExternalId(newExternalId);
    }

    private void updateDataListKey(DataListSave dataListSave, DataListEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        dataListSave.setKey(KeyUtils.lowerCaseNullFriendly(dataListSave.getKey(), ErrorCodeTwins.DATALIST_KEY_INCORRECT));
        if (!changesHelper.isChanged(DataListEntity.Fields.key, dbEntity.getKey(), dataListSave.getKey()))
            return;
        dbEntity.setKey(dataListSave.getKey());
    }

    private void updateDataListAttributeKey(DataListAttribute attribute, String fieldName, DataListEntity dbEntity, Function<DataListEntity, String> getAttributeKey, BiConsumer<DataListEntity, String> setAttributeKey, ChangesHelper changesHelper) throws ServiceException {
        if (attribute == null || attribute.getKey() == null)
            return;
        String key = getAttributeKey.apply(dbEntity);
        if (!changesHelper.isChanged(fieldName, key, attribute.getKey()))
            return;
        setAttributeKey.accept(dbEntity, attribute.getKey());
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
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperSharedSelectInHead))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldEntity.logNormal() + " is not shared in head");
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

}

