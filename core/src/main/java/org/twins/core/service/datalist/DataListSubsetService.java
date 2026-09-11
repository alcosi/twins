package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.datalist.DataListSubsetOptionRepository;
import org.twins.core.dao.datalist.DataListSubsetRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.datalist.DataListSubsetCreate;
import org.twins.core.domain.datalist.DataListSubsetUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

//Log calls that took more then 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListSubsetService extends EntitySecureFindServiceImpl<DataListSubsetEntity> {
    private final DataListSubsetRepository dataListSubsetRepository;
    private final DataListSubsetOptionRepository dataListSubsetOptionRepository;
    private final DataListService dataListService;
    private final I18nService i18nService;
    private final AuthService authService;
    private final UserService userService;

    @Override
    public CrudRepository<DataListSubsetEntity, UUID> entityRepository() {
        return dataListSubsetRepository;
    }

    @Override
    public Function<DataListSubsetEntity, UUID> entityGetIdFunction() {
        return DataListSubsetEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListSubsetEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DataListEntity dataList = dataListService.findEntitySafe(entity.getDataListId());
        return checkDomainAccessDenied(dataList.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(DataListSubsetEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave -> {
                if (entity.getId() == null) {
                    if (dataListSubsetRepository.existsByDataListIdAndKey(entity.getDataListId(), entity.getKey()))
                        throw new ServiceException(ErrorCodeTwins.DATALIST_SUBSET_KEY_IS_NOT_UNIQUE, "data list subset with key[" + entity.getKey() + "] already exists in data list[" + entity.getDataListId() + "]");
                } else {
                    if (dataListSubsetRepository.existsByDataListIdAndKeyAndIdNot(entity.getDataListId(), entity.getKey(), entity.getId()))
                        throw new ServiceException(ErrorCodeTwins.DATALIST_SUBSET_KEY_IS_NOT_UNIQUE, "data list subset with key[" + entity.getKey() + "] already exists in data list[" + entity.getDataListId() + "]");
                }
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListSubsetEntity> createDataListSubsets(List<DataListSubsetCreate> dataListSubsetCreates) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListSubsetCreates)) {
            return Collections.emptyList();
        }
        ApiUser apiUser = authService.getApiUser();
        //checks that data lists exist and belong to the current domain (single batch query)
        Kit<DataListEntity, UUID> dataListsKit = dataListService.findEntitiesSafe(
                dataListSubsetCreates.stream().map(DataListSubsetCreate::getDataListId).collect(Collectors.toSet()));
        List<DataListSubsetEntity> entities = new ArrayList<>(dataListSubsetCreates.size());
        for (DataListSubsetCreate create : dataListSubsetCreates) {
            if (!dataListsKit.containsKey(create.getDataListId()))
                throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "unknown data list id[" + create.getDataListId() + "]");
            entities.add(new DataListSubsetEntity()
                    .setDataListId(create.getDataListId())
                    .setKey(create.getKey())
                    .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_SUBSET_NAME, create.getNameI18n()).getId())
                    .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.DATA_LIST_SUBSET_DESCRIPTION, create.getDescriptionI18n()).getId())
                    .setCreatedByUserId(apiUser.getUserId())
                    .setCreatedAt(Timestamp.from(Instant.now())));
        }
        return StreamSupport.stream(saveSafe(entities).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListSubsetEntity> updateDataListSubsets(List<DataListSubsetUpdate> dataListSubsetUpdates) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListSubsetUpdates)) {
            return Collections.emptyList();
        }
        Kit<DataListSubsetEntity, UUID> dbEntitiesKit = findEntitiesSafe(dataListSubsetUpdates.stream().map(DataListSubsetUpdate::getId).toList());
        ChangesHelperMulti<DataListSubsetEntity> changes = new ChangesHelperMulti<>();
        List<DataListSubsetEntity> allEntities = new ArrayList<>(dataListSubsetUpdates.size());
        for (DataListSubsetUpdate update : dataListSubsetUpdates) {
            DataListSubsetEntity dbEntity = dbEntitiesKit.get(update.getId());
            allEntities.add(dbEntity);
            ChangesHelper changesHelper = new ChangesHelper();
            i18nService.updateI18nFieldForEntity(update.getNameI18n(), I18nType.DATA_LIST_SUBSET_NAME, dbEntity, DataListSubsetEntity::getNameI18nId, DataListSubsetEntity::setNameI18nId, DataListSubsetEntity.Fields.nameI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(update.getDescriptionI18n(), I18nType.DATA_LIST_SUBSET_DESCRIPTION, dbEntity, DataListSubsetEntity::getDescriptionI18NId, DataListSubsetEntity::setDescriptionI18NId, DataListSubsetEntity.Fields.descriptionI18NId, changesHelper);
            updateEntityFieldByValueIfNotNull(update.getKey(), dbEntity, DataListSubsetEntity::getKey, DataListSubsetEntity::setKey, DataListSubsetEntity.Fields.key, changesHelper);
            if (changesHelper.hasChanges()) {
                changes.add(dbEntity, changesHelper);
            }
        }
        updateSafe(changes);
        return allEntities;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteDataListSubsets(Set<UUID> dataListSubsetIdList) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListSubsetIdList)) {
            return;
        }
        findEntitiesSafe(dataListSubsetIdList);
        //db fk on data_list_subset_option also cascades, manual cleanup keeps the intent explicit
        dataListSubsetOptionRepository.deleteAllByDataListSubsetIdIn(dataListSubsetIdList);
        deleteSafe(dataListSubsetIdList);
    }

    public void loadDataList(DataListSubsetEntity dataListSubsetEntity) throws ServiceException {
        loadDataList(Collections.singletonList(dataListSubsetEntity));
    }

    public void loadDataList(Collection<DataListSubsetEntity> dataListSubsetCollection) throws ServiceException {
        dataListService.load(
                dataListSubsetCollection,
                DataListSubsetEntity::getDataListId,
                DataListSubsetEntity::getDataList,
                DataListSubsetEntity::setDataList);
    }

    public void loadUser(DataListSubsetEntity dataListSubsetEntity) throws ServiceException {
        loadUser(Collections.singletonList(dataListSubsetEntity));
    }

    public void loadUser(Collection<DataListSubsetEntity> dataListSubsetCollection) throws ServiceException {
        userService.load(
                dataListSubsetCollection,
                DataListSubsetEntity::getCreatedByUserId,
                DataListSubsetEntity::getCreatedByUser,
                DataListSubsetEntity::setCreatedByUser);
    }
}
