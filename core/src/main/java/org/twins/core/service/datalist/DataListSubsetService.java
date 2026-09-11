package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.twins.core.dao.datalist.*;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

//Log calls that took more then 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListSubsetService extends EntitySecureFindServiceImpl<DataListSubsetEntity> {
    private final DataListSubsetRepository dataListSubsetRepository;
    private final DataListSubsetOptionRepository dataListSubsetOptionRepository;
    private final DataListOptionRepository dataListOptionRepository;
    private final DataListService dataListService;

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
    public List<DataListSubsetEntity> createDataListSubsets(List<DataListSubsetEntity> dataListSubsets) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListSubsets)) {
            return Collections.emptyList();
        }
        //checks that data list exists and belongs to the current domain
        loadDataLists(dataListSubsets);
        List<DataListSubsetEntity> savedEntities = StreamSupport.stream(saveSafe(dataListSubsets).spliterator(), false).toList();
        return savedEntities;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListSubsetEntity> updateDataListSubsets(List<DataListSubsetEntity> dataListSubsets) throws ServiceException {
        if (CollectionUtils.isEmpty(dataListSubsets)) {
            return Collections.emptyList();
        }
        Kit<DataListSubsetEntity, UUID> dbEntitiesKit = findEntitiesSafe(dataListSubsets.stream().map(DataListSubsetEntity::getId).toList());
        ChangesHelperMulti<DataListSubsetEntity> changes = new ChangesHelperMulti<>();
        List<DataListSubsetEntity> allEntities = new ArrayList<>(dataListSubsets.size());
        for (DataListSubsetEntity entity : dataListSubsets) {
            DataListSubsetEntity dbEntity = dbEntitiesKit.get(entity.getId());
            allEntities.add(dbEntity);
            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByValueIfNotNull(entity.getName(), dbEntity, DataListSubsetEntity::getName, DataListSubsetEntity::setName, DataListSubsetEntity.Fields.name, changesHelper);
            updateEntityFieldByValue(entity.getDescription(), dbEntity, DataListSubsetEntity::getDescription, DataListSubsetEntity::setDescription, DataListSubsetEntity.Fields.description, changesHelper);
            updateEntityFieldByValueIfNotNull(entity.getKey(), dbEntity, DataListSubsetEntity::getKey, DataListSubsetEntity::setKey, DataListSubsetEntity.Fields.key, changesHelper);
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

    public void loadDataLists(DataListSubsetEntity dataListSubsetEntity) throws ServiceException {
        loadDataLists(Collections.singletonList(dataListSubsetEntity));
    }

    public void loadDataLists(Collection<DataListSubsetEntity> dataListSubsetCollection) throws ServiceException {
        Set<UUID> needLoadIds = new HashSet<>();
        for (DataListSubsetEntity dataListSubsetEntity : dataListSubsetCollection) {
            if (dataListSubsetEntity.getDataList() == null && dataListSubsetEntity.getDataListId() != null)
                needLoadIds.add(dataListSubsetEntity.getDataListId());
        }
        if (needLoadIds.isEmpty())
            return;
        Kit<DataListEntity, UUID> dataListsKit = dataListService.findEntitiesSafe(needLoadIds);
        for (DataListSubsetEntity dataListSubsetEntity : dataListSubsetCollection) {
            if (dataListSubsetEntity.getDataList() == null)
                dataListSubsetEntity.setDataList(dataListsKit.get(dataListSubsetEntity.getDataListId()));
        }
    }
}
