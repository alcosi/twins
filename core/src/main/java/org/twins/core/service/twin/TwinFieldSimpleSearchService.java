package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.Permissions;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.twins.core.dao.specifications.twin.TwinFieldSimpleSpecification.checkDomainId;

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



    public List<TwinFieldSimpleNoRelationsProjection> findTwinFieldSimpleValuesByTwinIds(BasicSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Kit<TwinIdNoRelationsProjection, UUID> twinIds = new Kit<>(twinSearchService.findTwins(search, TwinIdNoRelationsProjection.class), TwinIdNoRelationsProjection::id);
        Specification<TwinFieldSimpleEntity> spec = Specification.where(
                checkDomainId(apiUser.getDomainId())
                        .and(CommonSpecification.checkUuidIn(TwinFieldSimpleEntity.Fields.twinId, twinIds.getIdSet(), false, false))
        );
        //https://github.com/spring-projects/spring-data-jpa/pull/430
        return twinFieldSimpleRepository.findBy(spec, q -> q.as(TwinFieldSimpleNoRelationsProjection.class).all());
    }


}
