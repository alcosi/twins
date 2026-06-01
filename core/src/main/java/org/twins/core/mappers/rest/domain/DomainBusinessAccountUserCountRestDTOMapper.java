package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserCountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainBusinessAccountUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.domain.DomainBusinessAccountUserService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainBusinessAccountUserMode.class)
public class DomainBusinessAccountUserCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DomainBusinessAccountUserEntity>, DomainBusinessAccountUserCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.DomainBusinessAccountUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final DomainBusinessAccountUserService domainBusinessAccountUserService;

    @Override
    public void map(CountResult<DomainBusinessAccountUserEntity> src, DomainBusinessAccountUserCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setUserId(entity.getUserId())
                .setBusinessAccountId(entity.getBusinessAccountId())
                .setCount(src.getCount());
        if (mapperContext.hasModeButNot(UserMode.DomainBusinessAccountUser2UserMode.HIDE)) {
            domainBusinessAccountUserService.loadUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DomainBusinessAccountUser2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.HIDE)) {
            domainBusinessAccountUserService.loadBusinessAccount(entity);
            businessAccountDTOMapper.convertOrPostpone(entity.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DomainBusinessAccountUserEntity>> srcCollection, MapperContext mapperContext) throws Exception {
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).toList();
        if (mapperContext.hasModeButNot(UserMode.DomainBusinessAccountUser2UserMode.HIDE)) {
            domainBusinessAccountUserService.loadUser(entityCollection);
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.HIDE)) {
            domainBusinessAccountUserService.loadBusinessAccount(entityCollection);
        }
    }
}
