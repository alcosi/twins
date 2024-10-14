package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountUserDTOMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.businessaccount.BusinessAccountService;

import java.util.Collection;
import java.util.Locale;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DomainUserMode.DomainUser2DomainUserMode.class, BusinessAccountUserCollectionMode.class})
public class DomainUserRestDTOMapperV2 extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv2> {

    @MapperModePointerBinding(modes = UserMode.DomainUser2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    private final BusinessAccountUserDTOMapperV2 businessAccountUserDTOMapperV2;

    private final BusinessAccountService businessAccountService;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainUserMode.DomainUser2DomainUserMode.DETAILED)) {
            case DETAILED:
                businessAccountService.loadBusinessAccounts(src);
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId())
                        .setUser(userDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DomainUser2UserMode.SHORT))))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCurrentLocale(src.getI18nLocaleId() != null ? src.getI18nLocaleId() : Locale.ROOT);
                if (src.getBusinessAccountUserKit() != null) {
                    dst.setBusinessAccountUserIdList(src.getBusinessAccountUserKit().getIdSet());
                    //todo need if?
                    if (mapperContext.hasMode(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.DETAILED))
                        convertOrPostpone(src.getBusinessAccountUserKit(), dst, businessAccountUserDTOMapperV2, mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.SHORT)), DomainUserDTOv2::setBusinessAccountUsers, DomainUserDTOv2::setBusinessAccountUserIdList);
                }
                break;
            case SHORT:
                dst.setId(src.getId());
                break;
        }
    }

    private static boolean showBusinessAccountUserCollection(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(BusinessAccountUserCollectionMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showBusinessAccountUserCollection(mapperContext))
            businessAccountService.loadBusinessAccounts(srcCollection);
    }
}
