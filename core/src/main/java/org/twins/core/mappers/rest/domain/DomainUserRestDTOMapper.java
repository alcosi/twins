package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserCollectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.domain.DomainUserService;

import java.util.Collection;
import java.util.Locale;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DomainUserMode.class, BusinessAccountUserCollectionMode.class})
public class DomainUserRestDTOMapper extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv1> {
    @MapperModePointerBinding(modes = UserMode.DomainUser2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    private final DomainUserService domainUserService;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setLastActivityAt(src.getLastActivityAt() != null ? src.getLastActivityAt().toLocalDateTime() : null)
                        .setCurrentLocale(src.getI18nLocaleId() != null ? src.getI18nLocaleId() : Locale.ROOT);
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId());
                break;
        }
        if (mapperContext.hasModeButNot(UserMode.DomainUser2UserMode.HIDE)) {
            domainUserService.loadUser(src);
            dst.setUserId(src.getUserId());
            userDTOMapper.postpone(src.getUser(), mapperContext.forkOnPoint(UserMode.DomainUser2UserMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.DomainUser2UserMode.HIDE))
            domainUserService.loadUser(srcCollection);
    }
}
