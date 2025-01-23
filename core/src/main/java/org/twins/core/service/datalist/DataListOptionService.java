package org.twins.core.service.datalist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionService extends EntitySecureFindServiceImpl<DataListOptionEntity> {
    final DataListOptionRepository dataListOptionRepository;
    final AuthService authService;

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
