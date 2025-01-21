package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNoRelationsProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.auth.AuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinFieldSimpleSearchService extends EntitySecureFindServiceImpl<TwinFieldSimpleEntity> {

    private final AuthService authService;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final TwinSearchService twinSearchService;

    @Override
    public CrudRepository<TwinFieldSimpleEntity, UUID> entityRepository() {
        return twinFieldSimpleRepository;
    }

    @Override
    public Function<TwinFieldSimpleEntity, UUID> entityGetIdFunction() {
        return TwinFieldSimpleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFieldSimpleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFieldSimpleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }


    public List<TwinFieldSimpleNoRelationsProjection> findTwinFieldsSimple(BasicSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if(null == apiUser.getDomainId() || null == search.getHeaderTwinIdList() || null == search.getStatusIdList())
            return new ArrayList<>();
        return twinFieldSimpleRepository.findTwinFieldSimpleEntityProjected(apiUser.getDomainId(), search.getHeaderTwinIdList(), search.getTwinIdExcludeList(), search.getStatusIdList());
    }


}
