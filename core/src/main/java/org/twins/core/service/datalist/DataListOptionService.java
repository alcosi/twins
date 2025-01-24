package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.domain.datalist.DataListOptionCreate;
import org.twins.core.domain.datalist.DataListOptionSave;
import org.twins.core.domain.datalist.DataListOptionUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionService extends EntitySecureFindServiceImpl<DataListOptionEntity> {
    final DataListOptionRepository dataListOptionRepository;
    private final I18nService i18nService;
    private final DataListRepository dataListRepository;

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
        return false;
    }

    @Override
    public boolean validateEntity(DataListOptionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListOptionEntity createDataListOption(DataListOptionCreate dataListOptionCreate) throws ServiceException {
        DataListEntity dbDataList = entitySmartService.findById(dataListOptionCreate.getDataListId(), dataListRepository, EntitySmartService.FindMode.ifEmptyThrows);
        DataListOptionEntity dataListOption = new DataListOptionEntity()
                .setDataListId(dataListOptionCreate.getDataListId())
                .setIcon(dataListOptionCreate.getIcon())
                .setOptionI18NId(i18nService.createI18nAndTranslations(I18nType.PERMISSION_NAME, dataListOptionCreate.getNameI18n()).getId())
                .setStatus(DataListOptionEntity.Status.active);
        setAttributes(dbDataList, dataListOption, dataListOptionCreate);
        validateEntityAndThrow(dataListOption, EntitySmartService.EntityValidateMode.beforeSave);
        return dataListOptionRepository.save(dataListOption);
    }

    private void setAttributes(DataListEntity dbDataList, DataListOptionEntity dataListOption, DataListOptionSave dataListOptionSave) {
        dataListOption.setAttribute1value(dataListOptionSave.getAttributes().get(dbDataList.getAttribute1key()));
        dataListOption.setAttribute2value(dataListOptionSave.getAttributes().get(dbDataList.getAttribute2key()));
        dataListOption.setAttribute3value(dataListOptionSave.getAttributes().get(dbDataList.getAttribute3key()));
        dataListOption.setAttribute4value(dataListOptionSave.getAttributes().get(dbDataList.getAttribute4key()));
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListOptionEntity updateDataListOption(DataListOptionUpdate optionUpdate) throws ServiceException {
        DataListOptionEntity dbOption = findEntitySafe(optionUpdate.getId());
        DataListEntity dbDataList = dbOption.getDataList();
        ChangesHelper changesHelper = new ChangesHelper();
        updateDataListOptionIcon(optionUpdate, dbOption, changesHelper);
        updateDataListOptionName(optionUpdate.getNameI18n(), dbOption, changesHelper);
        updateAttributes(dbDataList, dbOption, optionUpdate, changesHelper);
        if (changesHelper.hasChanges()) {
            validateEntityAndThrow(dbOption, EntitySmartService.EntityValidateMode.beforeSave);
            entitySmartService.saveAndLogChanges(dbOption, dataListOptionRepository, changesHelper);
        }
        return dbOption;
    }

    private void updateAttributes(DataListEntity dataList, DataListOptionEntity option, DataListOptionUpdate optionUpdate, ChangesHelper changesHelper) {
        updateDataListOptionAttribute(getAttribute(optionUpdate, dataList.getAttribute1key()), DataListEntity.Fields.attribute1key, option, DataListOptionEntity::getAttribute1value, DataListOptionEntity::setAttribute1value, changesHelper);
        updateDataListOptionAttribute(getAttribute(optionUpdate, dataList.getAttribute2key()), DataListEntity.Fields.attribute2key, option, DataListOptionEntity::getAttribute2value, DataListOptionEntity::setAttribute2value, changesHelper);
        updateDataListOptionAttribute(getAttribute(optionUpdate, dataList.getAttribute3key()), DataListEntity.Fields.attribute3key, option, DataListOptionEntity::getAttribute3value, DataListOptionEntity::setAttribute3value, changesHelper);
        updateDataListOptionAttribute(getAttribute(optionUpdate, dataList.getAttribute4key()), DataListEntity.Fields.attribute4key, option, DataListOptionEntity::getAttribute4value, DataListOptionEntity::setAttribute4value, changesHelper);
    }

    private String getAttribute(DataListOptionUpdate dataListOptionUpdate, String key) {
        return dataListOptionUpdate.getAttributes().get(key);
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

    //todo move *options methods from  DataListService
}
