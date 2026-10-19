package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserCountDTOv1;
import org.twins.core.enums.sort.DomainBusinessAccountUserGroupField;
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
public class DomainBusinessAccountUserCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DomainBusinessAccountUserEntity, DomainBusinessAccountUserGroupField>, DomainBusinessAccountUserCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.DomainBusinessAccountUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final DomainBusinessAccountUserService domainBusinessAccountUserService;

    @Override
    public void map(CountResult<DomainBusinessAccountUserEntity, DomainBusinessAccountUserGroupField> src, DomainBusinessAccountUserCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setUserId(entity.getUserId())
                .setBusinessAccountId(entity.getBusinessAccountId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, UserMode.DomainBusinessAccountUser2UserMode.HIDE, src, DomainBusinessAccountUserGroupField.userId)) {
            domainBusinessAccountUserService.loadUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DomainBusinessAccountUser2UserMode.SHORT)));
        }
        if (needLoad(mapperContext, BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.HIDE, src, DomainBusinessAccountUserGroupField.businessAccountId)) {
            domainBusinessAccountUserService.loadBusinessAccount(entity);
            businessAccountDTOMapper.convertOrPostpone(entity.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DomainBusinessAccountUserEntity, DomainBusinessAccountUserGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, UserMode.DomainBusinessAccountUser2UserMode.HIDE, someCount, DomainBusinessAccountUserGroupField.userId)) {
            domainBusinessAccountUserService.loadUser(entityCollection);
        }
        if (needLoad(mapperContext, BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.HIDE, someCount, DomainBusinessAccountUserGroupField.businessAccountId)) {
            domainBusinessAccountUserService.loadBusinessAccount(entityCollection);
        }
    }
}
