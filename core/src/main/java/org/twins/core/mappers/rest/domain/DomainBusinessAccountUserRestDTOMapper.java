package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainBusinessAccountUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.service.domain.DomainBusinessAccountUserService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainBusinessAccountUserMode.class)
public class DomainBusinessAccountUserRestDTOMapper extends RestSimpleDTOMapper<DomainBusinessAccountUserEntity, DomainBusinessAccountUserDTOv1> {

    @MapperModePointerBinding(modes = UserMode.DomainBusinessAccountUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final DomainBusinessAccountUserService domainBusinessAccountUserService;

    @MapperModePointerBinding(modes = UserGroupMode.DomainBusinessAccountUser2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    @Override
    public void map(DomainBusinessAccountUserEntity src, DomainBusinessAccountUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainBusinessAccountUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setUserId(src.getUserId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setBusinessAccountUserId(src.getBusinessAccountUserId())
                        .setDomainBusinessAccountId(src.getDomainBusinessAccountId())
                        .setDomainUserId(src.getDomainUserId())
                        .setLastActivityAt(src.getLastActivityAt() != null ? src.getLastActivityAt().toLocalDateTime() : null)
                        .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null);
                break;
            case SHORT:
                dst
                        .setUserId(src.getUserId())
                        .setBusinessAccountId(src.getBusinessAccountId());
                break;
        }
        //todo postpone BAU, DBA, DU objects
        if (mapperContext.hasModeButNot(UserMode.DomainBusinessAccountUser2UserMode.HIDE)) {
            dst.setUserId(src.getUserId());
            userRestDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DomainBusinessAccountUser2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.HIDE)) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.DomainBusinessAccountUser2BusinessAccountMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserGroupMode.DomainBusinessAccountUser2UserGroupMode.HIDE)) {
            domainBusinessAccountUserService.loadGroups(src);
            dst.setUserGroupIds(userGroupRestDTOMapper.postpone(src.getUserGroupKit(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserGroupMode.DomainBusinessAccountUser2UserGroupMode.SHORT))));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainBusinessAccountUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(UserGroupMode.DomainBusinessAccountUser2UserGroupMode.HIDE)) {
            domainBusinessAccountUserService.loadGroups(srcCollection);
        }
    }
}
