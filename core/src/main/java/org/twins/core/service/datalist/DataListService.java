package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionResult;
import org.twins.core.dto.rest.datalist.DataListResult;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSharedSelectInHead;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.pagination.PaginationResult;
import org.twins.core.service.pagination.SimplePagination;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListService extends EntitySecureFindServiceImpl<DataListEntity> {
    final DataListRepository dataListRepository;
    final DataListOptionRepository dataListOptionRepository;
    final DataListRestDTOMapper dataListRestDTOMapper;
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
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
    public void findEntitySafe() {

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

    public Page<DataListOptionEntity> findDataListOptions(UUID dataListId, SimplePagination pagination) throws ServiceException {
        Page<DataListOptionEntity> dataListOptionPage;
        if (authService.getApiUser().isBusinessAccountSpecified()) {
            dataListOptionPage = dataListOptionRepository.findByDataListIdAndBusinessAccountId(dataListId, authService.getApiUser().getBusinessAccount().getId(), PaginationUtils.pageableOffset(pagination));
        } else {
            dataListOptionPage = dataListOptionRepository.findByDataListId(dataListId, PaginationUtils.pageableOffset(pagination));
        }
        return dataListOptionPage;
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

    public DataListOptionResult findDataListOptions(DataListEntity dataListEntity, SimplePagination pagination) throws ServiceException {
        List<DataListOptionEntity> options = null;
        PaginationResult paginationResult = null;
        if (pagination != null) {
            paginationResult = (PaginationResult) new PaginationResult()
                    .setOffset(pagination.getOffset())
                    .setLimit(pagination.getLimit());
        }
        if (dataListEntity.getOptions() != null) {// looks like all options were already loaded
            options = dataListEntity.getOptions().getCollection().stream().skip(pagination.getOffset()).limit(pagination.getLimit()).toList();
            paginationResult.setTotal(dataListEntity.getOptions().getCollection().size());
        } else if (pagination != null) {
            Page<DataListOptionEntity> optionsPage = findDataListOptions(dataListEntity.getId(), pagination);
            paginationResult.setTotal(optionsPage.getTotalElements());
        } else
            options = findDataListOptions(dataListEntity.getId());
        return convertKitInSearchResult(new Kit<>(options, DataListOptionEntity::getId), paginationResult);
    }

    public DataListResult getDataList(DataListEntity dataListEntity, DataListDTOv1 dataListDTO, MapperContext mapperContext) throws Exception {
        DataListOptionResult options = findDataListOptions(dataListEntity, mapperContext.getModePagination(DataListOptionRestDTOMapper.Mode.class));
        dataListDTO.setOptions(dataListOptionRestDTOMapper.convertMap(options.getOptionKit().getMap(), mapperContext)); //todo remove me after gateway support of relateMap of dataListOptions
        dataListRestDTOMapper.convertMapOrPostpone(options.getOptionKit(), dataListDTO, dataListOptionRestDTOMapper, mapperContext, DataListDTOv1::setOptions, DataListDTOv1::setOptionIdList);
        return convertListSearchResult(dataListDTO, options.getPagination());
    }

    private DataListOptionResult convertKitInSearchResult(Kit<DataListOptionEntity, UUID> dataListOptionEntityUUIDKit, PaginationResult pagination) {
        return new DataListOptionResult()
                .setOptionKit(dataListOptionEntityUUIDKit)
                .setPagination(pagination);
    }

    private DataListResult convertListSearchResult(DataListDTOv1 dataList, PaginationResult pagination) {
        return (DataListResult) new DataListResult()
                .setDataList(dataList)
                .setTotal(pagination.getTotal())
                .setOffset(pagination.getOffset())
                .setLimit(pagination.getLimit());
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
}

