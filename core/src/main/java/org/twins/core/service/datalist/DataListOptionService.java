package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nType;
import org.twins.core.service.i18n.I18nService;
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
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.datalist.DataListOptionCreate;
import org.twins.core.domain.datalist.DataListOptionUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public DataListOptionEntity createDataListOptions(DataListOptionCreate create) throws ServiceException {
        return createDataListOptions(List.of(create)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListOptionEntity> createDataListOptions(List<DataListOptionCreate> dataListOptionCreates) throws ServiceException {
        if (dataListOptionCreates.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataListOptionEntity> optionsToSave = new ArrayList<>();

        for (DataListOptionCreate dataListOptionCreate : dataListOptionCreates) {
            DataListEntity dataList = dataListService.findEntitySafe(dataListOptionCreate.getDataListId());
            loadDataListAttributeAccessors(dataList);

            DataListOptionEntity dataListOption = new DataListOptionEntity()
                    .setDataListId(dataListOptionCreate.getDataListId())
                    .setIcon(dataListOptionCreate.getIcon())
                    .setOptionI18NId(i18nService.createI18nAndTranslations(
                            I18nType.PERMISSION_NAME,
                            dataListOptionCreate.getNameI18n()).getId())
                    .setStatus(DataListOptionEntity.Status.active);

            createAttributes(dataList, dataListOption, dataListOptionCreate.getAttributes());

            validateEntityAndThrow(dataListOption, EntitySmartService.EntityValidateMode.beforeSave);
            optionsToSave.add(dataListOption);
        }

        List<DataListOptionEntity> result = new ArrayList<>();
        entityRepository().saveAll(optionsToSave).forEach(result::add);
        return result;
    }

    private void createAttributes(DataListEntity dataList, DataListOptionEntity dataListOption, Map<String, String> attributes) throws ServiceException {
        if (emptyAttributes(dataList))
            return;
        String attributeValue;
        for (var attributeAccessor : dataList.getAttributes().entrySet()) {
            attributeValue = getAttributeValueSafe(attributes, attributeAccessor.getKey());
            attributeAccessor.getValue().setter().accept(dataListOption, attributeValue);
        }
    }

    private static String getAttributeValueSafe(Map<String, String> optionAttributes, String attributeKey) throws ServiceException {
        if (StringUtils.isEmpty(attributeKey))
            return null; //no attribute configured
        if (MapUtils.isEmpty(optionAttributes) || !optionAttributes.containsKey(attributeKey))
            throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_INVALID_ATTRIBUTE, "Incorrect data list option attribute[" + attributeKey + "]");
        return optionAttributes.get(attributeKey);
    }

    private boolean emptyAttributes(DataListEntity dataListEntity) {
        loadDataListAttributeAccessors(dataListEntity);
        return MapUtils.isEmpty(dataListEntity.getAttributes());
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListOptionEntity updateDataListOptions(DataListOptionUpdate update) throws ServiceException {
        return updateDataListOptions(List.of(update)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListOptionEntity> updateDataListOptions(List<DataListOptionUpdate> optionUpdates) throws ServiceException {
        if (optionUpdates.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<DataListOptionEntity> changes = new ChangesHelperMulti<>();
        List<DataListOptionEntity> allEntities = new ArrayList<>(optionUpdates.size());

        for (DataListOptionUpdate update : optionUpdates) {
            DataListOptionEntity dbOption = findEntitySafe(update.getId());
            allEntities.add(dbOption);

            DataListEntity dbDataList = dbOption.getDataList();
            loadDataListAttributeAccessors(dbDataList);

            ChangesHelper changesHelper = new ChangesHelper();
            updateDataListOptionIcon(update, dbOption, changesHelper);
            updateDataListOptionStatus(update.getStatus(), dbOption, changesHelper);
            updateDataListOptionName(update.getNameI18n(), dbOption, changesHelper);
            updateAttributes(dbDataList, dbOption, update.getAttributes(), changesHelper);
            updateExternalId(dbOption, update.getExternalId(), changesHelper);

            changes.add(dbOption, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }


    public void updateExternalId(DataListOptionEntity dbOption, String newExternalId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(DataListOptionEntity.Fields.externalId, dbOption.getExternalId(), newExternalId))
            return;
        dbOption.setExternalId(newExternalId);
    }

    private void updateAttributes(DataListEntity dataList, DataListOptionEntity option, Map<String, String> attributes, ChangesHelper changesHelper) {
        if (emptyAttributes(dataList) || MapUtils.isEmpty(attributes))
            return;
        String attributeValue;
        for (var attributeAccessor : dataList.getAttributes().entrySet()) {
            if (StringUtils.isEmpty(attributeAccessor.getKey()))
                continue;
            if (MapUtils.isEmpty(attributes) || !attributes.containsKey(attributeAccessor.getKey()))
                continue;
            attributeValue = attributes.get(attributeAccessor.getKey());
            updateDataListOptionAttribute(attributeValue, attributeAccessor.getKey(), option, attributeAccessor.getValue().getter(), attributeAccessor.getValue().setter(), changesHelper);
        }
    }

    private void loadDataListAttributeAccessors(DataListEntity dataListEntity) {
        if (dataListEntity.getAttributes() != null)
            return;
        Map<String, DataListOptionEntity.AttributeAccessor> attributes = new HashMap<>();
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute1key()))
            attributes.put(dataListEntity.getAttribute1key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute1value, DataListOptionEntity::setAttribute1value));
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute2key()))
            attributes.put(dataListEntity.getAttribute2key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute2value, DataListOptionEntity::setAttribute2value));
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute3key()))
            attributes.put(dataListEntity.getAttribute3key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute3value, DataListOptionEntity::setAttribute3value));
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute4key()))
            attributes.put(dataListEntity.getAttribute4key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute4value, DataListOptionEntity::setAttribute4value));
        dataListEntity.setAttributes(attributes);
    }

    private void updateDataListOptionIcon(DataListOptionUpdate optionUpdate, DataListOptionEntity dbEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(DataListOptionEntity.Fields.icon, dbEntity.getIcon(), optionUpdate.getIcon()))
            return;
        dbEntity.setIcon(optionUpdate.getIcon());
    }

    private void updateDataListOptionStatus(DataListOptionEntity.Status status, DataListOptionEntity dbEntity, ChangesHelper changesHelper) {
        if (changesHelper.isChanged(DataListOptionEntity.Fields.status, dbEntity.getStatus(), status))
            dbEntity.setStatus(status);
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
