package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
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
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListService extends EntitySecureFindServiceImpl<DataListEntity> {
    final DataListRepository dataListRepository;
    final DataListOptionRepository dataListOptionRepository;
    final EntitySmartService entitySmartService;
    final TwinClassFieldService twinClassFieldService;
    final FeaturerService featurerService;
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
    public Kit<DataListOptionEntity> findDataListOptionsAsKit(UUID dataListId) throws ServiceException {
        return new Kit<>(findDataListOptions(dataListId), DataListOptionEntity::getId);
    }

    public Map<UUID, DataListOptionEntity> loadDataListOptions(DataListEntity dataListEntity) throws ServiceException {
        if (dataListEntity.getOptions() != null) return dataListEntity.getOptions().getMap();
        dataListEntity.setOptions(findDataListOptionsAsKit(dataListEntity.getId()));
        return dataListEntity.getOptions().getMap();
    }

    public DataListEntity findDataListOptionsSharedInHead(UUID twinClassFieldId, UUID headTwinId) throws ServiceException {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findEntitySafe(twinClassFieldId);
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperSharedSelectInHead))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not shared in head");
        return ((FieldTyperSharedSelectInHead) fieldTyper).getDataListWithValidOption(twinClassFieldEntity, headTwinId);
    }

    public DataListOptionEntity findDataListOption(UUID dataListOptionId) throws ServiceException {
        return authService.getApiUser().isBusinessAccountSpecified() ?
                findByIdAndBusinessAccount(dataListOptionId, authService.getApiUser().getBusinessAccount().getId(), EntitySmartService.FindMode.ifEmptyThrows) :
                entitySmartService.findById(dataListOptionId, dataListOptionRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }
    private DataListOptionEntity findByIdAndBusinessAccount(UUID uuid, UUID businessAccountId, EntitySmartService.FindMode mode) throws ServiceException {
        Optional<DataListOptionEntity> optional = dataListOptionRepository.findByIdAndBusinessAccountId(uuid, businessAccountId);
        switch (mode) {
            case ifEmptyNull:
                return optional.isEmpty() ? null : optional.get();
            case ifEmptyLogAndNull:
                if (optional.isEmpty()) {
                    log.error(dataListOptionRepository.getClass().getSimpleName() + " can not find entity with id[" + uuid + "]");
                    return null;
                } else
                    return optional.get();
            case ifEmptyThrows:
                if (optional.isEmpty())
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown " + dataListOptionRepository.getClass().getSimpleName() + "[" + uuid + "]");
                return optional.get();
        }
        return null;
    }

    public Map<UUID, DataListOptionEntity> findDataListOptionsMapByIds(Collection<UUID> dataListOptionIdSet) throws ServiceException {
        return EntitySmartService.convertToMap(
                authService.getApiUser().isBusinessAccountSpecified() ?
                        dataListOptionRepository.findByIdInAndBusinessAccountId(dataListOptionIdSet, authService.getApiUser().getBusinessAccount().getId()) :
                        dataListOptionRepository.findByIdIn(dataListOptionIdSet), DataListOptionEntity::getId
        );
    }
}

