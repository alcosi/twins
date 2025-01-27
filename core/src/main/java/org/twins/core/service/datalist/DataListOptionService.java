package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.domain.datalist.DataListOptionCreate;
import org.twins.core.domain.datalist.DataListOptionUpdate;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionService extends EntitySecureFindServiceImpl<DataListOptionEntity> {
    final DataListOptionRepository dataListOptionRepository;
    final AuthService authService;
    private final I18nService i18nService;

    @Lazy
    @Autowired
    private DataListService dataListService;

    @Override
    public CrudRepository<DataListOptionEntity, UUID> entityRepository() {
        return dataListOptionRepository;
    }

    @Override
    public Function<DataListOptionEntity, UUID> entityGetIdFunction() {
        return DataListOptionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListOptionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainEntity domain = apiUser.getDomain();
        boolean readDenied = (!entity.getDataList().getDomainId().equals(domain.getId()) || (apiUser.isBusinessAccountSpecified()
                && entity.getBusinessAccountId() != null
                && !entity.getBusinessAccountId().equals(apiUser.getBusinessAccount().getId())));
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in " + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(DataListOptionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListOptionEntity createDataListOption(DataListOptionCreate dataListOptionCreate) throws ServiceException {
        DataListEntity dbDataList = dataListService.findEntitySafe(dataListOptionCreate.getDataListId());
        fillingDataListAttribute(dbDataList);
        DataListOptionEntity dataListOption = new DataListOptionEntity()
                .setDataListId(dataListOptionCreate.getDataListId())
                .setIcon(dataListOptionCreate.getIcon())
                .setOptionI18NId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_NAME, dataListOptionCreate.getNameI18n()).getId())
                .setStatus(DataListOptionEntity.Status.active);
        checkAttributes(dbDataList.getAttributes(), dataListOptionCreate.getAttributes());
        createAttributes(dbDataList, dataListOption, dataListOptionCreate.getAttributes());
        validateEntityAndThrow(dataListOption, EntitySmartService.EntityValidateMode.beforeSave);
        return dataListOptionRepository.save(dataListOption);
    }

    private void createAttributes(DataListEntity dbDataList, DataListOptionEntity dataListOption, Map<String, String> attributes) {
        dataListOption.setAttribute1value(attributes.get(dbDataList.getAttribute1key()));
        dataListOption.setAttribute2value(attributes.get(dbDataList.getAttribute2key()));
        dataListOption.setAttribute3value(attributes.get(dbDataList.getAttribute3key()));
        dataListOption.setAttribute4value(attributes.get(dbDataList.getAttribute4key()));
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListOptionEntity updateDataListOption(DataListOptionUpdate optionUpdate) throws ServiceException {
        DataListOptionEntity dbOption = findEntitySafe(optionUpdate.getId());
        DataListEntity dbDataList = dbOption.getDataList();
        fillingDataListAttribute(dbDataList);
        ChangesHelper changesHelper = new ChangesHelper();
        updateDataListOptionIcon(optionUpdate, dbOption, changesHelper);
        updateDataListOptionName(optionUpdate.getNameI18n(), dbOption, changesHelper);
        checkAttributes(dbDataList.getAttributes(), optionUpdate.getAttributes());
        updateAttributes(dbDataList, dbOption, optionUpdate, changesHelper);
        if (changesHelper.hasChanges()) {
            validateEntityAndThrow(dbOption, EntitySmartService.EntityValidateMode.beforeSave);
            entitySmartService.saveAndLogChanges(dbOption, dataListOptionRepository, changesHelper);
        }
        return dbOption;
    }

    private void updateAttributes(DataListEntity dataList, DataListOptionEntity option, DataListOptionUpdate optionUpdate, ChangesHelper changesHelper) {
        updateDataListOptionAttribute(getAttributeByKey(optionUpdate, dataList.getAttribute1key()), DataListEntity.Fields.attribute1key, option, DataListOptionEntity::getAttribute1value, DataListOptionEntity::setAttribute1value, changesHelper);
        updateDataListOptionAttribute(getAttributeByKey(optionUpdate, dataList.getAttribute2key()), DataListEntity.Fields.attribute2key, option, DataListOptionEntity::getAttribute2value, DataListOptionEntity::setAttribute2value, changesHelper);
        updateDataListOptionAttribute(getAttributeByKey(optionUpdate, dataList.getAttribute3key()), DataListEntity.Fields.attribute3key, option, DataListOptionEntity::getAttribute3value, DataListOptionEntity::setAttribute3value, changesHelper);
        updateDataListOptionAttribute(getAttributeByKey(optionUpdate, dataList.getAttribute4key()), DataListEntity.Fields.attribute4key, option, DataListOptionEntity::getAttribute4value, DataListOptionEntity::setAttribute4value, changesHelper);
    }

    private String getAttributeByKey(DataListOptionUpdate dataListOptionUpdate, String key) {
        return dataListOptionUpdate.getAttributes().get(key);
    }

    private void fillingDataListAttribute(DataListEntity dbDataList) {
        dbDataList.setAttributes(Stream.of(
                        dbDataList.getAttribute1key(),
                        dbDataList.getAttribute2key(),
                        dbDataList.getAttribute3key(),
                        dbDataList.getAttribute4key())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    public void checkAttributes(Set<String> requiredAttrs, Map<String, String> attributes) throws ServiceException {
        for (String attr : requiredAttrs) {
            if (!attributes.containsKey(attr))
                throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_INVALID_ATTRIBUTE, "Incorrect data list option attribute[" + attr + "]");
        }
    }

    private void updateDataListOptionIcon(DataListOptionUpdate optionUpdate, DataListOptionEntity dbEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(DataListOptionEntity.Fields.icon, dbEntity.getIcon(), optionUpdate.getIcon()))
            return;
        dbEntity.setIcon(optionUpdate.getIcon());
    }

    private void updateDataListOptionName(I18nEntity nameI18n, DataListOptionEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbEntity.getOptionI18NId() != null)
            nameI18n.setId(dbEntity.getOptionI18NId());
        i18nService.saveTranslations(I18nType.DATA_LIST_NAME, nameI18n);
        //todo changesHelper for i18n doesn't work
        if (changesHelper.isChanged(DataListEntity.Fields.nameI18nId, dbEntity.getOptionI18NId(), nameI18n.getId()))
            dbEntity.setOptionI18NId(nameI18n.getId());
    }

    private void updateDataListOptionAttribute(String newAttr, String fieldName, DataListOptionEntity dbEntity, Function<DataListOptionEntity, String> getAttr, BiConsumer<DataListOptionEntity, String> setAttr, ChangesHelper changesHelper) {
        if (newAttr == null)
            return;
        String value = getAttr.apply(dbEntity);
        if (!changesHelper.isChanged(fieldName, value, newAttr))
            return;
        setAttr.accept(dbEntity, newAttr);
    }

    //Method for reloading options if dataList is not present in entity;
    public List<DataListOptionEntity> reloadOptionsOnDataListAbsent(List<DataListOptionEntity> options) throws ServiceException {
        List<UUID> idsForReload = new ArrayList<>();
        for (var option : options)
            if (null == option.getDataList() || null == option.getDataListId()) idsForReload.add(option.getId());
        if (!idsForReload.isEmpty()) {
            options.removeIf(o -> idsForReload.contains(o.getId()));
            options.addAll(findEntitiesSafe(idsForReload));
        }
        return options;
    }

    public Kit<DataListOptionEntity, UUID> findDataListOptionsByIds(Collection<UUID> dataListOptionIdSet) throws ServiceException {
        List<DataListOptionEntity> dataListOptionEntityList;
        if (authService.getApiUser().isBusinessAccountSpecified())
            dataListOptionEntityList = dataListOptionRepository.findByIdInAndBusinessAccountId(dataListOptionIdSet, authService.getApiUser().getBusinessAccount().getId());
        else
            dataListOptionEntityList = dataListOptionRepository.findByIdIn(dataListOptionIdSet);
        return new Kit<>(dataListOptionEntityList, DataListOptionEntity::getId);
    }

    //todo move *options methods from  DataListService
}
